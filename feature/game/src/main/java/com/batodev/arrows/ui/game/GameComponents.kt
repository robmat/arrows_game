package com.batodev.arrows.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLabel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.arrows.GameConstants
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.ui.BackIconButton
import com.batodev.arrows.ui.theme.HeartRed
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.ProgressBarGreen
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White

data class HintButtonState(
    val isAdFree: Boolean = false,
    val isAdLoaded: Boolean = false,
    val isAdLoading: Boolean = false,
)

data class GameTopBarState(
    val lives: Int,
    val maxLives: Int,
    val hintState: HintButtonState = HintButtonState(),
)

data class GameTopBarCallbacks(
    val onRestart: () -> Unit,
    val onHint: () -> Unit,
    val onBack: () -> Unit,
)

@Composable
fun GameTopBar(
    state: GameTopBarState,
    callbacks: GameTopBarCallbacks,
) {
    val themeColors = LocalThemeColors.current

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        GameControls(callbacks.onBack, callbacks.onRestart, themeColors)
        HeartsDisplay(state.lives, state.maxLives)
        HintButton(themeColors, callbacks.onHint, state.hintState)
    }
}

@Composable
private fun GameControls(
    onBack: () -> Unit,
    onRestart: () -> Unit,
    themeColors: ThemeColors,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BackIconButton(onClick = onBack, containerColor = themeColors.topBarButton)
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onRestart,
            colors = IconButtonDefaults.iconButtonColors(containerColor = themeColors.topBarButton),
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.content_description_restart),
                tint = White,
            )
        }
    }
}

@Composable
private fun HeartsDisplay(
    lives: Int,
    maxLives: Int,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(maxLives) { index ->
            Icon(
                imageVector = if (index < lives) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(R.string.content_description_life),
                tint = HeartRed,
                modifier = Modifier.size(24.dp),
            )
            if (index < maxLives - 1) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun HintButton(
    themeColors: ThemeColors,
    onClick: () -> Unit,
    state: HintButtonState,
) {
    val buttonText =
        when {
            state.isAdFree -> stringResource(R.string.hint_label)
            state.isAdLoading -> stringResource(R.string.loading_label)
            else -> stringResource(R.string.hint_label)
        }
    val buttonEnabled = state.isAdFree || !state.isAdLoading

    Button(
        onClick = onClick,
        enabled = buttonEnabled,
        colors = ButtonDefaults.buttonColors(containerColor = themeColors.topBarButton, contentColor = White),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.height(40.dp),
    ) {
        Icon(
            imageVector = Icons.Default.VideoLabel,
            contentDescription = stringResource(R.string.content_description_ad),
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = buttonText, fontSize = 12.sp)
    }
}

@Composable
fun GameProgressBar(
    totalSnakes: Int,
    currentSnakes: Int,
) {
    val themeColors = LocalThemeColors.current
    val targetProgress = if (totalSnakes > 0) (totalSnakes - currentSnakes).toFloat() / totalSnakes else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = GameConstants.PROGRESS_ANIM_DURATION),
        label = "ProgressBarAnimation",
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier.fillMaxWidth().height(6.dp),
        color = ProgressBarGreen,
        trackColor = themeColors.topBarButton,
        strokeCap = StrokeCap.Round,
    )
}
