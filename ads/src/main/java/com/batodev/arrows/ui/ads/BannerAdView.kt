package com.batodev.arrows.ui.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.batodev.arrows.ads.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * A banner for the bottom of a screen. Only compose it once ads may be requested (see
 * AppViewModel.showBannerAds): consent resolved, the ads SDK started, and the user not ad-free.
 */
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val adView =
        remember {
            AdView(context).apply {
                adUnitId = BuildConfig.BANNER_AD_UNIT_ID
                setAdSize(AdSize.BANNER)
            }
        }
    AndroidView(modifier = modifier.fillMaxWidth(), factory = { adView })

    LaunchedEffect(adView) { adView.loadAd(AdRequest.Builder().build()) }
    // Every screen composes its own banner, so each one is paused with the screen's lifecycle and
    // destroyed when it leaves composition - otherwise every visit leaves another AdView behind,
    // its WebView still running. Effects are disposed in reverse order: pause, then destroy.
    DisposableEffect(adView) { onDispose { adView.destroy() } }
    LifecycleResumeEffect(adView) {
        adView.resume()
        onPauseOrDispose { adView.pause() }
    }
}
