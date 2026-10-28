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
 * during launch, which is slow on low-end devices. The first rewarded ad is then loaded on the main
 * thread, which its load() call requires.
 */
class AdsInitializer(
    private val context: Context,
    private val rewardAdManager: RewardAdManager,
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
                // First: the managers' loads are no-ops until this is set. The interstitial is not
                // loaded here - the game screen loads it only when the coming win will show it.
                _isInitialized.value = true
                rewardAdManager.loadRewardAd()
            }
        }
    }
}
