package day08

import java.io.File

fun main() {
    val data = parse("src/main/kotlin/day08/Day08.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 08 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

private typealias Segments = Set<Char>
private typealias Entry = Pair<List<Segments>, List<Segments>>

private fun parse(path: String): List<Entry> =
    File(path)
        .readLines()
        .map(String::toEntry)

private fun String.toEntry(): Entry =
    this.split("|")
        .map { part -> part
            .trim()
            .split(" ")
            .map(String::toSet)
        }
        .let { (patterns, digits) -> Entry(patterns, digits) }

private fun part1(entries: List<Entry>) =
    entries.sumOf { (_, digits) ->
        digits.count { digit -> digit.size in arrayOf(2, 3, 4, 7) }
    }

private fun part2(entries: List<Entry>) =
    entries.sumOf { (patterns, digits) ->
        // The approach of this solution is to directly determine which
        // combinations of letters map to which digits.
        val mappings = Array(10) { setOf<Char>() }

        // First, we partition the unique digit patterns:
        // 1) digit '1' maps to the pattern with length 2
        // 2) digit '4' maps to the pattern with length 4
        // 3) digit '7' maps to the pattern with length 3
        // 4) digit '8' maps to the pattern with length 7
        //
        // Out of all digits, only 6 now remain:
        // - 3 of them have length 5 and may be one of '2','3','5'
        // - 3 of them have length 6 and may be one of '0','6','9'

        val patternsWithLength5 = mutableListOf<Segments>()
        val patternsWithLength6 = mutableListOf<Segments>()

        for (pattern in patterns) {
            when (pattern.size) {
                2 -> mappings[1] = pattern
                3 -> mappings[7] = pattern
                4 -> mappings[4] = pattern
                7 -> mappings[8] = pattern
                5 -> patternsWithLength5.add(pattern)
                6 -> patternsWithLength6.add(pattern)
            }
        }

        // Second, we can observe that there are overlaps between digit
        // patterns. We may use them to deduce new patterns from the
        // ones we already know.

        // 5) digit '6' maps to the pattern of length 6 that does not
        // contain all segments of digit '1'.
        mappings[6] = patternsWithLength6.first { pattern ->
            !pattern.containsAll(mappings[1])
        }
        // 6) digit '9' maps to the pattern of length 6 that contains
        // all segments of digit '4'
        mappings[9] = patternsWithLength6.first { pattern ->
            pattern.containsAll(mappings[4])
        }
        // 7) digit '0' maps to the last remaining pattern of length 6
        mappings[0] = patternsWithLength6.first { pattern ->
            pattern != mappings[6] && pattern != mappings[9]
        }
        // 8) digit '3' maps to the pattern of length 5 that contains
        // both segments of digit '1'
        mappings[3] = patternsWithLength5.first { pattern ->
            pattern.containsAll(mappings[1])
        }

        // Here the situation becomes trickier. I could not find a fitting
        // overlap to differentiate between digits 2 and 5, so we're going
        // to use another trick. Each pattern contains a segment initially
        // labelled 'f', except for digit '2'. If we find that segment,
        // the '2' is simply the pattern without it.
        val foundPatterns = mappings.filter(Set<Char>::isNotEmpty)
        val f = ('a'..'g').first { label ->
            foundPatterns.all { pattern -> pattern.contains(label) }
        }

        // 9) digit '2' maps to the pattern of length 5 that does not contain
        // segment initially labelled 'f'
        mappings[2] = patternsWithLength5.first { pattern ->
            !pattern.contains(f)
        }
        // 10) digit '5' maps to the last remaining pattern of length 5
        mappings[5] = patternsWithLength5.first { pattern ->
            pattern != mappings[3] && pattern != mappings[2]
        }

        // At this point it is enough to apply the mapping to each output
        // digit and combine them into an integer.
        digits.fold(0) { acc: Int, digit ->
            acc * 10 + mappings.indexOfFirst { it == digit }
        }
    }
