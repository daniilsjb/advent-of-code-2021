package day01

import java.io.File

fun main() {
    val data = parse("src/main/kotlin/day01/Day01.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 01 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

fun parse(path: String): List<Int> =
    File(path)
        .readLines()
        .map(String::toInt)

fun part1(numbers: List<Int>) =
    numbers
        .zipWithNext { previous, next -> if (next > previous) 1 else 0 }
        .sum()

fun part2(numbers: List<Int>) =
    numbers.windowed(size = 3, transform = List<Int>::sum)
        .zipWithNext { previous, next -> if (next > previous) 1 else 0 }
        .sum()