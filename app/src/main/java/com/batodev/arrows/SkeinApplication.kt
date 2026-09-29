package com.batodev.arrows

import android.app.Application
import com.batodev.arrows.ads.AdsInitializer
import com.batodev.arrows.ads.ConsentManager
import com.batodev.arrows.ads.di.adsModule
import com.batodev.arrows.data.di.dataModule
import com.batodev.arrows.ui.di.viewModelModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SkeinApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SkeinApplication)
            modules(dataModule, adsModule, viewModelModule)
        }
    }

    fun initializeAds() = get<AdsInitializer>().initialize()

    fun consentManager(): ConsentManager = get()
}
