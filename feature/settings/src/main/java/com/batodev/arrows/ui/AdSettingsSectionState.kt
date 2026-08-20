package com.batodev.arrows.ui

import android.app.Activity
import com.batodev.arrows.ads.RewardAdManager
import com.batodev.arrows.ui.theme.ThemeColors

data class AdSettingsSectionState(
    val onRewardedAdWatched: () -> Unit,
    val rewardAdManager: RewardAdManager,
    val themeColors: ThemeColors,
    val activity: Activity?,
    val rewardAdCount: Int,
    val isAdLoaded: Boolean,
    val isAdLoading: Boolean,
)
