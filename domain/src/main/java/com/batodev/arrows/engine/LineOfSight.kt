package com.batodev.arrows.engine

/**
 * Line-of-sight test for tap handling, which runs on the main thread inside input dispatch.
 *
 * Every cell occupied by a snake outside [ignoreIds] blocks the ray - including the tested snake's
 * own body. The occupancy grid is built once, on construction, so each test is a walk along the
 * ray. Scanning every snake's body for every ray cell instead, for every snake on the board, was
 * the top "Input dispatching timed out" ANR in Play Console on large boards.
 */
class LineOfSight(
    private val level: GameLevel,
    ignoreIds: Set<Int> = emptySet(),
) {
    private val occupied = BooleanArray(level.width * level.height)

    init {
        level.snakes
            .filter { it.id !in ignoreIds }
            .forEach { snake ->
                snake.body.filter(::isInside).forEach { occupied[it.y * level.width + it.x] = true }
            }
    }

    fun isObstructed(snake: Snake): Boolean {
        var current = snake.body.first() + snake.headDirection
        while (isInside(current)) {
            if (occupied[current.y * level.width + current.x]) return true
            current += snake.headDirection
        }
        return false
    }

    private fun isInside(p: Point) = p.x in 0 until level.width && p.y in 0 until level.height
}
