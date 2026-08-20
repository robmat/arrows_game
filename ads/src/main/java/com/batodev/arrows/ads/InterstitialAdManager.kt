package com.batodev.arrows.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InterstitialAdManager(
    private val context: Context,
) {
    private var interstitialAd: InterstitialAd? = null
    private val _isAdLoaded = MutableStateFlow(false)
    val isAdLoaded: StateFlow<Boolean> = _isAdLoaded.asStateFlow()

    private val _isAdLoading = MutableStateFlow(false)
    val isAdLoading: StateFlow<Boolean> = _isAdLoading.asStateFlow()

    fun loadInterstitialAd() {
        if (_isAdLoading.value || _isAdLoaded.value) return

        _isAdLoading.value = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            BuildConfig.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
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

    fun showInterstitialAd(
        activity: Activity,
        onAdDismissed: () -> Unit,
    ) {
        interstitialAd?.let { ad ->
            ad.show(activity)
            _isAdLoaded.value = false
            interstitialAd = null
            ad.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        onAdDismissed()
                        loadInterstitialAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        onAdDismissed()
                    }
                }
        } ?: run {
            loadInterstitialAd()
            onAdDismissed()
        }
    }
}
