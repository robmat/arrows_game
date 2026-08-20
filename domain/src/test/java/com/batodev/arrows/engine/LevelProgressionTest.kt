package com.batodev.arrows.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class LevelProgressionTest {
    @Test
    fun testLevelProgressionRules() {
        // Test L1: Base 5x5, Step 0. Reduction 0. Expected 5x5, 5 lives.
        verifyLevel(1, expectedW = 5, expectedH = 5, expectedLives = 5)

        // Test L10: Base 10x9, Step 1. Reduction 3. Expected 7x6, 4 lives.
        verifyLevel(10, expectedW = 7, expectedH = 6, expectedLives = 4)

        // Test L11: Base 10x10, Step 1. Reduction 3. Expected 7x7, 4 lives.
        verifyLevel(11, expectedW = 7, expectedH = 7, expectedLives = 4)

        // Test L20: Base 15x14, Step 2. Reduction 6. Expected 9x8, 3 lives.
        verifyLevel(20, expectedW = 9, expectedH = 8, expectedLives = 3)

        // Test L50: Lives should be at least 1.
        verifyLevel(50, expectedW = 15, expectedH = 14, expectedLives = 1)
    }

    private fun verifyLevel(
        levelNum: Int,
        expectedW: Int,
        expectedH: Int,
        expectedLives: Int,
    ) {
        val config = LevelProgression.calculateLevelConfiguration(levelNum)

        assertEquals("Level $levelNum Width mismatch", expectedW, config.width)
        assertEquals("Level $levelNum Height mismatch", expectedH, config.height)
        assertEquals("Level $levelNum Lives mismatch", expectedLives, config.maxLives)
    }
}
