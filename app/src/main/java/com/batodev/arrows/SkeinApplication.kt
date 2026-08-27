package com.batodev.arrows

import android.app.Application
import com.batodev.arrows.ads.ConsentManager
import com.batodev.arrows.ads.InterstitialAdManager
import com.batodev.arrows.ads.RewardAdManager
import com.batodev.arrows.ads.di.adsModule
import com.batodev.arrows.data.di.dataModule
import com.batodev.arrows.ui.di.viewModelModule
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.atomic.AtomicBoolean

class SkeinApplication : Application() {
    private val isAdsInitialized = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SkeinApplication)
            modules(dataModule, adsModule, viewModelModule)
        }
    }

    fun initializeAds() {
        if (!isAdsInitialized.compareAndSet(false, true)) return

        CoroutineScope(Dispatchers.Main).launch {
            MobileAds.initialize(this@SkeinApplication)
            get<RewardAdManager>().loadRewardAd()
            get<InterstitialAdManager>().loadInterstitialAd()
        }
    }

    fun consentManager(): ConsentManager = get()
}
