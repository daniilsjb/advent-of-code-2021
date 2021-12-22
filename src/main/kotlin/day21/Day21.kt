package day21

import java.io.File
import kotlin.math.max
import kotlin.math.min

fun main() {
    val data = parse("src/main/kotlin/day21/Day21.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 21 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

private data class GameState(
    val player1: Player,
    val player2: Player,
    val turn: Turn = Turn.PLAYER1,
)

private enum class Turn {
    PLAYER1, PLAYER2
}

private data class Player(
    val position: Int,
    val score: Long = 0L,
)

private fun parse(path: String): GameState {
    val (p1, p2) = File(path)
        .readText()
        .trim()
        .lines()
        .map(String::toPlayer)

    return GameState(p1, p2)
}

private fun String.toPlayer(): Player =
    this.split(" ")
        .last()
        .toInt()
        .run(::Player)

private fun move(player: Player, roll: Int): Player {
    val position = (player.position + roll - 1) % 10 + 1
    return Player(position, player.score + position)
}

private fun GameState.play(roll: Int): GameState =
    when (turn) {
        Turn.PLAYER1 -> copy(player1 = move(player1, roll), turn = Turn.PLAYER2)
        Turn.PLAYER2 -> copy(player2 = move(player2, roll), turn = Turn.PLAYER1)
    }

private fun part1(state: GameState): Long {
    var (dice, rolls) = 0 to 0

    fun dice(): Int =
        (((dice + 1) - 1 % 100) + 1)
            .also { roll -> dice = roll }
            .also { ++rolls }

    fun roll(): Int =
        dice() + dice() + dice()

    val score = generateSequence(state) { it.play(roll()) }
        .first { (p1, p2) -> p1.score >= 1000 || p2.score >= 1000 }
        .let { (p1, p2) -> min(p1.score, p2.score) }

    return score * rolls
}

/*
 * The key observation for this solution is that the possible rolls of the Dirac dice are always the same. Every
 * first roll is going to result in either 1, 2, or 3. For each of those outcomes, we have three more rolls like that,
 * and for each of those, we have another three. In total, there are 3^3 = 27 possible outcomes, which can be
 * summarized by this table:
 *
 * +-----------------------------------+-----------------------------------+-----------------------------------+
 * |                 1                 |                 2                 |                 3                 |
 * +-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * |     1     |     2     |     3     |     1     |     2     |     3     |     1     |     2     |     3     |
 * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
 * | 1 | 2 | 3 | 1 | 2 | 3 | 1 | 2 | 3 | 1 | 2 | 3 | 1 | 2 | 3 | 1 | 2 | 3 | 1 | 2 | 3 | 1 | 2 | 3 | 1 | 2 | 3 |
 * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
 *   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
 *   v   v   v   v   v   v   v   v   v   v   v   v   v   v   v   v   v   v   v   v   v   v   v   v   v   v   v
 * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
 * | 3 | 4 | 5 | 4 | 5 | 6 | 5 | 6 | 7 | 4 | 5 | 6 | 5 | 6 | 7 | 6 | 7 | 8 | 5 | 6 | 7 | 6 | 7 | 8 | 7 | 8 | 9 |
 * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
 *
 * Notice that all values are in range [3-9]. What's even more important is that these values repeat! For example,
 * the roll 6 appears 7 times in total. This means that there are 7 parallel universes where, at this very turn, the
 * current player has rolled a 6. The outcome for each of those universes must be the same, therefore we only have to
 * calculate the number of victories once, and simply multiply it by 7. The same logic applies to other rolls.
 */
private val Outcomes = arrayOf(
    3 to 1L,
    4 to 3L,
    5 to 6L,
    6 to 7L,
    7 to 6L,
    8 to 3L,
    9 to 1L,
)

private data class Universes(
    val player1: Long = 0L,
    val player2: Long = 0L,
)

private operator fun Universes.plus(other: Universes): Universes =
    Universes(player1 + other.player1, player2 + other.player2)

private operator fun Universes.times(scale: Long): Universes =
    Universes(player1 * scale, player2 * scale)

/*
 * The number of unique game states is limited, and they often repeat throughout calculations. Memoization helps reduce
 * unnecessary simulations of identical game sequences.
 */
private val Cache = mutableMapOf<GameState, Universes>()

private fun quantum(state: GameState): Universes {
    if (state.player1.score >= 21) {
        return Universes(player1 = 1L)
    }
    if (state.player2.score >= 21) {
        return Universes(player2 = 1L)
    }

    return Cache[state] ?: Outcomes
        .map { (roll, count) -> quantum(state.play(roll)) * count }
        .reduce(Universes::plus)
        .also { Cache[state] = it }
}

private fun part2(state: GameState): Long =
    quantum(state).let { (p1, p2) -> max(p1, p2) }