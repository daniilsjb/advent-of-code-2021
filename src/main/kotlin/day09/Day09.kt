package day09

import java.io.File

fun main() {
    val data = parse("src/main/kotlin/day09/Day09.txt")

    val answer1 = part1(data.toHeightmap())
    val answer2 = part2(data.toHeightmap())

    println("🎄 Day 09 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

fun parse(path: String): Triple<Int, Int, List<Int>> {
    val lines = File(path).readLines()

    val width = lines.first().length
    val height = lines.size

    val locations = lines.flatMap { line ->
        line.map(Char::digitToInt)
    }

    return Triple(width, height, locations)
}

fun Triple<Int, Int, List<Int>>.toHeightmap() =
    let { (width, height, locations) ->
        Heightmap(width, height, locations.map(::Location))
    }

data class Heightmap(
    val width: Int,
    val height: Int,
    val locations: List<Location>,
)

data class Location(
    val height: Int,
    var visited: Boolean = false,
)

operator fun Heightmap.get(x: Int, y: Int) =
    locations[y * width + x]

fun adjacentNodes(x: Int, y: Int, width: Int, height: Int) =
    listOfNotNull(
        if (x - 1 >= 0)     x - 1 to y else null,
        if (x + 1 < width)  x + 1 to y else null,
        if (y - 1 >= 0)     x to y - 1 else null,
        if (y + 1 < height) x to y + 1 else null,
    )

fun part1(heightmap: Heightmap): Int {
    var sum = 0
    val (width, height) = heightmap
    for (y in 0 until height) {
        for (x in 0 until width) {
            val node = heightmap[x, y]
            val isLowestPoint = adjacentNodes(x, y, width, height)
                .all { (nx, ny) -> heightmap[nx, ny].height > node.height }

            if (isLowestPoint) {
                sum += node.height + 1
            }
        }
    }
    return sum
}

fun Location.isInsideUnexploredBasin() =
    !visited && height != 9

fun Location.visit() {
    visited = true
}

fun countNodesInBasin(heightmap: Heightmap, x: Int, y: Int): Int {
    val nodesToVisit = ArrayDeque(listOf(x to y))

    var count = 0
    while (nodesToVisit.isNotEmpty()) {
        val (nx, ny) = nodesToVisit.removeLast()

        val node = heightmap[nx, ny]
        if (node.isInsideUnexploredBasin()) {
            node.visit()
            count += 1

            adjacentNodes(nx, ny, heightmap.width, heightmap.height)
                .forEach(nodesToVisit::addLast)
        }
    }

    return count
}

fun part2(heightmap: Heightmap): Int {
    val basins = mutableListOf<Int>()

    val (width, height) = heightmap
    for (y in 0 until height) {
        for (x in 0 until width) {
            val node = heightmap[x, y]
            if (node.isInsideUnexploredBasin()) {
                basins.add(countNodesInBasin(heightmap, x, y))
            }
        }
    }

    return basins
        .sortedDescending()
        .take(3)
        .fold(1, Int::times)
}
