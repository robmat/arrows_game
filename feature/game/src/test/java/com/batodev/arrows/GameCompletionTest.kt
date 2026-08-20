package com.batodev.arrows

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameCompletionTest {
    @Test
    fun `test ad-free user never sees interstitial`() {
        assertFalse(shouldShowInterstitialAd(isAdFree = true, gamesCompleted = 5))
        assertFalse(shouldShowInterstitialAd(isAdFree = true, gamesCompleted = 10))
        assertFalse(shouldShowInterstitialAd(isAdFree = true, gamesCompleted = 15))
    }

    @Test
    fun `test interstitial shown every 5 games`() {
        assertTrue(shouldShowInterstitialAd(isAdFree = false, gamesCompleted = 5))
        assertTrue(shouldShowInterstitialAd(isAdFree = false, gamesCompleted = 10))
        assertTrue(shouldShowInterstitialAd(isAdFree = false, gamesCompleted = 15))
    }

    @Test
    fun `test no interstitial between intervals`() {
        assertFalse(shouldShowInterstitialAd(isAdFree = false, gamesCompleted = 0))
        assertFalse(shouldShowInterstitialAd(isAdFree = false, gamesCompleted = 1))
        assertFalse(shouldShowInterstitialAd(isAdFree = false, gamesCompleted = 3))
        assertFalse(shouldShowInterstitialAd(isAdFree = false, gamesCompleted = 7))
    }
}
