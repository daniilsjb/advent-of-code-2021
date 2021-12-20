package day17

import java.io.File
import kotlin.math.abs
import kotlin.math.max

fun main() {
    val data = parse("src/main/kotlin/day17/Day17.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 17 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

private data class Target(
    val x1: Int,
    val x2: Int,
    val y1: Int,
    val y2: Int,
)

private fun parse(path: String): Target {
    val regex = """target area: x=(-?\d+)\.\.(-?\d+), y=(-?\d+)\.\.(-?\d+)""".toRegex()

    val contents = File(path).readText()
    val (x1, x2, y1, y2) = regex.find(contents)!!.destructured

    return Target(x1.toInt(), x2.toInt(), y1.toInt(), y2.toInt())
}

// Frankly, this problem is very confusing to me, and I doubt this solution
// will work for any input that adheres to the description. But it worked for
// me, and I don't feel interested in making this any more flexible ¯\_(ツ)_/¯

private fun part1(target: Target): Int =
    (abs(target.y1) - 1).let { n -> n * (n + 1) / 2 }

private fun Target.isReachedBy(vx: Int, vy: Int): Boolean {
    var (x, y) = 0 to 0
    var (dx, dy) = vx to vy

    while (x <= x2 && y >= y1) {
        x += dx
        y += dy

        if (x in x1..x2 && y in y1..y2) {
            return true
        }

        dx = max(dx - 1, 0)
        dy -= 1
    }

    return false
}

private fun part2(target: Target): Int {
    val (vx0, vx1) = 1 to target.x2
    val (vy0, vy1) = target.y1 to part1(target)

    var counter = 0
    for (vy in vy0..vy1) {
        for (vx in vx0..vx1) {
            if (target.isReachedBy(vx, vy)) {
                ++counter
            }
        }
    }

    return counter
}
