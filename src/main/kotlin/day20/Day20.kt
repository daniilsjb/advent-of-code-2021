package day20

import java.io.File

fun main() {
    val data = parse("src/main/kotlin/day20/Day20.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 20 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

fun parse(path: String): Enhance {
    val (algorithm, bits) = File(path)
        .readText()
        .trim()
        .split("""(\n\n)|(\r\n\r\n)""".toRegex())

    return Enhance(bits.toImage(), algorithm)
}

fun String.toImage(): Image =
    this.lines()
        .let { lines -> lines.joinToString(separator = "") to lines.size }
        .let { (bitmap, size) -> Image(bitmap, size) }

data class Enhance(
    val image: Image,
    val algorithm: String,
)

data class Image(
    val bitmap: String,
    val size: Int,
    val fill: Char = '.',
)

fun <T> Iterable<T>.cross(other: Iterable<T>): List<Pair<T, T>> =
    this.flatMap { a -> other.map { b -> a to b } }

operator fun Image.get(x: Int, y: Int): Char =
    if (x in 0 until size && y in 0 until size) {
        bitmap[y * size + x]
    } else {
        fill
    }

fun Image.adjacent(x: Int, y: Int): List<Char> =
    (-1..1).cross(-1..1)
        .map { (dy, dx) -> this[x + dx, y + dy] }

fun Image.indexAt(x: Int, y: Int): Int =
    this.adjacent(x, y)
        .fold(0) { acc, c -> acc shl 1 or if (c == '#') 1 else 0 }

fun Enhance.step(): Enhance {
    // After each iteration, the image expands one unit in each direction,
    // increasing the length of its sides by 2 in total.
    val size = image.size + 2
    val side = -1 until image.size + 1
    val bitmap = (side).cross(side)
        .map { (y, x) -> image.indexAt(x, y) }
        .map { index -> algorithm[index] }
        .joinToString(separator = "")

    val fill = if (image.fill == '.') {
        algorithm.first()
    } else {
        algorithm.last()
    }

    return this.copy(image = Image(bitmap, size, fill))
}

fun Enhance.perform(iterations: Int): Image =
    generateSequence(this) { p -> p.step() }
        .elementAt(iterations).image

fun Enhance.countAfter(iterations: Int): Int =
    perform(iterations)
        .bitmap.count { pixel -> pixel == '#' }

fun part1(enhance: Enhance): Int =
    enhance.countAfter(iterations = 2)

fun part2(enhance: Enhance): Int =
    enhance.countAfter(iterations = 50)
