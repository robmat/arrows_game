package com.batodev.arrows

internal object SettingsScreenLogic {
    fun sectionEntryDelayMs(sectionIndex: Int): Int = sectionIndex * GameConstants.SETTINGS_STAGGER_DELAY_MS
}
