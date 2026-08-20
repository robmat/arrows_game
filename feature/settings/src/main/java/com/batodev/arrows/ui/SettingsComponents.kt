package com.batodev.arrows.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.arrows.GameConstants
import com.batodev.arrows.ads.RewardAdManager
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.ui.theme.InactiveIcon
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White

@Composable
fun PreferencesSection(params: PreferencesParams) {
    val isVibrationEnabled by params.viewModel.isVibrationEnabled.collectAsState()
    val isSoundsEnabled by params.viewModel.isSoundsEnabled.collectAsState()
    val isFillBoardEnabled by params.viewModel.isFillBoardEnabled.collectAsState()
    val isWinVideosEnabled by params.viewModel.isWinVideosEnabled.collectAsState()

    SettingsGroup(params.themeColors.topBarButton) {
        SettingsSwitchItem(
            Icons.Default.Vibration,
            stringResource(R.string.vibrations_label),
            isVibrationEnabled,
            params.themeColors.accent,
        ) { params.viewModel.saveVibration(it) }
        SettingsSwitchItem(
            Icons.AutoMirrored.Filled.VolumeUp,
            stringResource(R.string.sounds_label),
            isSoundsEnabled,
            params.themeColors.accent,
        ) { params.viewModel.saveSounds(it) }
        SettingsSwitchItem(
            Icons.Default.VideoLibrary,
            stringResource(R.string.win_videos_label),
            isWinVideosEnabled,
            params.themeColors.accent,
        ) { params.viewModel.saveWinVideosEnabled(it) }
        SettingsSwitchItem(
            Icons.Default.Grid4x4,
            stringResource(R.string.fill_board_label),
            isFillBoardEnabled,
            params.themeColors.accent,
        ) { params.viewModel.saveFillBoard(it) }
        SettingsClickableItem(
            icon = Icons.Default.Palette,
            title = stringResource(R.string.theme_label),
            valueText = getLocalizedThemeName(params.currentTheme),
            onClick = params.onThemeClick,
        )
        SettingsClickableItem(
            icon = Icons.Default.Speed,
            title = stringResource(R.string.animation_speed_label),
            valueText = getLocalizedSpeedName(params.currentSpeed),
            onClick = params.onSpeedClick,
        )
    }
}

@Composable
private fun getLocalizedThemeName(theme: String): String =
    when (theme) {
        "Dark" -> stringResource(R.string.theme_dark)
        "Green" -> stringResource(R.string.theme_green)
        "Red" -> stringResource(R.string.theme_red)
        "Yellow" -> stringResource(R.string.theme_yellow)
        "Orange" -> stringResource(R.string.theme_orange)
        "Black and White" -> stringResource(R.string.theme_bw)
        else -> theme
    }

@Composable
private fun getLocalizedSpeedName(speed: String): String =
    when (speed) {
        "High" -> stringResource(R.string.speed_high)
        "Medium" -> stringResource(R.string.speed_medium)
        "Low" -> stringResource(R.string.speed_low)
        else -> speed
    }

@Composable
fun FeedbackSection(
    context: Context,
    themeColors: ThemeColors,
) {
    SettingsGroup(themeColors.topBarButton) {
        SettingsClickableItem(
            icon = Icons.Default.Star,
            title = stringResource(R.string.rate_us_label),
            onClick = { SettingsUtils.launchReviewFlow(context) },
        )
        SettingsClickableItem(
            icon = Icons.Default.Edit,
            title = stringResource(R.string.write_us_label),
            onClick = { SettingsUtils.launchEmail(context) },
        )
        SettingsClickableItem(
            icon = Icons.Default.Apps,
            title = stringResource(R.string.more_games_label),
            onClick = {
                SettingsUtils.launchBrowser(
                    context,
                    "https://play.google.com/store/apps/dev?id=8228670503574649511",
                )
            },
        )
        SettingsClickableItem(
            icon = Icons.Default.Code,
            title = stringResource(R.string.source_code_label),
            onClick = { SettingsUtils.launchBrowser(context, GameConstants.GITHUB_REPO_URL) },
        )
    }
}

@Composable
fun PurchasesSection(
    viewModel: AppViewModel,
    rewardAdManager: RewardAdManager,
    themeColors: ThemeColors,
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val isAdFree by viewModel.isAdFree.collectAsState()
    val rewardAdCount by viewModel.rewardAdCount.collectAsState()
    val isAdLoaded by rewardAdManager.isAdLoaded.collectAsState()
    val isAdLoading by rewardAdManager.isAdLoading.collectAsState()

    SettingsGroup(themeColors.topBarButton) {
        if (isAdFree) {
            AdFreeSection(themeColors)
        } else {
            AdNotFreeSection(
                AdSettingsSectionState(
                    onRewardedAdWatched = viewModel::handleRewardedAdWatched,
                    rewardAdManager = rewardAdManager,
                    themeColors = themeColors,
                    activity = activity,
                    rewardAdCount = rewardAdCount,
                    isAdLoaded = isAdLoaded,
                    isAdLoading = isAdLoading,
                ),
            )
        }
    }
}

@Composable
fun LegalSection(
    context: Context,
    themeColors: ThemeColors,
    onLicensesClick: () -> Unit,
    showPrivacyOptions: Boolean = false,
    onPrivacyOptionsClick: () -> Unit = {},
) {
    SettingsGroup(themeColors.topBarButton) {
        SettingsClickableItem(
            icon = Icons.Default.Description,
            title = stringResource(R.string.privacy_label),
            onClick = { SettingsUtils.launchBrowser(context, "https://robmat.github.io/privacy_policy.html") },
        )
        if (showPrivacyOptions) {
            SettingsClickableItem(
                icon = Icons.Default.Description,
                title = stringResource(R.string.manage_consent_label),
                onClick = { onPrivacyOptionsClick() },
            )
        }
        SettingsClickableItem(
            icon = Icons.Default.Description,
            title = stringResource(R.string.third_party_licenses_label),
            onClick = { onLicensesClick() },
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onThemeSelect: (String) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val themes = listOf("Dark", "Green", "Red", "Yellow", "Orange", "Black and White")
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = themeColors.bottomBar,
        title = {
            Text(
                text = stringResource(R.string.choose_theme_title),
                color = White,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                themes.forEach { theme ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onThemeSelect(theme) }
                                .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = theme == currentTheme,
                            onClick = { onThemeSelect(theme) },
                            colors =
                                RadioButtonDefaults.colors(
                                    selectedColor = themeColors.accent,
                                    unselectedColor = InactiveIcon,
                                ),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = getLocalizedThemeName(theme), color = White, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_label), color = themeColors.accent)
            }
        },
    )
}

@Composable
fun AnimationSpeedSelectionDialog(
    currentSpeed: String,
    onDismiss: () -> Unit,
    onSpeedSelect: (String) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val speeds = listOf("High", "Medium", "Low")
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = themeColors.bottomBar,
        title = {
            Text(
                text = stringResource(R.string.animation_speed_label),
                color = White,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                speeds.forEach { speed ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSpeedSelect(speed) }
                                .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = speed == currentSpeed,
                            onClick = { onSpeedSelect(speed) },
                            colors =
                                RadioButtonDefaults.colors(
                                    selectedColor = themeColors.accent,
                                    unselectedColor = InactiveIcon,
                                ),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = getLocalizedSpeedName(speed), color = White, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_label), color = themeColors.accent)
            }
        },
    )
}

data class PreferencesParams(
    val viewModel: AppViewModel,
    val themeColors: ThemeColors,
    val currentTheme: String,
    val currentSpeed: String,
    val onThemeClick: () -> Unit,
    val onSpeedClick: () -> Unit,
)
