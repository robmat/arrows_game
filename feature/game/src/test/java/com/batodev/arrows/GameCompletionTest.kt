package com.batodev.arrows

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameCompletionTest {
    @Test
    fun `test ad-free user never sees interstitial`() {
        assertFalse(shouldShowInterstitialAd(isAdFree = true, gamesCompleted = 3))
        assertFalse(shouldShowInterstitialAd(isAdFree = true, gamesCompleted = 6))
        assertFalse(shouldShowInterstitialAd(isAdFree = true, gamesCompleted = 9))
    }

    @Test
    fun `test interstitial shown every 3 games`() {
        assertTrue(shouldShowInterstitialAd(isAdFree = false, gamesCompleted = 3))
        assertTrue(shouldShowInterstitialAd(isAdFree = false, gamesCompleted = 6))
        assertTrue(shouldShowInterstitialAd(isAdFree = false, gamesCompleted = 9))
    }

    @Test
    fun `test no interstitial between intervals`() {
        assertFalse(shouldShowInterstitialAd(isAdFree = false, gamesCompleted = 0))
        assertFalse(shouldShowInterstitialAd(isAdFree = false, gamesCompleted = 1))
        assertFalse(shouldShowInterstitialAd(isAdFree = false, gamesCompleted = 2))
        assertFalse(shouldShowInterstitialAd(isAdFree = false, gamesCompleted = 4))
    }

    @Test
    fun `test interstitial is due only when the next win shows one`() {
        assertTrue(isInterstitialDueNext(isAdFree = false, gamesCompleted = 2))
        assertTrue(isInterstitialDueNext(isAdFree = false, gamesCompleted = 5))
        assertFalse(isInterstitialDueNext(isAdFree = false, gamesCompleted = 0))
        assertFalse(isInterstitialDueNext(isAdFree = false, gamesCompleted = 3))
        assertFalse(isInterstitialDueNext(isAdFree = false, gamesCompleted = 4))
    }

    @Test
    fun `test interstitial never due for ad-free user`() {
        assertFalse(isInterstitialDueNext(isAdFree = true, gamesCompleted = 2))
        assertFalse(isInterstitialDueNext(isAdFree = true, gamesCompleted = 5))
    }
}
