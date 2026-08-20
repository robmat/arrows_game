package com.batodev.arrows.navigation.transitions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavTransitionTypeTest {
    @Test
    fun `has exactly five transition types`() {
        assertEquals(5, NavTransitionType.entries.size)
    }

    @Test
    fun `fade type exists`() {
        assertTrue(NavTransitionType.FADE in NavTransitionType.entries)
    }

    @Test
    fun `slide horizontal type exists`() {
        assertTrue(NavTransitionType.SLIDE_HORIZONTAL in NavTransitionType.entries)
    }

    @Test
    fun `slide vertical type exists`() {
        assertTrue(NavTransitionType.SLIDE_VERTICAL in NavTransitionType.entries)
    }

    @Test
    fun `scale fade type exists`() {
        assertTrue(NavTransitionType.SCALE_FADE in NavTransitionType.entries)
    }

    @Test
    fun `rotate fade type exists`() {
        assertTrue(NavTransitionType.ROTATE_FADE in NavTransitionType.entries)
    }

    @Test
    fun `entries returns all types without duplicates`() {
        val entries = NavTransitionType.entries
        assertEquals(entries.size, entries.toSet().size)
    }
}
