package day19

import java.io.File
import kotlin.math.abs

fun main() {
    val data = parse("src/main/kotlin/day19/Day19.txt")

    val origin = Point(0, 0, 0)
    val pivot = TranslatedScanner(origin, data[0].beacons)

    val scanners = data.drop(1)
    val translated = translateAll(scanners, listOf(pivot))

    val answer1 = part1(translated)
    val answer2 = part2(translated)

    println("🎄 Day 19 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

/*
 * Point
 */

data class Point(
    val x: Int,
    val y: Int,
    val z: Int,
)

operator fun Point.plus(other: Point): Point =
    Point(x + other.x, y + other.y, z + other.z)

operator fun Point.minus(other: Point): Point =
    Point(x - other.x, y - other.y, z - other.z)

fun distance(p1: Point, p2: Point): Int =
    abs(p2.x - p1.x) + abs(p2.y - p1.y) + abs(p2.z - p1.z)

/*
 * Rotations
 */

val Rotations = listOf<(Point) -> Point>(
    // 90° rotations around +x
    { (x, y, z) -> Point(+x, +y, +z) },
    { (x, y, z) -> Point(+x, -z, +y) },
    { (x, y, z) -> Point(+x, -y, -z) },
    { (x, y, z) -> Point(+x, +z, -y) },

    // 90° rotations around -x
    { (x, y, z) -> Point(-x, -y, +z) },
    { (x, y, z) -> Point(-x, +z, +y) },
    { (x, y, z) -> Point(-x, +y, -z) },
    { (x, y, z) -> Point(-x, -z, -y) },

    // 90° rotations around +y
    { (x, y, z) -> Point(+y, +z, +x) },
    { (x, y, z) -> Point(+y, -x, +z) },
    { (x, y, z) -> Point(+y, -z, -x) },
    { (x, y, z) -> Point(+y, +x, -z) },

    // 90° rotations around -y
    { (x, y, z) -> Point(-y, -z, +x) },
    { (x, y, z) -> Point(-y, +x, +z) },
    { (x, y, z) -> Point(-y, +z, -x) },
    { (x, y, z) -> Point(-y, -x, -z) },

    // 90° rotations around +z
    { (x, y, z) -> Point(+z, +x, +y) },
    { (x, y, z) -> Point(+z, -y, +x) },
    { (x, y, z) -> Point(+z, -x, -y) },
    { (x, y, z) -> Point(+z, +y, -x) },

    // 90° rotations around -z
    { (x, y, z) -> Point(-z, -x, +y) },
    { (x, y, z) -> Point(-z, +y, +x) },
    { (x, y, z) -> Point(-z, +x, -y) },
    { (x, y, z) -> Point(-z, -y, -x) },
)

fun Set<Point>.getOrientations(): List<List<Point>> =
    Rotations.map { rotate -> this.map(rotate) }

/*
 * Scanner
 */

data class Scanner(val beacons: Set<Point>) {
    // All orientations are pre-computed because they never change. This way we
    // avoid rotating points each time a scanner is tried for overlaps.
    val orientations = beacons.getOrientations()
}

/*
 * Translation
 * -----------
 *
 * The main idea behind this problem is that the positions of all beacons and
 * scanners must be converted into a common coordinate system before we can
 * perform sensible operations on them. Because initially all coordinates are
 * expressed in each scanner's local space, we need to first choose a global,
 * or primary, space in which all final coordinates will be expressed.
 *
 * This choice is arbitrary, and the simplest approach is to choose the 0th
 * scanner as the pivot. This also means that the pivot scanner is already
 * correct: we don't need to fix its orientation or change its coordinates in
 * any way.
 *
 * For every other scanner, we need to find its relative position to the pivot
 * scanner, which is possible (according to the problem) when two scanners have
 * at least 12 overlapping beacons. Once we find a scanner like this and find
 * its shift from the pivot, we can use the same shift to translate its
 * beacons to primary space.
 *
 * Once a scanner is "translated" into the primary space, it also becomes part
 * of the pivot and is used when determining further translations. This process
 * continues until there no scanners are left untranslated.
 */

data class TranslatedScanner(
    val position: Point,
    val beacons: Set<Point>,
)

tailrec fun translateAll(scanners: List<Scanner>, pivots: List<TranslatedScanner>): List<TranslatedScanner> {
    if (scanners.isEmpty()) {
        return pivots
    }

    val newScanners = mutableListOf<Scanner>()
    val newPivots = pivots.toMutableList()

    for (scanner in scanners) {
        when (val translated = translate(scanner, pivots)) {
            null -> newScanners.add(scanner)
            else -> newPivots.add(translated)
        }
    }

    return translateAll(newScanners, newPivots)
}

fun translate(scanner: Scanner, pivots: List<TranslatedScanner>): TranslatedScanner? {
    return pivots.firstNotNullOfOrNull { pivot -> findOverlap(scanner, pivot) }
}

fun findOverlap(scanner: Scanner, pivot: TranslatedScanner): TranslatedScanner? {
    for (orientation in scanner.orientations) {
        for (translated in pivot.beacons) {
            for (beacon in orientation) {
                val shift = translated - beacon
                val shifted = orientation.mapTo(HashSet()) { b -> b + shift }
                val common = pivot.beacons intersect shifted
                if (common.size >= 12) {
                    return TranslatedScanner(shift, shifted)
                }
            }
        }
    }
    return null
}

/*
 * Parsing
 */

fun parse(path: String): List<Scanner> {
    val blocks = File(path)
        .readText()
        .trim()
        .split("""(\n\n)|(\r\n\r\n)""".toRegex())

    return blocks
        .map(String::toBeacons)
        .map(::Scanner)
}

fun String.toBeacons(): Set<Point> =
    this.lines()
        .drop(1)
        .mapTo(HashSet(), String::toPoint)

fun String.toPoint(): Point =
    this.split(",")
        .map(String::toInt)
        .let { (x, y, z) -> Point(x, y, z) }

/*
 * Solutions
 */

fun part1(scanners: List<TranslatedScanner>): Int =
    scanners
        .flatMapTo(HashSet()) { it.beacons}
        .count()

fun part2(scanners: List<TranslatedScanner>): Int =
    scanners.maxOf { a ->
        scanners.maxOf { b ->
            distance(a.position, b.position)
        }
    }