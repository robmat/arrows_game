package com.batodev.arrows.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.batodev.arrows.GameConstants
import com.batodev.arrows.SoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TapHandler(
    private val coroutineScope: CoroutineScope,
    private val soundManager: SoundManager?,
    private val onVibrate: () -> Unit,
) {
    var flashingSnakeId by mutableStateOf<Int?>(null)
        private set

    fun handleSnakeTap(params: TapParams) {
        if (params.isVibrationEnabled) onVibrate()
        if (params.obstructed) {
            handleObstructed(params.snake, params.lives, params.onPenalty)
        } else {
            handleSuccessful(params.onSuccess)
        }
    }

    fun flashSnake(snakeId: Int) {
        flashingSnakeId = snakeId
        coroutineScope.launch {
            delay(GameConstants.FLASH_DURATION_MS)
            if (flashingSnakeId == snakeId) flashingSnakeId = null
        }
    }

    private fun handleObstructed(
        snake: Snake,
        lives: Int,
        onPenalty: () -> Unit,
    ) {
        if (lives > 0) {
            onPenalty()
            if (lives > 1) soundManager?.playLiveLost() else soundManager?.playGameLost()
        }
        flashSnake(snake.id)
    }

    private fun handleSuccessful(onSuccess: () -> Unit) {
        soundManager?.playRandomSwitch()
        onSuccess()
    }

    fun clearFlash() {
        flashingSnakeId = null
    }
}
