package day04

import java.io.File

fun main() {
    val (numbers, boards) = parse("src/main/kotlin/day04/Day04.txt")

    val answer1 = part1(numbers, boards.map(List<Int>::toMutableList))
    val answer2 = part2(numbers, boards.map(List<Int>::toMutableList))

    println("🎄 Day 04 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

fun parse(path: String): Pair<List<Int>, List<List<Int>>> {
    val entries = File(path)
        .readText()
        .trim()
        .split("""\s+""".toRegex())

    val numbers = entries.first()
        .split(",")
        .map(String::toInt)

    val boards = entries.subList(1, entries.size)
        .map(String::toInt)
        .chunked(25)

    return Pair(numbers, boards)
}

// We need to keep track of each board's state as the game progresses.
// The simplest and most effective approach would be to mutate the board itself.
typealias Board = MutableList<Int>

// Boards have a sense of dimensionality, but all numbers are stored as flat lists.
// This structure allows us to fully describe a given number's position.
data class Position(
    val col: Int,
    val row: Int,
    val idx: Int,
)

// It is important to remember that a number is not guaranteed to appear on every
// board, therefore it may not always have a position.
fun Board.findPosition(number: Int): Position? =
    (0 until 5).firstNotNullOfOrNull { row ->
        (0 until 5).firstNotNullOfOrNull { col ->
            val index = row * 5 + col
            if (this[index] == number)
                Position(col, row, index)
            else
                null
        }
    }

// There are several approaches we could use to signify that a given number was
// marked, here's one of them: all numbers that appear on boards are always positive;
// therefore, we can use the sign of the number as the "marked" flag.
//
// Since a marked number is never needed again, we are free to discard it during
// the calculations and replace it with something completely different. This may
// not be the cleanest approach, but it lets us reuse space efficiently, avoid
// introducing extra data classes, etc.
fun Board.mark(idx: Int) {
    this[idx] = -1
}

// Naturally, the board may be put into a winning position only after a number
// was marked, and the number can only affect one row and one column. It's enough
// to just check these two, without bothering the rest of the board.
fun Board.hasWon(col: Int, row: Int) =
    (0 until 5).all { i -> this[row * 5 + i] < 0 } ||
    (0 until 5).all { i -> this[i * 5 + col] < 0 }

// For the first part of the problem, the algorithm is straightforward:
// we mark each number, in turn, on each board, until we encounter a winning
// position, in which case the result is directly returned.

fun part1(numbers: List<Int>, boards: List<Board>) =
    numbers.firstNotNullOf { number ->
        boards.firstNotNullOfOrNull { board ->
            board.findPosition(number)?.let { (col, row, idx) ->
                board.mark(idx)
                if (board.hasWon(col, row))
                    number * board.sumOf { i -> if (i >= 0) i else 0 }
                else
                    null
            }
        }
    }

// For the second part, the approach is a little more involved: we go through
// each board and find the first winning turn (each board is guaranteed to win
// at some point); we then find the victory that happened on the latest turn
// and use it to calculate the final result.

data class Victory(
    val turn: Int,
    val number: Int,
    val board: Board
)

fun part2(numbers: List<Int>, boards: List<Board>) =
    boards.map { board ->
        numbers.indexOfFirst { number ->
            board.findPosition(number)?.let { (col, row, idx) ->
                board.mark(idx)
                board.hasWon(col, row)
            } ?: false
        }
        .let { turn -> Victory(turn, numbers[turn], board) }
    }
    .maxByOrNull(Victory::turn)!!
    .let { (_, number, board) ->
        number * board.sumOf { i -> if (i >= 0) i else 0 }
    }