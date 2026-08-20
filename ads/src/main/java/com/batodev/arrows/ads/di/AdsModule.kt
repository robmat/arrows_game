package com.batodev.arrows.ads.di

import com.batodev.arrows.ads.ConsentManager
import com.batodev.arrows.ads.InterstitialAdManager
import com.batodev.arrows.ads.RewardAdManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val adsModule =
    module {
        single { RewardAdManager(androidContext()) }
        single { InterstitialAdManager(androidContext()) }
        single { ConsentManager(androidContext()) }
    }
