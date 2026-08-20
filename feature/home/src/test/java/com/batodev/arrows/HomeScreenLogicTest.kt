package com.batodev.arrows

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeScreenLogicTest {
    @Test
    fun `entryDelayMs returns zero for first element`() {
        assertEquals(0, HomeScreenLogic.entryDelayMs(0))
    }

    @Test
    fun `entryDelayMs returns one stagger delay for second element`() {
        assertEquals(GameConstants.HOME_STAGGER_DELAY_MS, HomeScreenLogic.entryDelayMs(1))
    }

    @Test
    fun `entryDelayMs returns two stagger delays for third element`() {
        assertEquals(2 * GameConstants.HOME_STAGGER_DELAY_MS, HomeScreenLogic.entryDelayMs(2))
    }

    @Test
    fun `entryDelayMs scales linearly with index`() {
        val index = 5
        assertEquals(index * GameConstants.HOME_STAGGER_DELAY_MS, HomeScreenLogic.entryDelayMs(index))
    }
}
