package com.batodev.arrows.engine

object SolvabilityChecker {
    private const val SOLVABILITY_ITERATION_MARGIN = 10

    fun isResolvable(level: GameLevel): Boolean {
        val grid = createGrid(level)
        val snakeMap = level.snakes.associateBy { it.id }
        val remaining = snakeMap.keys.toMutableSet()
        val maxIter = level.snakes.size + SOLVABILITY_ITERATION_MARGIN
        var iter = 0

        while (remaining.isNotEmpty() && iter < maxIter) {
            iter++
            val removable =
                remaining.firstOrNull { sId ->
                    val s = snakeMap[sId]!!
                    hasCleanLoS(LoSParams(s.body.first(), s.headDirection, sId, grid, level.width, level.height))
                } ?: return false

            snakeMap[removable]!!.body.forEach { grid[it.x][it.y] = 0 }
            remaining.remove(removable)
        }
        return remaining.isEmpty()
    }

    fun findRemovableSnake(level: GameLevel): Int? {
        val grid = createGrid(level)
        return level.snakes
            .firstOrNull { s ->
                hasCleanLoS(LoSParams(s.body.first(), s.headDirection, s.id, grid, level.width, level.height))
            }?.id
    }

    fun isLineOfSightObstructed(
        level: GameLevel,
        snake: Snake,
        ignoreIds: Set<Int> = emptySet(),
    ): Boolean {
        val head = snake.body.first()
        val direction = snake.headDirection
        var current = head + direction

        while (isInside(current, level.width, level.height)) {
            val isOccupied =
                level.snakes.any { other ->
                    other.id !in ignoreIds && other.body.contains(current)
                }
            if (isOccupied) return true
            current += direction
        }
        return false
    }

    private fun createGrid(level: GameLevel): Array<IntArray> {
        val grid = Array(level.width) { IntArray(level.height) }
        level.snakes.forEach { s -> s.body.forEach { p -> grid[p.x][p.y] = s.id } }
        return grid
    }

    private fun hasCleanLoS(params: LoSParams): Boolean {
        var curr = params.head + params.dir
        while (isInside(curr, params.w, params.h)) {
            if (params.grid[curr.x][curr.y] != 0 && params.grid[curr.x][curr.y] != params.sId) return false
            curr += params.dir
        }
        return true
    }

    private fun isInside(
        p: Point,
        w: Int,
        h: Int,
    ) = p.x in 0 until w && p.y in 0 until h
}
