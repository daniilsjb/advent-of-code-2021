package day09

import java.io.File

fun main() {
    val (width, height, locations) = parse("src/main/kotlin/day09/Day09.txt")

    val answer1 = part1(Heightmap(width, height, locations.map(::Location)))
    val answer2 = part2(Heightmap(width, height, locations.map(::Location)))

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

fun part1(heightmap: Heightmap) =
    (0 until heightmap.height).sumOf { y ->
        (0 until heightmap.width).sumOf { x ->
            val (height, _) = heightmap[x, y]
            adjacentNodes(x, y, heightmap.width, heightmap.height)
                .all { (nx, ny) -> heightmap[nx, ny].height > height }
                .let { lowest -> if (lowest) height + 1 else 0 }
        }
    }

fun floodFill(heightmap: Heightmap, x: Int, y: Int): Int {
    val nodesToVisit = ArrayDeque<Pair<Int, Int>>()
        .apply { addLast(x to y) }

    var count = 0
    while (nodesToVisit.isNotEmpty()) {
        val (nx, ny) = nodesToVisit.removeLast()

        val node = heightmap[nx, ny]
        if (node.visited || node.height == 9) {
            continue
        }

        node.visited = true
        adjacentNodes(nx, ny, heightmap.width, heightmap.height)
            .forEach(nodesToVisit::addLast)

        count += 1
    }
    return count
}

fun part2(heightmap: Heightmap) =
    (0 until heightmap.height).flatMap { y ->
        (0 until heightmap.width).mapNotNull { x ->
            heightmap[x, y].let { (height, visited) ->
                if (visited || height == 9) {
                    null
                } else {
                    floodFill(heightmap, x, y)
                }
            }
        }
    }
    .sortedDescending()
    .take(3)
    .fold(1, Int::times)
