package com.batodev.arrows.engine

object GenerationUtils {
    fun isInside(
        p: Point,
        w: Int,
        h: Int,
    ) = p.x in 0 until w && p.y in 0 until h

    fun forbiddenPoints(
        head: Point,
        dir: Direction,
        w: Int,
        h: Int,
    ): Set<Point> {
        val points = mutableSetOf<Point>()
        var current = head + dir
        while (isInside(current, w, h)) {
            points.add(current)
            current += dir
        }
        return points
    }

    fun hasClearLoS(
        start: Point,
        dir: Direction,
        occupied: Array<BooleanArray>,
        w: Int,
        h: Int,
    ): Boolean {
        var current = start + dir
        while (isInside(current, w, h)) {
            if (occupied[current.x][current.y]) return false
            current += dir
        }
        return true
    }

    fun countValidCells(
        width: Int,
        height: Int,
        walls: Array<BooleanArray>,
    ): Int {
        var count = 0
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (!walls[x][y]) count++
            }
        }
        return count
    }

    fun isFreeAt(
        p: Point,
        occupied: Array<BooleanArray>,
        config: GameGeneratorConfig,
    ): Boolean =
        isInside(p, config.width, config.height) &&
            !occupied[p.x][p.y] && !config.walls[p.x][p.y]

    fun getOrderedDirections(
        possible: List<Direction>,
        prevDir: Direction?,
        straightPreference: Float,
        rnd: kotlin.random.Random,
    ): List<Direction> {
        if (prevDir == null || straightPreference <= 0f) return possible
        val shouldGoStraight = possible.contains(prevDir) && rnd.nextFloat() < straightPreference
        return if (shouldGoStraight) {
            listOf(prevDir) + (possible - prevDir)
        } else {
            possible
        }
    }
}
