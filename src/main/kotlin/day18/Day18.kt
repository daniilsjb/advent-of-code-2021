package day18

import java.io.File
import java.lang.Integer.max

fun main() {
    val data = parse("src/main/kotlin/day18/Day18.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 18 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

/*
 * AST
 */

sealed interface Element {
    @JvmInline
    value class Number(val value: Int) : Element {
        override fun toString() = value.toString()
    }

    data class Pair(val lhs: Element, val rhs: Element) : Element {
        override fun toString() = "[$lhs,$rhs]"
    }
}

/*
 * Parsing
 */

fun parse(path: String): List<Element> =
    File(path)
        .readLines()
        .map(String::toElement)

fun String.toElement(): Element =
    ParserState(source = this).element()

data class ParserState(
    val source: String,
    var current: Int = 0,
)

/* Parsing Primitives */

fun ParserState.peek(): Char =
    source[current]

fun ParserState.next(): Char =
    source[current++]

/* Syntax Rules */

/*
 * element ::= pair | number
 */
fun ParserState.element(): Element {
    return if (peek() == '[') {
        pair()
    } else {
        number()
    }
}

/*
 * pair ::= '[' element ',' element ']'
 */
fun ParserState.pair(): Element {
    next() // skip [
    val lhs = element()
    next() // skip ,
    val rhs = element()
    next() // skip ]
    return Element.Pair(lhs, rhs)
}

/*
 * number ::= [0-9]+
 */
fun ParserState.number(): Element {
    var result = 0
    while (peek().isDigit()) {
        result = result * 10 + next().digitToInt()
    }
    return Element.Number(result)
}

/*
 * Reduction
 */

/*
 * Every reduction attempt may either succeed and yield a new simplified tree,
 * or fail in case the tree could not be simplified any further.
 */
sealed interface Reduction {
    @JvmInline
    value class Success(val value: Element) : Reduction

    object Failure : Reduction
}

/*
 * Fully reduces an element by repeatedly performing reduction steps until
 * there is nothing more to reduce. At the point when no further reduction
 * may happen, this function returns the original element.
 */
tailrec fun reduce(element: Element): Element =
    when (val result = step(element)) {
        is Reduction.Success -> reduce(result.value)
        is Reduction.Failure -> element
    }

/*
 * Attempts to perform a single reduction step. According to the rules,
 * exploding has higher priority than splitting, so each action is tried
 * in order until one of them succeeds.
 */
fun step(element: Element): Reduction {
    val actions = arrayOf(::explode, ::split)
    for (action in actions) {
        val result = action(element)
        if (result is Reduction.Success) {
            return result
        }
    }

    return Reduction.Failure
}

/*
 * Exploding
 */

/*
 * Attempts to reduce the tree by exploding its leftmost pair nested four
 * levels deep. If no such pair exists, returns a failure.
 */
fun explode(element: Element): Reduction =
    explodeAt(element, depth = 0).let { explosion ->
        when (explosion) {
            is Explosion.Success -> Reduction.Success(explosion.result)
            is Explosion.Failure -> Reduction.Failure
        }
    }

/*
 * Explosions are not straightforward reductions because they require the
 * context of the entire tree: the numbers stored in the "exploded" pair
 * must be added to the numbers that are immediately to the left and right,
 * which will be contained by other branches.
 *
 * Since tree elements do not have references to their parents, this addition
 * cannot happen directly. Instead, explosions are handled recursively, with
 * each parent element making calls to its children. This means that, in order
 * to access an element's parent, we need to return to the previous call frame.
 *
 * Therefore, all the information necessary to perform an explosion has to
 * propagate up the call stack, and it is then the callers' responsibility to
 * "finish" the work. This type is used to encode an explosion in-progress.
 */
sealed interface Explosion {
    data class Success(
        val result: Element,
        val delta: Delta,
    ) : Explosion

    object Failure : Explosion
}

/*
 * The explosion's delta is the pair of numbers that should be added to the
 * numbers on the left and right from the explosion point. Because this process
 * happens gradually as we move up the call stack, we need a way to signify
 * that either side of the delta has already been handled, which is why they
 * are nullable.
 */
data class Delta(
    val lhs: Int?,
    val rhs: Int?,
)

/*
 * Attempts to explode an element at some depth.
 */
fun explodeAt(element: Element, depth: Int): Explosion =
    when (element) {
        is Element.Pair -> explodeAt(element, depth)
        is Element.Number -> explodeAt(element, depth)
    }

fun explodeAt(element: Element.Pair, depth: Int): Explosion {
    if (depth == 4) {
        return Explosion.Success(Element.Number(0), element.toDelta())
    }

    explodeAt(element.lhs, depth + 1).let { explosion ->
        if (explosion !is Explosion.Success) {
            return@let
        }

        val (result, delta) = explosion
        val right = when (delta.rhs) {
            null -> element.rhs
            else -> element.rhs.addToLeftmost(delta.rhs)
        }

        val tree = Element.Pair(result, right)
        return Explosion.Success(tree, Delta(delta.lhs, null))
    }

    explodeAt(element.rhs, depth + 1).let { explosion ->
        if (explosion !is Explosion.Success) {
            return@let
        }

        val (result, delta) = explosion
        val left = when (delta.lhs) {
            null -> element.lhs
            else -> element.lhs.addToRightmost(delta.lhs)
        }

        val tree = Element.Pair(left, result)
        return Explosion.Success(tree, Delta(null, delta.rhs))
    }

    return Explosion.Failure
}

/*
 * A number may never explode, therefore this is a no-op that always fails.
 */
fun explodeAt(element: Element.Number, depth: Int): Explosion =
    Explosion.Failure

fun Element.Pair.toDelta(): Delta {
    val lhs = (lhs as Element.Number).value
    val rhs = (rhs as Element.Number).value
    return Delta(lhs, rhs)
}

fun Element.addToLeftmost(delta: Int): Element =
    when (this) {
        is Element.Number -> Element.Number(value + delta)
        is Element.Pair -> copy(lhs = lhs.addToLeftmost(delta))
    }

fun Element.addToRightmost(delta: Int): Element =
    when (this) {
        is Element.Number -> Element.Number(value + delta)
        is Element.Pair -> copy(rhs = rhs.addToRightmost(delta))
    }

/*
 * Splitting
 */

/*
 * Attempts to reduce the tree by splitting its leftmost number that is greater
 * than 9. If no such number exists, returns a failure.
 */
fun split(element: Element): Reduction =
    when (element) {
        is Element.Pair -> split(element)
        is Element.Number -> split(element)
    }

/*
 * Attempts to split each branch of a pair, starting with the leftmost and
 * terminating on first success.
 */
fun split(element: Element.Pair): Reduction {
    split(element.lhs).let { result ->
        if (result is Reduction.Success) {
            return@split Reduction.Success(element.copy(lhs = result.value))
        }
    }

    split(element.rhs).let { result ->
        if (result is Reduction.Success) {
            return@split Reduction.Success(element.copy(rhs = result.value))
        }
    }

    return Reduction.Failure
}

/*
 * Attempts to split a number if it meets the reduction criterion.
 */
fun split(element: Element.Number): Reduction =
    if (element.value > 9) {
        val lhs = Element.Number((element.value + 0) / 2)
        val rhs = Element.Number((element.value + 1) / 2)
        Reduction.Success(Element.Pair(lhs, rhs))
    } else {
        Reduction.Failure
    }

/*
 * Evaluation
 */

operator fun Element.plus(other: Element): Element =
    Element.Pair(this, other).run(::reduce)

fun magnitude(element: Element): Int =
    when (element) {
        is Element.Pair -> magnitude(element.lhs) * 3 + magnitude(element.rhs) * 2
        is Element.Number -> element.value
    }

fun part1(elements: List<Element>) =
    elements
        .reduce(Element::plus)
        .run(::magnitude)

fun part2(elements: List<Element>): Int {
    var max = 0
    for ((i, x) in elements.withIndex()) {
        for ((j, y) in elements.withIndex()) {
            if (i == j) {
                continue
            }

            val m1 = magnitude(x + y)
            val m2 = magnitude(y + x)

            val current = max(m1, m2)
            if (current > max) {
                max = current
            }
        }
    }
    return max
}
