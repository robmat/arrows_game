package com.batodev.arrows

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsScreenLogicTest {
    @Test
    fun `sectionEntryDelayMs returns zero for first section`() {
        assertEquals(0, SettingsScreenLogic.sectionEntryDelayMs(0))
    }

    @Test
    fun `sectionEntryDelayMs returns one stagger delay for second section`() {
        assertEquals(GameConstants.SETTINGS_STAGGER_DELAY_MS, SettingsScreenLogic.sectionEntryDelayMs(1))
    }

    @Test
    fun `sectionEntryDelayMs returns two stagger delays for third section`() {
        assertEquals(2 * GameConstants.SETTINGS_STAGGER_DELAY_MS, SettingsScreenLogic.sectionEntryDelayMs(2))
    }

    @Test
    fun `sectionEntryDelayMs scales linearly with index`() {
        val index = 4
        assertEquals(index * GameConstants.SETTINGS_STAGGER_DELAY_MS, SettingsScreenLogic.sectionEntryDelayMs(index))
    }
}
