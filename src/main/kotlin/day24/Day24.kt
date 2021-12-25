package day24

import java.io.File

fun main() {
    val (a, b, c) = parse("src/main/kotlin/day24/Day24.txt")

    val answer1 = part1(a, b, c)
    val answer2 = part2(a, b, c)

    println("🎄 Day 24 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

/*
 * The trickiest part of this problem is that the ALU is just a ruse. The key
 * to efficiently solving this challenge is in analyzing the program itself,
 * not in implementing the virtual machine. This is the logic behind how this
 * solution works:
 *
 * 1) The MONAD program does some work for every one of the 14-digits we supply
 * to it. If we look into the program's code and look at each block starting
 * with instruction "inp w", we can observe that the blocks are similar, the
 * only difference between them being 3 constants per block.
 *
 * 2) To better understand what MONAD does, we can decompile the program to a
 * higher-level language, or even pseudo-code. For example, I converted every
 * instruction into a format like this:
 *
 *     add x 25 -> x := x + 25
 *     mul y z  -> y := y * z
 *     eql x w  -> x := 1 if x == w else 0
 *
 * At this point it's easy to merge some operations together and see what
 * each block is really trying to achieve. Ultimately, this is what the logic
 * for each digit looks like, in pseudo-code:
 *
 *     w := read()
 *     if (z % 26 + b) == w:
 *         z := z / a
 *     else:
 *         z := z / a * 26 + w + c
 *
 * where a, b, and c are some constants.
 *
 * 3) Now we can analyze the constants to gain some additional insight. It
 * turns out that 'a' can ever only be 1 or 26, abs(b) is always below 26, and
 * 'c' is always below 26. The number 26 seems to play the role of a base;
 * register z may be seen as a base-26 number with those operations:
 *
 *     z := z * 26 + (w + c)    ; append a base-26 digit to the end
 *     z := z / 26              ; remove the last base-26 digit
 *
 * Taking the above into account, we can model register z as a stack, where
 * each cell contains some base-26 digit, and we end up with the following two
 * cases:
 *
 *     [a = 1]
 *     w := read()
 *     if (z % 26 + b) == w:
 *         z := z                       ; no-op
 *     else:
 *         z := z * 26 + w + c          ; push digit (w + c) onto z
 *
 *     [a = 26]
 *     w := read()
 *     if (z % 26 + b) == w:
 *         z := z / 26                  ; pop digit from z
 *     else:
 *         z := (z / 26) * 26 + w + c   ; replace last digit of z with (w + c)
 *
 * 4) The key insight here is that we can push to z when 'a' is 1 and pop from
 * z when 'a' is 26. It turns out that each of those values appear exactly 7
 * times, meaning there are as many pushes as there are pops. Now the problem
 * really reduces to ensuring that the pushes and pops are balanced, such that
 * we start and end with an empty stack (i.e. 'z' equalling 0).
 *
 * 5) Whenever 'a' is 1, we want to execute the second condition, and whenever
 * 'a' is 26, we want to execute the first condition. We also need to ensure
 * that the digit we're adding and removing is the same. Therefore:
 *
 *     w1 + c1 = w2 - b2
 *
 * where subscript 1 refers to the constants at pushing and 2 refers to the
 * constants at popping.
 *
 * So, if we can determine the values of w1 and w2, then we know what digits
 * must be placed at these two positions of the model number. Note that, when
 * 'a' is 1, 'b' is always greater than 10, which means that the second
 * condition is always executed.
 *
 * To solve for those two variables, on each pop we can simply try every
 * combination of digits from 9-1 or 1-9 (depending on whether we need the
 * largest or the smallest model number) until the condition above holds.
 */

private fun parse(path: String): Triple<List<Long>, List<Long>, List<Long>> {
    val program = File(path)
        .readText()
        .lines()

    val a = mutableListOf<Long>()
    val b = mutableListOf<Long>()
    val c = mutableListOf<Long>()

    var offset = 0
    repeat(14) {
        a.add(program[offset + 4].split(" ").last().toLong())
        b.add(program[offset + 5].split(" ").last().toLong())
        c.add(program[offset + 15].split(" ").last().toLong())
        offset += 18
    }

    return Triple(a, b, c)
}

private fun part1(a: List<Long>, b: List<Long>, c: List<Long>): String {
    val digits = Array(14) { 0 }
    val stack = ArrayDeque<Int>()

    outer@
    for (i1 in 0..13) {
        if (a[i1] == 1L) {
            stack.addLast(i1)
        } else {
            val i0 = stack.removeLast()
            for (w1 in 9 downTo 1) {
                for (w2 in 9 downTo 1) {
                    if (w1 + c[i0] == w2 - b[i1]) {
                        digits[i0] = w1
                        digits[i1] = w2
                        continue@outer
                    }
                }
            }
        }
    }

    return digits.joinToString(separator = "")
}

private fun part2(a: List<Long>, b: List<Long>, c: List<Long>): String {
    val digits = Array(14) { 0 }
    val stack = ArrayDeque<Int>()

    outer@
    for (i1 in 0..13) {
        if (a[i1] == 1L) {
            stack.addLast(i1)
        } else {
            val i0 = stack.removeLast()
            for (w1 in 1..9) {
                for (w2 in 1..9) {
                    if (w1 + c[i0] == w2 - b[i1]) {
                        digits[i0] = w1
                        digits[i1] = w2
                        continue@outer
                    }
                }
            }
        }
    }

    return digits.joinToString(separator = "")
}
