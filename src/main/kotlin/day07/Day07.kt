package day07

import java.io.File
import kotlin.math.abs

fun main() {
    val data = parse("src/main/kotlin/day07/Day07.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 07 🎄")

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

fun part1(numbers: List<Int>) =
    (0..numbers.maxOf { it })
        .map { focus -> numbers.sumOf { position -> abs(focus - position) } }
        .minOf { it }

fun part2(numbers: List<Int>) =
    (0..numbers.maxOf { it })
        .map { focus -> numbers.sumOf { position -> abs(focus - position).let { n -> n * (n + 1) shr 1 } } }
        .minOf { it }