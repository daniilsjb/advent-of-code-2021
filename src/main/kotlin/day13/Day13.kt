package day13

import java.io.File
import kotlin.math.abs

fun main() {
    val (points, folds) = parse("src/main/kotlin/day13/Day13.txt")

    val answer1 = part1(points, folds)
    val answer2 = part2(points, folds)

    println("🎄 Day 13 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer:\n$answer2")
}

enum class Axis {
    X, Y
}

data class Point(
    val x: Int,
    val y: Int,
)

data class Fold(
    val axis: Axis,
    val line: Int,
)

data class Instructions(
    val points: Set<Point>,
    val folds: List<Fold>,
)

fun parse(path: String): Instructions {
    val (pointsPart, foldsPart) = File(path)
        .readText()
        .split("""(\n\n)|(\r\n\r\n)""".toRegex())
        .map(String::trim)

    val points = pointsPart
        .lines()
        .mapTo(HashSet(), String::toPoint)

    val folds = foldsPart
        .lines()
        .map(String::toFold)

    return Instructions(points, folds)
}

fun String.toPoint(): Point =
    this.split(",")
        .let { (lhs, rhs) -> Point(lhs.toInt(), rhs.toInt()) }

fun String.toFold(): Fold =
    this.removePrefix("fold along ")
        .split("=")
        .let { (lhs, rhs) -> Fold(lhs.toAxis(), rhs.toInt()) }

fun String.toAxis(): Axis =
    when (this) {
        "x" -> Axis.X
        "y" -> Axis.Y
        else -> error("Invalid folding axis")
    }

fun Point.reflect(axis: Axis, line: Int): Point =
    when (axis) {
        Axis.X -> Point(line - abs(x - line), y)
        Axis.Y -> Point(x, line - abs(y - line))
    }

fun part1(points: Set<Point>, folds: List<Fold>): Int {
    val (axis, line) = folds.first()
    return points
        .mapTo(HashSet()) { it.reflect(axis, line) }
        .count()
}

fun part2(points: Set<Point>, folds: List<Fold>): String {
    val code = folds.fold(points) { dots, (axis, line) ->
        dots.mapTo(HashSet())  { it.reflect(axis, line) }
    }

    return buildString {
        for (y in 0 until 6) {
            for (x in 0 until 40) {
                if (x % 5 == 0) {
                    append("  ")
                }
                append(if (code.contains(Point(x, y))) '#' else ' ')
            }
            append('\n')
        }
    }
}
