package day06

import java.io.File

fun main() {
    val data = parse("src/main/kotlin/day06/Day06.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 06 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

fun parse(path: String): List<Int> =
    File(path)
        .readText()
        .trim()
        .split(",")
        .map(String::toInt)

fun simulate(pool: List<Int>, iterations: Int): Long {
    val queue = ArrayDeque(listOf<Long>(0, 0, 0, 0, 0, 0, 0, 0, 0))
    for (item in pool) {
        ++queue[item]
    }

    for (day in 0 until iterations) {
        val breeders = queue.removeFirst()
        queue.addLast(breeders)
        queue[queue.lastIndex - 2] += breeders
    }

    return queue.sum()
}

fun part1(pool: List<Int>) =
    simulate(pool, 80)

fun part2(pool: List<Int>) =
    simulate(pool, 256)