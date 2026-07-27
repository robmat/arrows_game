package com.batodev.arrows.engine.usecase

import com.batodev.arrows.data.IGameStateRepository
import com.batodev.arrows.data.IUserPreferencesRepository
import com.batodev.arrows.data.GameLevelData
import com.batodev.arrows.engine.LevelProgression
import kotlinx.coroutines.flow.firstOrNull

/**
 * Loads the current saved game state and combines it with user preferences
 * to produce a fully resolved [Result] ready for the GameEngine to use.
 *
 * Returns null if no saved game exists.
 */
class GetCurrentGameUseCase(
    private val prefsRepository: IUserPreferencesRepository,
    private val gameStateRepository: IGameStateRepository,
) {
    companion object {
        private const val STATE_INITIAL = "INITIAL"
        private const val STATE_CURRENT = "CURRENT"
    }

    data class Result(
        val initialState: GameLevelData,
        val currentState: GameLevelData,
        val maxLives: Int,
        val currentLives: Int,
    )

    suspend operator fun invoke(): Result? {
        val initialData = gameStateRepository.loadGameLevel(STATE_INITIAL)
        val currentData = gameStateRepository.loadGameLevel(STATE_CURRENT)
        if (initialData == null || currentData == null) return null
        val levelNum = prefsRepository.levelNumber.firstOrNull() ?: 1
        val savedLives = prefsRepository.currentLives.firstOrNull()
        val config = LevelProgression.calculateLevelConfiguration(levelNum)
        return Result(
            initialState = initialData,
            currentState = currentData,
            maxLives = config.maxLives,
            currentLives = savedLives ?: config.maxLives,
        )
    }
}
