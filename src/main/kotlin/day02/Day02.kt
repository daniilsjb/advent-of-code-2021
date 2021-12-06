package day02

import java.io.File

fun main() {
    val data = parse("src/main/kotlin/day02/Day02.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 02 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

enum class Action {
    Up, Down, Forward,
}

data class Command(
    val action: Action,
    val number: Int,
)

fun parse(path: String): List<Command> =
    File(path)
        .readLines()
        .map(String::toCommand)

fun String.toCommand(): Command {
    val (actionPart, numberPart) = this.split(" ")
    val action = when (actionPart) {
        "up" -> Action.Up
        "down" -> Action.Down
        "forward" -> Action.Forward
        else -> error("Invalid action")
    }
    val number = numberPart.toInt()
    return Command(action, number)
}

fun part1(commands: List<Command>): Int {
    var (horizontal, depth) = arrayOf(0, 0)
    for ((action, number) in commands) {
        when (action) {
            Action.Up -> depth -= number
            Action.Down -> depth += number
            Action.Forward -> horizontal += number
        }
    }
    return horizontal * depth
}

fun part2(commands: List<Command>): Int {
    var (horizontal, depth, aim) = arrayOf(0, 0, 0)
    for ((action, number) in commands) {
        when (action) {
            Action.Up -> aim -= number
            Action.Down -> aim += number
            Action.Forward -> {
                horizontal += number
                depth += aim * number
            }
        }
    }
    return horizontal * depth
}
