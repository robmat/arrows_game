package com.batodev.arrows.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Starts the Mobile Ads SDK, once, after consent allows ad requests.
 *
 * MobileAds.initialize() runs on a background thread, as Google recommends: its synchronous part
 * (checking and loading the ads module from Google Play services) otherwise blocks the main thread
 * during launch, which is slow on low-end devices. The first ads are then loaded on the main
 * thread, which their load() calls require.
 */
class AdsInitializer(
    private val context: Context,
    private val rewardAdManager: RewardAdManager,
    private val interstitialAdManager: InterstitialAdManager,
) {
    private val isStarted = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize() {
        if (!isStarted.compareAndSet(false, true)) return

        scope.launch {
            MobileAds.initialize(context)
            withContext(Dispatchers.Main) {
                rewardAdManager.loadRewardAd()
                interstitialAdManager.loadInterstitialAd()
            }
        }
    }
}
