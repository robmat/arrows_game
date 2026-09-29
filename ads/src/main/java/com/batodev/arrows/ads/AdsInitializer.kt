package com.batodev.arrows.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _isInitialized = MutableStateFlow(false)

    /** True once the SDK has been started - which only happens after consent allows ad requests. */
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    fun initialize() {
        if (!isStarted.compareAndSet(false, true)) return

        scope.launch {
            MobileAds.initialize(context)
            withContext(Dispatchers.Main) {
                rewardAdManager.loadRewardAd()
                interstitialAdManager.loadInterstitialAd()
                _isInitialized.value = true
            }
        }
    }
}
