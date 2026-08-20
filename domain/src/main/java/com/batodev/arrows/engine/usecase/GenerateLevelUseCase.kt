package com.batodev.arrows.engine.usecase

import com.batodev.arrows.engine.BoardShape
import com.batodev.arrows.engine.BoardShapeProvider
import com.batodev.arrows.engine.GameGenerator
import com.batodev.arrows.engine.GameLevel
import com.batodev.arrows.engine.LevelConfiguration
import com.batodev.arrows.engine.LevelProgression
import com.batodev.arrows.engine.GenerationParams
import kotlin.random.Random

/**
 * Single-purpose use case that generates a solvable game level.
 *
 * Encapsulates the logic previously scattered across [LevelManager]:
 * level configuration calculation, board shape selection, and generation.
 */
class GenerateLevelUseCase(
    private val gameGenerator: GameGenerator,
    private val shapeProvider: BoardShapeProvider?,
    private val random: Random = Random.Default,
) {
    data class Params(
        val levelNumber: Int,
        val fillBoard: Boolean,
        val forcedWidth: Int? = null,
        val forcedHeight: Int? = null,
        val forcedLives: Int? = null,
        val forcedShape: String? = null,
        val isCustomGame: Boolean = false,
        val onProgress: (Float) -> Unit = {},
    )

    data class Result(
        val level: GameLevel,
        val config: LevelConfiguration,
    )

    suspend operator fun invoke(params: Params): Result {
        val config = LevelProgression.calculateLevelConfiguration(
            params.levelNumber, params.forcedWidth, params.forcedHeight, params.forcedLives
        )
        val shape = selectShape(config, params.forcedShape, params.isCustomGame)
        val level = gameGenerator.generateSolvableLevel(
            GenerationParams(
                width = config.width,
                height = config.height,
                maxSnakeLength = config.maxSnakeLength,
                onProgress = params.onProgress,
                fillTheBoard = params.fillBoard,
                boardShape = shape,
            )
        )
        return Result(level, config)
    }

    private fun selectShape(
        config: LevelConfiguration,
        forcedShape: String?,
        isCustomGame: Boolean,
    ): BoardShape? = when {
        forcedShape != null -> shapeProvider?.getShapeByName(forcedShape)
        isCustomGame -> null
        LevelProgression.shouldApplyShape(config, random) -> shapeProvider?.getRandomShape()
        else -> null
    }
}
