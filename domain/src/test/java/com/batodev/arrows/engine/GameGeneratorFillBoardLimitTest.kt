package com.batodev.arrows.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameGeneratorFillBoardLimitTest {
    @Test
    fun testFillBoardLimitsDimensionsTo35() {
        val generator = GameGenerator()
        val params =
            GenerationParams(
                width = 50,
                height = 50,
                maxSnakeLength = 7,
                fillTheBoard = true,
            )

        val level = generator.generateSolvableLevel(params)

        assertEquals("Width should be limited to 35", 35, level.width)
        assertEquals("Height should be limited to 35", 35, level.height)
    }

    @Test
    fun testFillBoardAllowsSmallerDimensions() {
        val generator = GameGenerator()
        val params =
            GenerationParams(
                width = 20,
                height = 25,
                maxSnakeLength = 7,
                fillTheBoard = true,
            )

        val level = generator.generateSolvableLevel(params)

        assertEquals("Width should remain 20", 20, level.width)
        assertEquals("Height should remain 25", 25, level.height)
    }

    @Test
    fun testNonFillBoardAllowsLargerDimensions() {
        val generator = GameGenerator()
        val params =
            GenerationParams(
                width = 50,
                height = 50,
                maxSnakeLength = 7,
                fillTheBoard = false,
            )

        val level = generator.generateSolvableLevel(params)

        assertEquals("Width should remain 50", 50, level.width)
        assertEquals("Height should remain 50", 50, level.height)
    }

    @Test
    fun testFillBoardLimitsOnlyExceedingDimension() {
        val generator = GameGenerator()
        val params =
            GenerationParams(
                width = 40,
                height = 20,
                maxSnakeLength = 7,
                fillTheBoard = true,
            )

        val level = generator.generateSolvableLevel(params)

        assertEquals("Width should be limited to 35", 35, level.width)
        assertEquals("Height should remain 20", 20, level.height)
    }

    @Test
    fun testFillBoardAt35x35ProducesSolvableLevel() {
        val generator = GameGenerator()
        val params =
            GenerationParams(
                width = 35,
                height = 35,
                maxSnakeLength = 7,
                fillTheBoard = true,
            )

        val level = generator.generateSolvableLevel(params)

        assertTrue("Level should be solvable", SolvabilityChecker.isResolvable(level))
        assertEquals("Board should be fully filled", 35 * 35, level.snakes.sumOf { it.body.size })
    }

    @Test
    fun testFillBoardWithLimitedSizeProducesSolvableLevel() {
        val generator = GameGenerator()
        val params =
            GenerationParams(
                width = 100,
                height = 100,
                maxSnakeLength = 7,
                fillTheBoard = true,
            )

        val level = generator.generateSolvableLevel(params)

        assertTrue("Level should be solvable", SolvabilityChecker.isResolvable(level))
        assertEquals("Width should be limited", 35, level.width)
        assertEquals("Height should be limited", 35, level.height)
    }
}
