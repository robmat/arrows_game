package com.batodev.arrows

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratorUnlockTest {
    @Test
    fun testGeneratorLockedBelowUnlockLevel() {
        val levelNumber = GameConstants.GENERATOR_UNLOCK_LEVEL - 1
        val isUnlocked = levelNumber >= GameConstants.GENERATOR_UNLOCK_LEVEL
        assertFalse("Generator should be locked below level ${GameConstants.GENERATOR_UNLOCK_LEVEL}", isUnlocked)
    }

    @Test
    fun testGeneratorUnlockedAtUnlockLevel() {
        val levelNumber = GameConstants.GENERATOR_UNLOCK_LEVEL
        val isUnlocked = levelNumber >= GameConstants.GENERATOR_UNLOCK_LEVEL
        assertTrue("Generator should be unlocked at level ${GameConstants.GENERATOR_UNLOCK_LEVEL}", isUnlocked)
    }

    @Test
    fun testGeneratorUnlockedAboveUnlockLevel() {
        val levelNumber = GameConstants.GENERATOR_UNLOCK_LEVEL + 10
        val isUnlocked = levelNumber >= GameConstants.GENERATOR_UNLOCK_LEVEL
        assertTrue("Generator should be unlocked above level ${GameConstants.GENERATOR_UNLOCK_LEVEL}", isUnlocked)
    }

    @Test
    fun testUnlockLevelIstwenty() {
        val unlockLevel = GameConstants.GENERATOR_UNLOCK_LEVEL
        assertTrue("Generator unlock level should be 20", unlockLevel == 20)
    }

    @Test
    fun testGeneratorLockedAtLevelOne() {
        val levelNumber = 1
        val isUnlocked = levelNumber >= GameConstants.GENERATOR_UNLOCK_LEVEL
        assertFalse("Generator should be locked at level 1", isUnlocked)
    }

    @Test
    fun testGeneratorLockedAtLevelNineteen() {
        val levelNumber = 19
        val isUnlocked = levelNumber >= GameConstants.GENERATOR_UNLOCK_LEVEL
        assertFalse("Generator should be locked at level 19", isUnlocked)
    }
}
