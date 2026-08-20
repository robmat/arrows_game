package com.batodev.arrows.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.batodev.arrows.GameConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RemovalAnimator(
    private val coroutineScope: CoroutineScope,
) {
    var removalProgress by mutableStateOf<Map<Int, Float>>(emptyMap())
        private set

    fun animate(
        snakeId: Int,
        animationSpeed: String,
        onStep: (Int, Float) -> Unit,
        onComplete: (Int) -> Unit,
    ) {
        val durationMs =
            when (animationSpeed) {
                "High" -> GameConstants.REMOVAL_DURATION_HIGH
                "Low" -> GameConstants.REMOVAL_DURATION_LOW
                else -> GameConstants.REMOVAL_DURATION_MEDIUM
            }
        coroutineScope.launch {
            removalProgress = removalProgress.toMutableMap().apply { put(snakeId, 0f) }
            var elapsed = 0L
            while (elapsed < durationMs && isActive) {
                delay(GameConstants.REMOVAL_FRAME_DELAY_MS)
                elapsed += GameConstants.REMOVAL_FRAME_DELAY_MS
                val linearP = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
                val p = linearP * linearP * linearP
                removalProgress = removalProgress.toMutableMap().apply { put(snakeId, p) }
                onStep(snakeId, p)
            }
            removalProgress = removalProgress.toMutableMap().apply { put(snakeId, 1f) }
            onComplete(snakeId)
            removalProgress = removalProgress.toMutableMap().apply { remove(snakeId) }
        }
    }

    fun clear() {
        removalProgress = emptyMap()
    }
}
