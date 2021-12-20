package day15

import java.io.File
import java.util.*

fun main() {
    val data = parse("src/main/kotlin/day15/Day15.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 15 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

private data class Graph(
    val size: Int,
    val vertices: List<Int>,
)

private data class Vertex(
    val x: Int,
    val y: Int,
)

private data class Path(
    val target: Vertex,
    val distance: Int,
) : Comparable<Path> {

    override fun compareTo(other: Path): Int =
        distance.compareTo(other.distance)
}

private fun parse(path: String): Graph {
    val lines = File(path).readLines()

    val size = lines.size
    val nodes = lines.flatMap { line ->
        line.map(Char::digitToInt)
    }

    return Graph(size, nodes)
}

private fun Graph.distanceAt(vertex: Vertex): Int {
    val dx = vertex.x / size
    val dy = vertex.y / size

    val px = vertex.x % size
    val py = vertex.y % size

    val combinedCost = vertices[py * size + px] + dx + dy
    return (combinedCost - 1) % 9 + 1
}

private fun Vertex.edges(target: Vertex): List<Vertex> =
    listOfNotNull(
        if (x - 1 > 0)         Vertex(x - 1, y) else null,
        if (x + 1 <= target.x) Vertex(x + 1, y) else null,
        if (y - 1 > 0)         Vertex(x, y - 1) else null,
        if (y + 1 <= target.y) Vertex(x, y + 1) else null,
    )

private fun findPath(graph: Graph, target: Vertex): Int {
    val source = Vertex(0, 0)

    val distances = mutableMapOf(source to 0)
    val frontier = PriorityQueue<Path>()
        .apply { add(Path(target = source, distance = 0)) }

    while (frontier.isNotEmpty()) {
        val (vertex, distance) = frontier.poll()
        if (vertex == target) {
            return distance
        }

        if (distance > distances.getValue(vertex)) continue
        for (edge in vertex.edges(target)) {
            val distanceToEdge = distance + graph.distanceAt(edge)
            if (distanceToEdge < distances.getOrDefault(edge, Int.MAX_VALUE)) {
                distances[edge] = distanceToEdge
                frontier.add(Path(edge, distanceToEdge))
            }
        }
    }

    error("Could not find the shortest path.")
}

private fun part1(graph: Graph): Int =
    findPath(graph, target = Vertex(graph.size - 1, graph.size - 1))

private fun part2(graph: Graph): Int =
    findPath(graph, target = Vertex(graph.size * 5 - 1, graph.size * 5 - 1))
