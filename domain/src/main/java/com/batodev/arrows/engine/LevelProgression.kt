package com.batodev.arrows.engine

import com.batodev.arrows.GameConstants
import kotlin.random.Random

object LevelProgression {
    fun calculateLevelConfiguration(
        levelNum: Int,
        forcedWidth: Int? = null,
        forcedHeight: Int? = null,
        forcedLives: Int? = null,
    ): LevelConfiguration {
        val progressionStep = levelNum / GameConstants.LEVELS_PER_PROGRESSION_STEP
        val sizeReduction = progressionStep * GameConstants.SIZE_REDUCTION_PER_STEP
        val livesReduction = progressionStep * GameConstants.LIVES_REDUCTION_PER_STEP

        val baseH = GameConstants.BASE_BOARD_SIZE + (levelNum - 1) / 2
        val baseW = GameConstants.BASE_BOARD_SIZE + levelNum / 2

        val h = forcedHeight ?: (baseH - sizeReduction).coerceAtLeast(1)
        val w = forcedWidth ?: (baseW - sizeReduction).coerceAtLeast(1)

        val maxLives = forcedLives ?: (GameConstants.DEFAULT_INITIAL_LIVES - livesReduction).coerceAtLeast(1)
        val snakeLen =
            (GameConstants.MIN_SNAKE_LENGTH_BASE + levelNum / 2)
                .coerceIn(GameConstants.MIN_SNAKE_LENGTH_MIN, GameConstants.MIN_SNAKE_LENGTH_MAX)

        return LevelConfiguration(w, h, snakeLen, maxLives)
    }

    /**
     * Randomly decides whether a board shape should be applied to a level,
     * with the probability scaling from 0 at [GameConstants.MIN_BOARD_SIZE_FOR_SHAPES]
     * up to 1 at [GameConstants.MAX_BOARD_SIZE_FOR_ALWAYS_SHAPE].
     */
    fun shouldApplyShape(
        config: LevelConfiguration,
        random: Random,
    ): Boolean {
        val size = maxOf(config.width, config.height)
        val probability =
            when {
                size < GameConstants.MIN_BOARD_SIZE_FOR_SHAPES -> {
                    0f
                }

                size >= GameConstants.MAX_BOARD_SIZE_FOR_ALWAYS_SHAPE -> {
                    1f
                }

                else -> {
                    val ratio =
                        (size - GameConstants.MIN_BOARD_SIZE_FOR_SHAPES).toFloat() /
                            (GameConstants.MAX_BOARD_SIZE_FOR_ALWAYS_SHAPE - GameConstants.MIN_BOARD_SIZE_FOR_SHAPES)
                    GameConstants.BASE_SHAPE_PROBABILITY + (1f - GameConstants.BASE_SHAPE_PROBABILITY) * ratio
                }
            }
        return random.nextFloat() < probability
    }
}
