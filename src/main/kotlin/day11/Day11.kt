package day11

import java.io.File

fun main() {
    val data = parse("src/main/kotlin/day11/Day11.txt")

    val answer1 = part1(data.toMutableList())
    val answer2 = part2(data.toMutableList())

    println("🎄 Day 11 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

private fun parse(path: String): List<Int> =
    File(path)
        .readLines()
        .flatMap { it.map(Char::digitToInt) }

private fun adjacentNodes(x: Int, y: Int): List<Pair<Int, Int>> =
    listOfNotNull(
        if (x - 1 >= 0 && y - 1 >= 0) x - 1 to y - 1 else null,
        if (              y - 1 >= 0) x     to y - 1 else null,
        if (x + 1 < 10 && y - 1 >= 0) x + 1 to y - 1 else null,
        if (x - 1 >= 0              ) x - 1 to y     else null,
        if (x + 1 < 10              ) x + 1 to y     else null,
        if (x - 1 >= 0 && y + 1 < 10) x - 1 to y + 1 else null,
        if (              y + 1 < 10) x     to y + 1 else null,
        if (x + 1 < 10 && y + 1 < 10) x + 1 to y + 1 else null,
    )

private fun step(energy: MutableList<Int>): Int {
    var flashCount = 0

    val flashQueue = ArrayDeque<Pair<Int, Int>>(100)
    for (y in 0 until 10) {
        for (x in 0 until 10) {
            val index = y * 10 + x
             if (++energy[index] > 9) {
                flashQueue.addLast(x to y)
                energy[index] = 0
            }
        }
    }

    while (flashQueue.isNotEmpty()) {
        val (x, y) = flashQueue.removeLast()
        for ((nx, ny) in adjacentNodes(x, y)) {
            val index = ny * 10 + nx
             if (energy[index] != 0 && ++energy[index] > 9) {
                flashQueue.addLast(nx to ny)
                energy[index] = 0
            }
        }
        ++flashCount
    }

    return flashCount
}

private fun part1(energy: MutableList<Int>) =
    (0 until 100)
        .sumOf { step(energy) }

private fun part2(energy: MutableList<Int>) =
    generateSequence(1) { it + 1 }
        .first { step(energy) == 100 }
