package com.batodev.arrows.ads.di

import com.batodev.arrows.ads.AdsInitializer
import com.batodev.arrows.ads.ConsentManager
import com.batodev.arrows.ads.InterstitialAdManager
import com.batodev.arrows.ads.RewardAdManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val adsModule =
    module {
        // The ad managers request ads only once AdsInitializer has started the SDK, which it does
        // only after consent allows it. Looked up on each call: AdsInitializer depends on them.
        single { RewardAdManager(androidContext()) { get<AdsInitializer>().isInitialized.value } }
        single { InterstitialAdManager(androidContext()) { get<AdsInitializer>().isInitialized.value } }
        single { ConsentManager(androidContext()) }
        single { AdsInitializer(androidContext(), get(), get()) }
    }
