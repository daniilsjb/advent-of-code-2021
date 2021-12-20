package day14

import java.io.File

fun main() {
    val (template, rules) = parse("src/main/kotlin/day14/Day14.txt")

    val answer1 = part1(template, rules)
    val answer2 = part2(template, rules)

    println("🎄 Day 14 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

private typealias Template = String
private typealias Rules = Map<String, Char>

private fun parse(path: String): Pair<Template, Rules> {
    val (template, rulesPart) = File(path)
        .readText()
        .trim()
        .split("""(\n\n)|(\r\n\r\n)""".toRegex())

    val rules = rulesPart
        .lines()
        .associate(String::toRule)

    return template to rules
}

private fun String.toRule() =
    this.split(" -> ")
        .let { (match, element) -> match to element.first() }

private fun expand(template: Template, rules: Rules, iterations: Int): Long {
    val frequencies = template
        .groupingBy { it }
        .eachCount()
        .mapValuesTo(mutableMapOf()) { (_, v) -> v.toLong() }

    var patterns = template
        .zipWithNext { a, b -> "$a$b" }
        .groupingBy { it }
        .eachCount()
        .mapValues { (_, v) -> v.toLong() }

    repeat(iterations) {
        val next = mutableMapOf<String, Long>()
        for ((pattern, count) in patterns) {
            val element = rules.getValue(pattern)

            val lhs = "${pattern[0]}$element"
            val rhs = "$element${pattern[1]}"

            next.merge(lhs, count, Long::plus)
            next.merge(rhs, count, Long::plus)

            frequencies.merge(element, count, Long::plus)
        }
        patterns = next
    }

    val max = frequencies.maxOf { it.value }
    val min = frequencies.minOf { it.value }
    return max - min
}

private fun part1(template: Template, rules: Rules) =
    expand(template, rules, iterations = 10)

private fun part2(template: Template, rules: Rules) =
    expand(template, rules, iterations = 40)
