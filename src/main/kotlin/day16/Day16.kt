package day16

import java.io.File

fun main() {
    val data = parse("src/main/kotlin/day16/Day16.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 16 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

/*
 * AST
 */

private data class Packet(
    val header: Header,
    val payload: Payload,
)

private data class Header(
    val version: Int,
    val type: Int,
)

private sealed interface Payload {
    @JvmInline
    value class Literal(val value: Long) : Payload

    @JvmInline
    value class Operator(val operands: List<Packet>) : Payload
}

/*
 * Parsing
 */

private fun parse(path: String): Packet =
    File(path)
        .readText()
        .trim()
        .toPacket()

private fun String.toPacket(): Packet {
    val message = map(Char::toBitPattern)
        .joinToString(separator = "")

    return ParserState(message).packet()
}

private fun Char.toBitPattern(): String =
    digitToInt(16)
        .toString(2)
        .padStart(4, '0')

/* Parsing Information */

private data class ParserState(
    val message: String,
    var current: Int = 0,
)

/* Parsing Primitives */

private fun ParserState.consume(n: Int): String =
    message.substring(current, current + n).also { current += n }

private fun ParserState.finished(): Boolean =
    current >= message.length

private fun ParserState.readInt(n: Int): Int =
    consume(n).toInt(2)

private fun ParserState.readLong(n: Int): Long =
    consume(n).toLong(2)

/* Syntax Rules */

private fun ParserState.packet(): Packet {
    val header = Header(readInt(3), readInt(3))
    val payload = if (header.type == 4) {
        literal()
    } else {
        operator()
    }
    return Packet(header, payload)
}

private fun ParserState.literal(): Payload {
    var result = 0L

    do {
        val prefix = consume(1)
        result = result shl 4 or readLong(4)
    } while (prefix == "1")

    return Payload.Literal(result)
}

private fun ParserState.operator(): Payload {
    val packets = mutableListOf<Packet>()

    when (consume(1)) {
        "0" -> {
            val data = consume(readInt(15))
            val parser = ParserState(data)
            while (!parser.finished()) {
                packets.add(parser.packet())
            }
        }
        "1" -> {
            val number = readInt(11)
            repeat(number) {
                packets.add(packet())
            }
        }
    }

    return Payload.Operator(packets)
}

/*
 * Evaluation
 */

private fun sum(packet: Packet): Int {
    val (header, payload) = packet
    return when (payload) {
        is Payload.Literal -> header.version
        is Payload.Operator -> {
            header.version + payload.operands.sumOf(::sum)
        }
    }
}

private fun eval(packet: Packet): Long {
    val (header, payload) = packet
    return when (payload) {
        is Payload.Literal -> payload.value
        is Payload.Operator -> {
            val operands = payload.operands.map(::eval)
            return when (header.type) {
                0 -> operands.reduce(Long::plus)
                1 -> operands.reduce(Long::times)
                2 -> operands.minOf { it }
                3 -> operands.maxOf { it }
                5 -> operands.let { (a, b) -> if (a > b) 1 else 0 }
                6 -> operands.let { (a, b) -> if (a < b) 1 else 0 }
                7 -> operands.let { (a, b) -> if (a == b) 1 else 0 }
                else -> error("Invalid operator type")
            }
        }
    }
}

private fun part1(packet: Packet): Int =
    sum(packet)

private fun part2(packet: Packet): Long =
    eval(packet)