package com.batodev.arrows.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Loads and shows the interstitial. Loading is left to the caller, which should only ask for an ad
 * when one is about to be shown: every loaded ad that is never shown counts against the show rate.
 */
class InterstitialAdManager(
    private val context: Context,
    /** False until consent allows ad requests and the ads SDK has been started. */
    private val canRequestAds: () -> Boolean,
) {
    private var interstitialAd: InterstitialAd? = null
    private var loadedAtMillis = 0L
    private val _isAdLoaded = MutableStateFlow(false)
    val isAdLoaded: StateFlow<Boolean> = _isAdLoaded.asStateFlow()

    private val _isAdLoading = MutableStateFlow(false)
    val isAdLoading: StateFlow<Boolean> = _isAdLoading.asStateFlow()

    /** Loads an ad unless a fresh one is already loaded or loading; a stale one is replaced. */
    fun loadInterstitialAd() {
        if (_isAdLoaded.value && isStale()) clearAd()
        if (!canRequestAds() || _isAdLoading.value || _isAdLoaded.value) return

        _isAdLoading.value = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            BuildConfig.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    loadedAtMillis = SystemClock.elapsedRealtime()
                    _isAdLoaded.value = true
                    _isAdLoading.value = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    _isAdLoaded.value = false
                    _isAdLoading.value = false
                }
            },
        )
    }

    /** Shows the loaded ad, or - with none loaded, or only a stale one - moves straight on. */
    fun showInterstitialAd(
        activity: Activity,
        onAdDismissed: () -> Unit,
    ) {
        if (isStale()) clearAd()
        interstitialAd?.let { ad ->
            // Set before show(), as Google requires: a failure to show can be reported during
            // show() itself, and onAdDismissed - which moves the game on - must still run.
            ad.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        onAdDismissed()
                    }
                }
            clearAd()
            ad.show(activity)
        } ?: onAdDismissed()
    }

    // Google no longer serves an interstitial an hour after it loaded; showing one fails.
    private fun isStale(): Boolean = SystemClock.elapsedRealtime() - loadedAtMillis > MAX_AD_AGE_MILLIS

    private fun clearAd() {
        interstitialAd = null
        _isAdLoaded.value = false
    }

    private companion object {
        /** Just under the hour Google allows, so an ad is never shown on its last seconds. */
        const val MAX_AD_AGE_MILLIS = 55 * 60 * 1000L
    }
}
