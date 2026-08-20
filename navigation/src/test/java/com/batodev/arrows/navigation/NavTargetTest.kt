package com.batodev.arrows.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class NavTargetTest {
    @Test
    fun `home is always the same singleton`() {
        assertSame(NavTarget.Home, NavTarget.Home)
    }

    @Test
    fun `generate is always the same singleton`() {
        assertSame(NavTarget.Generate, NavTarget.Generate)
    }

    @Test
    fun `settings is always the same singleton`() {
        assertSame(NavTarget.Settings, NavTarget.Settings)
    }

    @Test
    fun `game has false isCustom by default`() {
        assertFalse(NavTarget.Game().isCustom)
    }

    @Test
    fun `game has null customWidth by default`() {
        assertNull(NavTarget.Game().customWidth)
    }

    @Test
    fun `game has null customHeight by default`() {
        assertNull(NavTarget.Game().customHeight)
    }

    @Test
    fun `game has null customShape by default`() {
        assertNull(NavTarget.Game().customShape)
    }

    @Test
    fun `game targets with identical params are equal`() {
        val first = NavTarget.Game(isCustom = true, customWidth = 8, customHeight = 6, customShape = "heart")
        val second = NavTarget.Game(isCustom = true, customWidth = 8, customHeight = 6, customShape = "heart")
        assertEquals(first, second)
    }

    @Test
    fun `game targets with different widths are not equal`() {
        val first = NavTarget.Game(customWidth = 5)
        val second = NavTarget.Game(customWidth = 10)
        assertNotEquals(first, second)
    }

    @Test
    fun `game targets with different shapes are not equal`() {
        val first = NavTarget.Game(customShape = "circle")
        val second = NavTarget.Game(customShape = "heart")
        assertNotEquals(first, second)
    }

    @Test
    fun `toCustomGameParams maps isCustom correctly`() {
        val params = NavTarget.Game(isCustom = true).toCustomGameParams()
        assertTrue(params.isCustom)
    }

    @Test
    fun `toCustomGameParams maps customWidth correctly`() {
        val params = NavTarget.Game(customWidth = 12).toCustomGameParams()
        assertEquals(12, params.customWidth)
    }

    @Test
    fun `toCustomGameParams maps customHeight correctly`() {
        val params = NavTarget.Game(customHeight = 9).toCustomGameParams()
        assertEquals(9, params.customHeight)
    }

    @Test
    fun `toCustomGameParams maps customShape correctly`() {
        val params = NavTarget.Game(customShape = "diamond").toCustomGameParams()
        assertEquals("diamond", params.customShape)
    }

    @Test
    fun `toCustomGameParams preserves null width for default game`() {
        val params = NavTarget.Game().toCustomGameParams()
        assertNull(params.customWidth)
    }

    @Test
    fun `toCustomGameParams preserves null shape for default game`() {
        val params = NavTarget.Game().toCustomGameParams()
        assertNull(params.customShape)
    }

    @Test
    fun `toCustomGameParams maps all custom fields together`() {
        val game =
            NavTarget.Game(
                isCustom = true,
                customWidth = 7,
                customHeight = 5,
                customShape = "star",
            )
        val params = game.toCustomGameParams()
        assertTrue(params.isCustom)
        assertEquals(7, params.customWidth)
        assertEquals(5, params.customHeight)
        assertEquals("star", params.customShape)
    }
}
