package day22

import java.io.File
import kotlin.math.max
import kotlin.math.min

fun main() {
    val data = parse("src/main/kotlin/day22/Day22.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 22 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

private data class Cuboid(
    val x0: Int, val x1: Int,
    val y0: Int, val y1: Int,
    val z0: Int, val z1: Int,
    val on: Boolean = true,
)

private fun Cuboid.overlap(other: Cuboid): Cuboid? {
    val (ax0, ax1, ay0, ay1, az0, az1) = this
    val (bx0, bx1, by0, by1, bz0, bz1) = other

    val ox0 = max(ax0, bx0)
    val ox1 = min(ax1, bx1)
    val oy0 = max(ay0, by0)
    val oy1 = min(ay1, by1)
    val oz0 = max(az0, bz0)
    val oz1 = min(az1, bz1)

    return if (ox0 <= ox1 && oy0 <= oy1 && oz0 <= oz1) {
        Cuboid(ox0, ox1, oy0, oy1, oz0, oz1)
    } else {
        null
    }
}

/*
 * A recursive solution based on the principle of exclusion and inclusion.
 * To be fair, this is not the first solution I came up with. My initial idea
 * was to maintain a list of non-overlapping cuboids by splitting every cuboid
 * into smaller ones whenever overlaps occur, and then calculating the sum of
 * their volumes.
 *
 * However, I found out about this principle and thought that this was a much
 * neater way of implementing this, and it works quite fast, too. There is
 * always something new to learn! :)
 */
private fun count(cuboids: List<Cuboid>): Long {
    if (cuboids.isEmpty()) {
        return 0
    }

    val (head, rest) = cuboids.first() to cuboids.drop(1)
    return if (head.on) {
        val intersections = rest.mapNotNull(head::overlap)
        head.volume() + count(rest) - count(intersections)
    } else {
        count(rest)
    }
}

private fun Cuboid.volume(): Long {
    val xs = (x1 - x0 + 1).toLong()
    val ys = (y1 - y0 + 1).toLong()
    val zs = (z1 - z0 + 1).toLong()
    return xs * ys * zs
}

private fun parse(path: String): List<Cuboid> =
    File(path)
        .readLines()
        .map(String::toCuboid)

private fun String.toCuboid(): Cuboid {
    val (valuePart, coordinatesPart) = this.split(" ")
    val (x, y, z) = coordinatesPart
        .split(",")
        .map { it.drop(2) }

    val (x0, x1) = x.toBounds()
    val (y0, y1) = y.toBounds()
    val (z0, z1) = z.toBounds()

    val on = valuePart == "on"
    return Cuboid(x0, x1, y0, y1, z0, z1, on)
}

private fun String.toBounds(): Pair<Int, Int> =
    this.split("..")
        .map(String::toInt)
        .let { (a, b) -> a to b }

private fun part1(cuboids: List<Cuboid>): Long =
    cuboids
        .filter { (x0, x1, y0, y1, z0, z1) -> arrayOf(x0, x1, y0, y1, z0, z1).all { it in -50..50 } }
        .let(::count)

private fun part2(cuboids: List<Cuboid>): Long =
    count(cuboids)
