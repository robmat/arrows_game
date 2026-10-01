package com.batodev.arrows.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.batodev.arrows.GameConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow

class EntryAnimator(
    private val coroutineScope: CoroutineScope,
) {
    companion object {
        private const val CUBIC_EASING_POWER = 3

        // The ripple's stagger is spread over at most this long, however many snakes there are.
        // At a flat 50ms per snake a big board's entry ran for tens of seconds (51s at level
        // 1000) - and taps are ignored until it ends.
        private const val MAX_TOTAL_STAGGER_MS = 1_500L
    }

    var entryProgress by mutableStateOf<Map<Int, Float>>(emptyMap())
        private set
    var isEntryAnimating by mutableStateOf(false)
        private set

    private var entryJob: Job? = null

    fun animate(
        snakes: List<Snake>,
        boardWidth: Int,
        boardHeight: Int,
    ) {
        clear()
        if (snakes.isEmpty()) return

        val order = rippleOrder(snakes, boardWidth, boardHeight)
        val stagger = minOf(GameConstants.SNAKE_ENTRY_STAGGER_MS, MAX_TOTAL_STAGGER_MS / maxOf(1, order.size - 1))
        isEntryAnimating = true
        entryProgress = order.associateWith { 0f }

        // One loop drives every snake, rather than a coroutine per snake each copying the whole
        // progress map every frame. Time is counted in frames, as in RemovalAnimator.
        entryJob =
            coroutineScope.launch {
                var elapsed = 0L
                while (true) {
                    val progress = HashMap<Int, Float>()
                    order.forEachIndexed { index, snakeId ->
                        val linear =
                            ((elapsed - stagger * index).toFloat() / GameConstants.SNAKE_ENTRY_DURATION_MS)
                                .coerceIn(0f, 1f)
                        // Ease-out cubic for smooth deceleration: 1 - (1 - x)^3
                        if (linear < 1f) progress[snakeId] = 1f - (1f - linear).pow(CUBIC_EASING_POWER)
                    }
                    entryProgress = progress
                    if (progress.isEmpty()) break
                    delay(GameConstants.REMOVAL_FRAME_DELAY_MS)
                    elapsed += GameConstants.REMOVAL_FRAME_DELAY_MS
                }
                isEntryAnimating = false
            }
    }

    /** Snake ids by distance from the board centre, so the entry ripples outward. */
    private fun rippleOrder(
        snakes: List<Snake>,
        boardWidth: Int,
        boardHeight: Int,
    ): List<Int> {
        val centerX = boardWidth / 2f
        val centerY = boardHeight / 2f
        return snakes
            .map { snake ->
                val cx = snake.body.sumOf { it.x }.toFloat() / snake.body.size
                val cy = snake.body.sumOf { it.y }.toFloat() / snake.body.size
                snake.id to (cx - centerX) * (cx - centerX) + (cy - centerY) * (cy - centerY)
            }.sortedBy { it.second }
            .map { it.first }
    }

    fun clear() {
        entryJob?.cancel()
        entryJob = null
        entryProgress = emptyMap()
        isEntryAnimating = false
    }
}
