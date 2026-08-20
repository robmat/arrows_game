package com.batodev.arrows

internal object HomeScreenLogic {
    fun entryDelayMs(staggerIndex: Int): Int = staggerIndex * GameConstants.HOME_STAGGER_DELAY_MS
}
