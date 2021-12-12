package day12

import java.io.File

fun main() {
    val data = parse("src/main/kotlin/day12/Day12.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 12 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

typealias Label = String
typealias Graph = Map<Label, List<Label>>

fun parse(path: String): Graph {
    val graph = mutableMapOf<Label, List<Label>>()

    for (line in File(path).readLines()) {
        val (lhs, rhs) = line.split("-")
        graph.merge(lhs, listOf(rhs)) { a, b -> a + b }
        graph.merge(rhs, listOf(lhs)) { a, b -> a + b }
    }

    return graph
}

// Depth-first count of all possible paths from a node to the end of the graph.
// When bias is set to true, a single small cave is allowed to be visited twice.
//
// There are several inefficiencies about this solution:
// - storing a unique copy of the history of visited nodes per recursive call is
// expensive, and could be replaced with a simple mutable list/map;
// - storing labels as strings and performing string comparison is slow, and could
// be optimized by assigning unique numeric identifiers during parsing.
//
// Nonetheless, this solution is simple and runs fast enough for me :)
fun countPaths(graph: Graph, current: Label, visited: List<Label> = listOf(current), bias: Boolean = false): Int =
    graph.getValue(current).sumOf { neighbour ->
        if (neighbour == "end") {
            1
        } else if (neighbour == "start") {
            0
        } else if (neighbour.all(Char::isLowerCase)) {
            if (!visited.contains(neighbour)) {
                countPaths(graph, neighbour, visited + neighbour, bias)
            } else if (bias) {
                countPaths(graph, neighbour, visited + neighbour)
            } else {
                0
            }
        } else {
            countPaths(graph, neighbour, visited + neighbour, bias)
        }
    }

fun part1(graph: Graph) =
    countPaths(graph, "start")

fun part2(graph: Graph) =
    countPaths(graph, "start", bias = true)
