package com.batodev.arrows.data

import kotlinx.coroutines.flow.Flow

interface IGameStateRepository {
    fun hasSavedLevel(): Flow<Boolean>
    suspend fun saveGameLevel(stateType: String, width: Int, height: Int, snakes: List<SnakeSaveData>)
    suspend fun loadGameLevel(stateType: String): GameLevelData?
    suspend fun clearAllSavedLevels()
}

interface ICorePreferences {
    val theme: Flow<String>
    val animationSpeed: Flow<String>
    val isVibrationEnabled: Flow<Boolean>
    val isSoundsEnabled: Flow<Boolean>

    suspend fun saveThemePreference(theme: String)
    suspend fun saveAnimationSpeed(speed: String)
    suspend fun saveVibrationPreference(enabled: Boolean)
    suspend fun saveSoundsPreference(enabled: Boolean)
}

interface IGameplayPreferences {
    val isFillBoardEnabled: Flow<Boolean>
    val levelNumber: Flow<Int>
    val currentLives: Flow<Int>
    val gamesCompleted: Flow<Int>
    val introCompleted: Flow<Boolean>

    suspend fun saveFillBoardPreference(enabled: Boolean)
    suspend fun saveLevelNumber(level: Int)
    suspend fun saveCurrentLives(lives: Int)
    suspend fun incrementGamesCompleted()
    suspend fun saveIntroCompleted(completed: Boolean)
}

interface IDebugPreferences {
    val debugForcedWidth: Flow<Int?>
    val debugForcedHeight: Flow<Int?>
    val debugForcedLives: Flow<Int?>
    val debugForcedShape: Flow<String?>

    suspend fun saveDebugForcedWidth(width: Int?)
    suspend fun saveDebugForcedHeight(height: Int?)
    suspend fun saveDebugForcedLives(lives: Int?)
    suspend fun saveDebugForcedShape(shape: String?)
}

interface IMonetizationPreferences {
    val isAdFree: Flow<Boolean>
    val rewardAdCount: Flow<Int>
    val isWinVideosEnabled: Flow<Boolean>

    suspend fun saveIsAdFree(isAdFree: Boolean)
    suspend fun incrementRewardAdCount()
    suspend fun resetRewardAdCount()
    suspend fun saveWinVideosEnabled(enabled: Boolean)
}

// Room DAOs and preference repositories mirror a single DB table's columns;
// this stays one interface for callers (a single Koin single<>), the function
// count is kept under threshold by segregating the members across the four
// cohesive interfaces above instead.
interface IUserPreferencesRepository :
    ICorePreferences,
    IGameplayPreferences,
    IDebugPreferences,
    IMonetizationPreferences
