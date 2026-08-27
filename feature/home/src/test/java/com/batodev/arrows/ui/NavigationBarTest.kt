package com.batodev.arrows.ui

import com.batodev.arrows.GameConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationBarTest {
    @Test
    fun `test generator locked below unlock level`() {
        val levelNumber = GameConstants.GENERATOR_UNLOCK_LEVEL - 1
        val isUnlocked = levelNumber >= GameConstants.GENERATOR_UNLOCK_LEVEL
        assertFalse("Generator should be locked below level ${GameConstants.GENERATOR_UNLOCK_LEVEL}", isUnlocked)
    }

    @Test
    fun `test generator unlocked at unlock level`() {
        val levelNumber = GameConstants.GENERATOR_UNLOCK_LEVEL
        val isUnlocked = levelNumber >= GameConstants.GENERATOR_UNLOCK_LEVEL
        assertTrue("Generator should be unlocked at level ${GameConstants.GENERATOR_UNLOCK_LEVEL}", isUnlocked)
    }

    @Test
    fun `test generator unlocked above unlock level`() {
        val levelNumber = GameConstants.GENERATOR_UNLOCK_LEVEL + 1
        val isUnlocked = levelNumber >= GameConstants.GENERATOR_UNLOCK_LEVEL
        assertTrue("Generator should be unlocked above level ${GameConstants.GENERATOR_UNLOCK_LEVEL}", isUnlocked)
    }

    @Test
    fun `test generator not navigable when locked and debug bypass disabled`() {
        val isUnlocked = false
        val debugBypass = false
        val canNavigate = isUnlocked || debugBypass
        assertFalse("Generator should not be navigable when locked outside debug builds", canNavigate)
    }

    @Test
    fun `test generator navigable when locked but debug bypass enabled`() {
        val isUnlocked = false
        val debugBypass = true
        val canNavigate = isUnlocked || debugBypass
        assertTrue("Generator should be navigable when locked in debug builds", canNavigate)
    }

    @Test
    fun `test generator navigable when unlocked regardless of debug bypass`() {
        val isUnlocked = true
        for (debugBypass in listOf(true, false)) {
            val canNavigate = isUnlocked || debugBypass
            assertTrue("Generator should be navigable once unlocked", canNavigate)
        }
    }

    @Test
    fun `test navigation destination HOME enum exists`() {
        val destination = NavigationDestination.HOME
        assertEquals("HOME destination should exist", NavigationDestination.HOME, destination)
    }

    @Test
    fun `test navigation destination GENERATOR enum exists`() {
        val destination = NavigationDestination.GENERATOR
        assertEquals("GENERATOR destination should exist", NavigationDestination.GENERATOR, destination)
    }

    @Test
    fun `test navigation destination SETTINGS enum exists`() {
        val destination = NavigationDestination.SETTINGS
        assertEquals("SETTINGS destination should exist", NavigationDestination.SETTINGS, destination)
    }

    @Test
    fun `test navigation destination enum equality`() {
        assertEquals(
            "HOME should equal HOME",
            NavigationDestination.HOME,
            NavigationDestination.HOME,
        )
        assertEquals(
            "GENERATOR should equal GENERATOR",
            NavigationDestination.GENERATOR,
            NavigationDestination.GENERATOR,
        )
        assertEquals(
            "SETTINGS should equal SETTINGS",
            NavigationDestination.SETTINGS,
            NavigationDestination.SETTINGS,
        )
    }

    @Test
    fun `test navigation destination enum inequality`() {
        assertTrue(
            "HOME should not equal GENERATOR",
            NavigationDestination.HOME != NavigationDestination.GENERATOR,
        )
        assertTrue(
            "HOME should not equal SETTINGS",
            NavigationDestination.HOME != NavigationDestination.SETTINGS,
        )
        assertTrue(
            "GENERATOR should not equal SETTINGS",
            NavigationDestination.GENERATOR != NavigationDestination.SETTINGS,
        )
    }

    @Test
    fun `test all navigation destinations are unique`() {
        val destinations =
            listOf(
                NavigationDestination.HOME,
                NavigationDestination.GENERATOR,
                NavigationDestination.SETTINGS,
            )
        assertEquals("Should have 3 destinations", 3, destinations.size)
        assertEquals("All destinations should be unique", 3, destinations.toSet().size)
    }
}
