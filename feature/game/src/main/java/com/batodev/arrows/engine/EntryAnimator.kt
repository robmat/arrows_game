package com.batodev.arrows.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.batodev.arrows.GameConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.pow

class EntryAnimator(
    private val coroutineScope: CoroutineScope,
) {
    companion object {
        private const val CUBIC_EASING_POWER = 3
        private const val COMPLETION_BUFFER_MS = 50L
    }

    var entryProgress by mutableStateOf<Map<Int, Float>>(emptyMap())
        private set
    var isEntryAnimating by mutableStateOf(false)
        private set

    private var entryJobs = mutableListOf<Job>()

    fun animate(
        snakes: List<Snake>,
        boardWidth: Int,
        boardHeight: Int,
    ) {
        clear()
        if (snakes.isEmpty()) return

        isEntryAnimating = true
        val initialProgress = snakes.associate { it.id to 0f }
        entryProgress = initialProgress

        // Sort by distance from board center -> ripple outward
        val centerX = boardWidth.toFloat() / 2f
        val centerY = boardHeight.toFloat() / 2f

        val sortedSnakes =
            snakes.sortedBy { snake ->
                val cx =
                    snake.body
                        .map { it.x.toFloat() }
                        .average()
                        .toFloat()
                val cy =
                    snake.body
                        .map { it.y.toFloat() }
                        .average()
                        .toFloat()
                (cx - centerX) * (cx - centerX) + (cy - centerY) * (cy - centerY)
            }

        val duration = GameConstants.SNAKE_ENTRY_DURATION_MS
        val stagger = GameConstants.SNAKE_ENTRY_STAGGER_MS
        val frameDelay = GameConstants.REMOVAL_FRAME_DELAY_MS

        sortedSnakes.forEachIndexed { index, snake ->
            val delayMs = stagger * index
            val snakeId = snake.id

            val job =
                coroutineScope.launch {
                    if (delayMs > 0) {
                        delay(delayMs)
                    }
                    if (!isActive) return@launch

                    val startTime = System.currentTimeMillis()
                    while (isActive) {
                        val elapsed = System.currentTimeMillis() - startTime
                        val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                        // Ease-out cubic for smooth deceleration: 1 - (1 - x)^3
                        val eased = 1f - (1f - progress).pow(CUBIC_EASING_POWER)

                        entryProgress = entryProgress.toMutableMap().apply { put(snakeId, eased) }

                        if (progress >= 1f) {
                            entryProgress = entryProgress.toMutableMap().apply { remove(snakeId) }
                            break
                        }
                        delay(frameDelay)
                    }
                }
            entryJobs.add(job)
        }

        // Clear isEntryAnimating after all snakes finish
        val totalDuration = stagger * (sortedSnakes.size - 1) + duration + COMPLETION_BUFFER_MS
        val completionJob =
            coroutineScope.launch {
                delay(totalDuration)
                if (isActive) {
                    isEntryAnimating = false
                }
            }
        entryJobs.add(completionJob)
    }

    fun clear() {
        entryJobs.forEach { it.cancel() }
        entryJobs.clear()
        entryProgress = emptyMap()
        isEntryAnimating = false
    }
}
