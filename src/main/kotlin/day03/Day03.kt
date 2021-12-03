package day03

import java.io.File

fun main() {
    val data = File("src/main/kotlin/day03/Day03.txt")
        .readLines()

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 03 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

fun part1(lines: List<String>): Int {
    val bitCount = lines[0].length
    val gamma = (0 until bitCount)
        .map { index -> lines.count { line -> line[index] == '1' } }
        .map { count -> if (2 * count > lines.size) 1 else 0 }
        .fold(0) { acc, bit -> (acc shl 1) or bit }

    val epsilon = gamma xor ((1 shl bitCount) - 1)
    return gamma * epsilon
}

fun part2(lines: List<String>): Int {
    val calculateRating = { criteria: Char, complement: Char ->
        var index = 0
        val values = lines.toMutableList()
        while (values.size != 1) {
            val count = values.count { it[index] == '1' }
            val bit = if (2 * count >= values.size) criteria else complement
            values.removeAll { it[index] == bit }
            ++index
        }
        values.first().toInt(radix = 2)
    }

    val o2 = calculateRating('1', '0')
    val co2 = calculateRating('0', '1')

    return o2 * co2
}
