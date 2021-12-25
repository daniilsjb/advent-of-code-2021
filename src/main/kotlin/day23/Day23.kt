package day23

import java.io.File
import java.lang.StringBuilder
import java.util.*
import kotlin.math.abs

fun main() {
    val data = parse("src/main/kotlin/day23/Day23.txt")

    val answer1 = part1(data)
    val answer2 = part2(data)

    println("🎄 Day 23 🎄")

    println()

    println("[Part 1]")
    println("Answer: $answer1")

    println()

    println("[Part 2]")
    println("Answer: $answer2")
}

/*
 *            Hallway        |               Rooms
 * --------------------------+----------------------------------------------
 *
 *     . . . . . . . . . . . | A B C D A B C D                      (Part 1)
 *
 *     . . . . . . . . . . . | A B C D A B C D A B C D A B C D      (Part 2)
 *                             ^~~~~~~~^
 *                             Stride: 4
 */

private typealias Burrow = String
private typealias Amphipod = Char

private val Corridors = arrayOf(0, 1, 3, 5, 7, 9, 10)

private val Amphipod.index: Int
    get() = this - 'A'

private val Doors = arrayOf(2, 4, 6, 8)
private val Amphipod.door: Int
    get() = Doors[index]

private const val Stride = 4
private val Rooms = arrayOf(11, 12, 13, 14)
private val Amphipod.room: Int
    get() = Rooms[index]

private val Energies = arrayOf(1, 10, 100, 1000)
private val Amphipod.energy: Int
    get() = Energies[index]

private data class Step(
    val burrow: Burrow,
    val energy: Int,
)

private fun String.swap(a: Int, b: Int): String =
    StringBuilder(this)
        .apply { setCharAt(a, this@swap[b]) }
        .apply { setCharAt(b, this@swap[a]) }
        .toString()

// Returns the index of the next available cell in the amphipod's corresponding
// room if it is not occupied by amphipods of the wrong type, otherwise returns null.
private fun Amphipod.nextRoomPosition(burrow: Burrow, roomSize: Int): Int? {
    val cells = (0 until roomSize).map { i -> burrow[room + Stride * i] }

    return if (cells.all { it == '.' || it == this }) {
        cells.indexOfLast { it == '.' }
    } else {
        null
    }
}

private fun Burrow.hasPath(source: Int, target: Int): Boolean {
    // Source must be excluded from the path because it is always occupied
    val path = if (target > source) {
        target downTo source + 1
    } else {
        source - 1 downTo target
    }

    return path.all { i -> this[i] == '.' }
}

private fun movesFromHallway(burrow: Burrow, roomSize: Int): List<Step> =
    Corridors.mapNotNull { corridor ->
        // Consider only cells taken up by some amphipods
        val amphipod = burrow[corridor]
        if (amphipod == '.') {
            return@mapNotNull null
        }

        // If the target room of this amphipod is unavailable, then it has no
        // possible moves in this configuration.
        val nextRoomPosition = amphipod.nextRoomPosition(burrow, roomSize)
            ?: return@mapNotNull null

        // Now we have to make sure that the room is reachable by finding a
        // direct path from amphipod's position to the door.
        val source: Int = corridor
        val target: Int = amphipod.door
        if (!burrow.hasPath(source, target)) {
            return@mapNotNull null
        }

        // Find the absolute index of the amphipod's new position with the room
        val absolutePosition = amphipod.room + Stride * nextRoomPosition
        val configuration = burrow.swap(source, absolutePosition)

        // Find the total amount of energy consumed by this move
        val distanceWithinHallway = abs(target - source)
        val distanceWithinRoom = nextRoomPosition + 1
        val totalEnergy = (distanceWithinHallway + distanceWithinRoom) * amphipod.energy

        // Combine all the above into a single step
        Step(configuration, totalEnergy)
    }

private fun movesFromRooms(burrow: Burrow, roomSize: Int): List<Step> =
    Rooms.zip(Doors).flatMap { (room, door) ->
        // Collect every amphipod inside this room
        val cells = (0 until roomSize).map { i -> burrow[room + Stride * i] }

        // If the room is empty, no moves can be made from there
        if (cells.all { it == '.' }) {
            return@flatMap emptyList()
        }

        // Now we may choose the top-most amphipod in this room
        val nextPosition = cells.indexOfFirst { it != '.' }
        val nextAmphipod = burrow[room + Stride * nextPosition]

        // Try building paths from that amphipod to every corridor cell
        Corridors.mapNotNull { corridor ->
            if (burrow[corridor] == '.' && burrow.hasPath(door, corridor)) {
                val absolutePosition = room + Stride * nextPosition
                val configuration = burrow.swap(absolutePosition, corridor)

                val distanceWithinHallway = abs(door - corridor)
                val distanceWithinRoom = nextPosition + 1
                val totalEnergy = (distanceWithinHallway + distanceWithinRoom) * nextAmphipod.energy

                Step(configuration, totalEnergy)
            } else {
                null
            }
        }
    }

private fun moves(burrow: Burrow, roomSize: Int): List<Step> =
    movesFromHallway(burrow, roomSize) + movesFromRooms(burrow, roomSize)

private fun organize(start: Burrow, target: Burrow, roomSize: Int): Int {
    val distances = mutableMapOf(start to 0)

    val frontier = PriorityQueue(Comparator.comparing(Step::energy))
        .apply { add(Step(start, 0)) }

    while (frontier.isNotEmpty()) {
        val (current, energy) = frontier.poll()
        if (current == target) {
            break
        }

        if (energy > distances.getValue(current)) {
            continue
        }

        for (next in moves(current, roomSize)) {
            val step = Step(next.burrow, energy + next.energy)

            if (step.energy < distances.getOrDefault(step.burrow, Int.MAX_VALUE)) {
                distances[step.burrow] = step.energy
                frontier.add(step)
            }
        }
    }

    return distances.getValue(target)
}

private fun parse(path: String): Burrow =
    File(path)
        .readText()
        .filter(Char::isLetter)
        .let { "...........${it}" }

private fun part1(burrow: Burrow): Int {
    val target = "...........ABCDABCD"
    return organize(burrow, target, roomSize = 2)
}

private fun part2(burrow: Burrow): Int {
    val upper = burrow.substring(0, 15)
    val lower = burrow.substring(15, 19)
    val combined = "${upper}DCBADBAC${lower}"

    val target = "...........ABCDABCDABCDABCD"
    return organize(combined, target, roomSize = 4)
}
