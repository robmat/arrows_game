package com.batodev.arrows.engine

import com.batodev.arrows.data.GameLevelData
import com.batodev.arrows.data.GameStateDao
import com.batodev.arrows.data.IUserPreferencesRepository
import com.batodev.arrows.data.PointData
import com.batodev.arrows.data.SnakeSaveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class LevelManager(
    private val repository: IUserPreferencesRepository,
    private val gameGenerator: GameGenerator,
    private val shapeProvider: BoardShapeProvider?,
    private val random: kotlin.random.Random,
    private val gameStateDao: GameStateDao,
) {
    companion object {
        const val STATE_INITIAL = "INITIAL"
        const val STATE_CURRENT = "CURRENT"
    }

    private fun GameLevel.toSaveData(): List<SnakeSaveData> =
        snakes.map { snake ->
            SnakeSaveData(
                id = snake.id,
                headDirection = snake.headDirection.name,
                bodyPoints = snake.body.map { PointData(it.x, it.y) },
            )
        }

    private fun GameLevelData.toDomain(): GameLevel =
        GameLevel(
            width = width,
            height = height,
            snakes =
                snakes.map { sd ->
                    Snake(
                        id = sd.id,
                        body = sd.bodyPoints.map { Point(it.x, it.y) },
                        headDirection = Direction.valueOf(sd.headDirection),
                    )
                },
        )

    suspend fun loadLevel(
        onSuccess: (initial: GameLevel, current: GameLevel, maxLives: Int, currentLives: Int) -> Unit,
        onFailure: suspend () -> Unit,
    ) {
        val initialData = gameStateDao.loadGameLevel(STATE_INITIAL)
        val currentData = gameStateDao.loadGameLevel(STATE_CURRENT)
        val savedLives = repository.currentLives.firstOrNull()

        if (initialData != null && currentData != null) {
            try {
                val currentLevelNum = repository.levelNumber.firstOrNull() ?: 1
                val config = LevelProgression.calculateLevelConfiguration(currentLevelNum)
                onSuccess(
                    initialData.toDomain(),
                    currentData.toDomain(),
                    config.maxLives,
                    savedLives ?: config.maxLives,
                )
            } catch (_: Exception) {
                onFailure()
            }
        } else {
            onFailure()
        }
    }

    suspend fun regenerateLevel(params: RegenerationParams) {
        val fillBoard = repository.isFillBoardEnabled.firstOrNull() ?: false
        val currentLevelNum = repository.levelNumber.firstOrNull() ?: 1

        val config =
            LevelProgression.calculateLevelConfiguration(
                currentLevelNum,
                params.forcedWidth,
                params.forcedHeight,
                params.forcedLives,
            )

        val shape = determineShape(config, params.forcedShape, params.isCustomGame)

        val newLevel =
            gameGenerator.generateSolvableLevel(
                GenerationParams(
                    width = config.width,
                    height = config.height,
                    maxSnakeLength = config.maxSnakeLength,
                    onProgress = params.onProgress,
                    fillTheBoard = fillBoard,
                    boardShape = shape,
                ),
            )

        withContext(Dispatchers.Main) {
            params.onComplete(newLevel, config)
        }
    }

    private fun determineShape(
        config: LevelConfiguration,
        forcedShape: String?,
        isCustomGame: Boolean,
    ): BoardShape? =
        if (forcedShape != null) {
            shapeProvider?.getShapeByName(forcedShape)
        } else if (isCustomGame) {
            null
        } else if (LevelProgression.shouldApplyShape(config, random)) {
            shapeProvider?.getRandomShape()
        } else {
            null
        }

    suspend fun saveState(
        level: GameLevel,
        lives: Int,
    ) {
        gameStateDao.saveGameLevel(STATE_CURRENT, level.width, level.height, level.toSaveData())
        repository.saveCurrentLives(lives)
    }

    suspend fun saveInitialState(
        initialLevel: GameLevel,
        lives: Int,
    ) {
        val saveData = initialLevel.toSaveData()
        gameStateDao.saveGameLevel(STATE_INITIAL, initialLevel.width, initialLevel.height, saveData)
        gameStateDao.saveGameLevel(STATE_CURRENT, initialLevel.width, initialLevel.height, saveData)
        repository.saveCurrentLives(lives)
    }

    suspend fun advanceLevel(currentLevelNum: Int) {
        repository.saveLevelNumber(currentLevelNum + 1)
        gameStateDao.clearAllSavedLevels()
    }

    suspend fun clearSavedGame() {
        gameStateDao.clearAllSavedLevels()
    }
}
