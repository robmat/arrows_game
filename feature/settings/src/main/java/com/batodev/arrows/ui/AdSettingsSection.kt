package com.batodev.arrows.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.arrows.GameConstants
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White

@Composable
private fun AdRemovalRow(
    verticalPadding: Dp,
    trailingContent: @Composable () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Block,
            contentDescription = null,
            tint = White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.remove_ads_label),
            color = White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        trailingContent()
    }
}

@Composable
fun AdFreeSection(themeColors: ThemeColors) {
    AdRemovalRow(verticalPadding = 12.dp) {
        Text(
            text = stringResource(R.string.ads_removed),
            fontSize = 14.sp,
            color = themeColors.accent,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun AdNotFreeSection(state: AdSettingsSectionState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AdRemovalRow(verticalPadding = 8.dp) {
            Text(
                text = "${state.rewardAdCount} / ${GameConstants.REQUIRED_AD_COUNT_FOR_AD_FREE}",
                fontSize = 14.sp,
                color = state.themeColors.accent,
                fontWeight = FontWeight.Bold,
            )
        }
        LinearProgressIndicator(
            progress = { state.rewardAdCount.toFloat() / GameConstants.REQUIRED_AD_COUNT_FOR_AD_FREE },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            color = state.themeColors.accent,
        )
        AdWatchButton(state)
    }
}

@Composable
private fun AdWatchButton(state: AdSettingsSectionState) {
    Button(
        onClick = {
            state.activity?.let { act ->
                state.rewardAdManager.showRewardAd(
                    activity = act,
                    onRewarded = { handleAdReward(state) },
                    onAdDismissed = { /* No action needed */ },
                )
            }
        },
        enabled = state.isAdLoaded && !state.isAdLoading,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = state.themeColors.accent),
    ) {
        Text(
            text =
                when {
                    state.isAdLoading -> stringResource(R.string.loading_ad)
                    !state.isAdLoaded -> stringResource(R.string.ad_not_ready)
                    else -> stringResource(R.string.watch_ad_label)
                },
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun handleAdReward(state: AdSettingsSectionState) {
    state.viewModel.handleRewardedAdWatched()
}
