package day05

import java.io.File
import kotlin.math.min
import kotlin.math.max

fun main() {
    val data = parse("src/main/kotlin/day05/Day05.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 05 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

private data class Point(
    val x: Int,
    val y: Int,
)

private data class Line(
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int,
)

private fun parse(path: String): List<Line> =
    File(path)
        .readLines()
        .map(String::toLine)

private fun String.toLine(): Line {
    val regex = """(\d+),(\d+) -> (\d+),(\d+)""".toRegex()
    val (x1, y1, x2, y2) = regex.find(this)!!
        .destructured
        .toList()
        .map(String::toInt)

    return Line(x1, y1, x2, y2)
}

private fun part1(lines: List<Line>) =
    lines.asSequence()
        .flatMap { (x1, y1, x2, y2) ->
            if (y1 == y2) {
                (min(x1, x2)..max(x1, x2)).asSequence().map { x -> Point(x, y1) }
            } else if (x1 == x2) {
                (min(y1, y2)..max(y1, y2)).asSequence().map { y -> Point(x1, y) }
            } else {
                sequenceOf()
            }
        }
        .groupingBy { it }
        .eachCount()
        .count { (_, frequency) -> frequency >= 2 }

private fun part2(lines: List<Line>) =
    lines.asSequence()
        .flatMap { (x1, y1, x2, y2) ->
            if (y1 == y2) {
                (min(x1, x2)..max(x1, x2)).asSequence().map { x -> Point(x, y1) }
            } else if (x1 == x2) {
                (min(y1, y2)..max(y1, y2)).asSequence().map { y -> Point(x1, y) }
            } else {
                val xd = if (x2 > x1) 1 else -1
                val yd = if (y2 > y1) 1 else -1

                (0..(max(x1, x2) - min(x1, x2))).asSequence()
                    .map { delta -> Point(x1 + delta * xd, y1 + delta * yd) }
            }
        }
        .groupingBy { it }
        .eachCount()
        .count { (_, frequency) -> frequency >= 2 }