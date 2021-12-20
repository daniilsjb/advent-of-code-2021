package day10

import java.io.File

fun main() {
    val data = parse("src/main/kotlin/day10/Day10.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 10 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

private fun parse(path: String) =
    File(path).readLines()

private fun part1(lines: List<String>): Long {
    var sum = 0L
    for (line in lines) {
        val stack = ArrayDeque<Char>()
        for (char in line) {
            when (char) {
                '(' -> stack.addLast(')')
                '[' -> stack.addLast(']')
                '{' -> stack.addLast('}')
                '<' -> stack.addLast('>')
                else -> if (char != stack.removeLast()) {
                    when (char) {
                        ')' -> sum += 3
                        ']' -> sum += 57
                        '}' -> sum += 1197
                        '>' -> sum += 25137
                    }
                    break
                }
            }
        }
    }
    return sum
}

private fun part2(lines: List<String>): Long {
    val scores = mutableListOf<Long>()

    outer@
    for (line in lines) {
        val stack = ArrayDeque<Char>()
        for (char in line) {
            when (char) {
                '(' -> stack.addFirst(')')
                '[' -> stack.addFirst(']')
                '{' -> stack.addFirst('}')
                '<' -> stack.addFirst('>')
                else -> if (char != stack.removeFirst()) {
                    continue@outer
                }
            }
        }
        val score = stack.fold(0) { acc: Long, expected ->
            when (expected) {
                ')' -> acc * 5 + 1
                ']' -> acc * 5 + 2
                '}' -> acc * 5 + 3
                '>' -> acc * 5 + 4
                else -> error("Invalid closing character")
            }
        }
        scores.add(score)
    }

    return scores.sorted().run { this[size / 2] }
}