package com.batodev.arrows.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LineOfSightTest {
    @Test
    fun testMatchesFullBodyScanOnGeneratedLevels() {
        val generator = GameGenerator()
        val random = Random(42)
        val boards =
            listOf(
                GenerationParams(8, 8, 4),
                GenerationParams(25, 24, 6),
                GenerationParams(45, 44, 8),
                GenerationParams(20, 20, 7, fillTheBoard = true),
            )

        boards.forEach { params ->
            val level = generator.generateSolvableLevel(params)
            val ignoreSets =
                listOf(
                    emptySet(),
                    level.snakes
                        .filter { random.nextInt(10) == 0 }
                        .map { it.id }
                        .toSet(),
                )
            ignoreSets.forEach { ignoreIds ->
                val lineOfSight = LineOfSight(level, ignoreIds)
                level.snakes.forEach { snake ->
                    assertEquals(
                        "Snake ${snake.id} on ${level.width}x${level.height}, ignoring $ignoreIds",
                        scanEveryBody(level, snake, ignoreIds),
                        lineOfSight.isObstructed(snake),
                    )
                }
            }
        }
    }

    @Test
    fun testOwnBodyAheadOfHeadBlocks() {
        // Head at (1,1) pointing down, into its own body at (1,2).
        val snake = Snake(1, listOf(Point(1, 1), Point(0, 1), Point(0, 2), Point(1, 2)), Direction.DOWN)
        val level = GameLevel(5, 5, listOf(snake))

        assertTrue(LineOfSight(level).isObstructed(snake))
        assertFalse(LineOfSight(level, ignoreIds = setOf(1)).isObstructed(snake))
    }

    @Test
    fun testIgnoredSnakesDoNotBlock() {
        val s1 = Snake(1, listOf(Point(1, 1)), Direction.RIGHT)
        val s2 = Snake(2, listOf(Point(3, 1)), Direction.RIGHT)
        val level = GameLevel(5, 5, listOf(s1, s2))

        assertTrue(LineOfSight(level).isObstructed(s1))
        assertFalse(LineOfSight(level, ignoreIds = setOf(2)).isObstructed(s1))
        assertFalse(LineOfSight(level).isObstructed(s2))
    }

    @Test
    fun testBodyPointsOutsideTheBoardAreIgnored() {
        val snake = Snake(1, listOf(Point(0, 0), Point(-1, 0)), Direction.RIGHT)
        val level = GameLevel(3, 3, listOf(snake))

        assertFalse(LineOfSight(level).isObstructed(snake))
    }

    // The per-cell scan LineOfSight replaced: every snake's body, checked for every ray cell.
    private fun scanEveryBody(
        level: GameLevel,
        snake: Snake,
        ignoreIds: Set<Int>,
    ): Boolean {
        var current = snake.body.first() + snake.headDirection
        while (current.x in 0 until level.width && current.y in 0 until level.height) {
            if (level.snakes.any { it.id !in ignoreIds && current in it.body }) return true
            current += snake.headDirection
        }
        return false
    }
}
