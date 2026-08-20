package com.batodev.arrows.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RewardAdManager(
    private val context: Context,
) {
    private var rewardedAd: RewardedAd? = null
    private val _isAdLoaded = MutableStateFlow(false)
    val isAdLoaded: StateFlow<Boolean> = _isAdLoaded.asStateFlow()

    private val _isAdLoading = MutableStateFlow(false)
    val isAdLoading: StateFlow<Boolean> = _isAdLoading.asStateFlow()

    fun loadRewardAd() {
        if (_isAdLoading.value || _isAdLoaded.value) return

        _isAdLoading.value = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            BuildConfig.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _isAdLoaded.value = true
                    _isAdLoading.value = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    _isAdLoaded.value = false
                    _isAdLoading.value = false
                }
            },
        )
    }

    fun showRewardAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onAdDismissed: () -> Unit,
    ) {
        rewardedAd?.let { ad ->
            ad.show(activity) { _ ->
                onRewarded()
                _isAdLoaded.value = false
                rewardedAd = null
                loadRewardAd()
            }
            ad.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        onAdDismissed()
                        _isAdLoaded.value = false
                        rewardedAd = null
                        loadRewardAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        onAdDismissed()
                        _isAdLoaded.value = false
                        rewardedAd = null
                    }
                }
        } ?: run {
            loadRewardAd()
            onAdDismissed()
        }
    }
}
