package com.batodev.arrows.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.batodev.arrows.feature.game.BuildConfig
import com.batodev.arrows.ui.AppViewModel
import kotlinx.coroutines.CoroutineScope

data class IntroState(
    val showIntro: Boolean,
    val onDismiss: () -> Unit,
)

@Composable
fun rememberIntroState(
    viewModel: AppViewModel,
    isLoading: Boolean,
    snakesCount: Int,
): IntroState {
    val introCompleted by viewModel.introCompleted.collectAsState()
    var showIntro by remember { mutableStateOf(false) }
    // Remember the snake count at the moment the intro became visible so we only
    // auto-dismiss when a snake is actually removed, not on the initial load spike.
    var introSnakesCount by remember { mutableIntStateOf(-1) }

    LaunchedEffect(introCompleted, isLoading) {
        if (!isLoading && (introCompleted == false || BuildConfig.DRAW_DEBUG_STUFF)) {
            introSnakesCount = snakesCount
            showIntro = true
        }
    }

    // Auto-dismiss once the user removes their first snake
    LaunchedEffect(snakesCount) {
        if (showIntro && introSnakesCount > 0 && snakesCount < introSnakesCount) {
            showIntro = false
            if (!BuildConfig.DRAW_DEBUG_STUFF) viewModel.saveIntroCompleted(true)
        }
    }

    val onDismiss: () -> Unit = {
        showIntro = false
        if (!BuildConfig.DRAW_DEBUG_STUFF) viewModel.saveIntroCompleted(true)
    }
    return IntroState(showIntro, onDismiss)
}
