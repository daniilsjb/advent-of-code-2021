package day25

import java.io.File

fun main() {
    val data = parse("src/main/kotlin/day25/Day25.txt")

    val answer = solve(data)

    println("🎄 Day 25 🎄")
    println("Answer: $answer")
}

private data class Area(
    val width: Int,
    val height: Int,
    val cells: List<Char>,
)

private fun Area.getIndex(x: Int, y: Int): Int {
    val xi = if (x >= width) {
        x - width
    } else if (x < 0) {
        x + width
    } else {
        x
    }

    val yi = if (y >= height) {
        y - height
    } else if (y < 0) {
        y + height
    } else {
        y
    }

    return yi * width + xi
}

private fun parse(path: String): Area =
    File(path)
        .readLines()
        .let { lines ->
            val height = lines.size
            val width = lines[0].length
            val cells = lines.flatMap(String::toList)

            Area(width, height, cells)
        }

private fun move(area: Area, herd: Char): Area {
    val dx = if (herd == '>') 1 else 0
    val dy = if (herd == '>') 0 else 1

    val prev = area.cells
    val next = prev.toMutableList()

    for (y in 0 until area.height) {
        for (x in 0 until area.width) {
            val index = area.getIndex(x, y)
            if (prev[index] == herd) {
                val nextIndex = area.getIndex(x + dx, y + dy)
                if (prev[nextIndex] == '.') {
                    next[nextIndex] = herd
                    next[index] = '.'
                }
            }
        }
    }

    return area.copy(cells = next)
}

private fun step(previous: Area): Area {
    return move(move(previous, '>'), 'v')
}

private fun solve(area: Area): Int {
    return 1 + generateSequence(area) { step(it) }
        .zipWithNext()
        .indexOfFirst { (a1, a2) -> a1 == a2 }
}
