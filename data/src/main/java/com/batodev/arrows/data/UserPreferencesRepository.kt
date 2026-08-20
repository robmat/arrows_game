package com.batodev.arrows.data

import com.batodev.arrows.data.IUserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class UserPreferencesRepository(
    private val core: CorePreferencesDao,
    private val gameplay: GameplayPreferencesDao,
    private val debug: DebugPreferencesDao,
    private val monetization: MonetizationPreferencesDao,
) : IUserPreferencesRepository {
    override val theme: Flow<String> get() = core.getTheme()
    override val animationSpeed: Flow<String> get() = core.getAnimationSpeed()
    override val isVibrationEnabled: Flow<Boolean> get() = core.getIsVibrationEnabled()
    override val isSoundsEnabled: Flow<Boolean> get() = core.getIsSoundsEnabled()
    override val isFillBoardEnabled: Flow<Boolean> get() = gameplay.getIsFillBoardEnabled()
    override val levelNumber: Flow<Int> get() = gameplay.getLevelNumber()
    override val currentLives: Flow<Int> get() = gameplay.getCurrentLives()
    override val debugForcedWidth: Flow<Int?> get() = debug.getDebugForcedWidth()
    override val debugForcedHeight: Flow<Int?> get() = debug.getDebugForcedHeight()
    override val debugForcedLives: Flow<Int?> get() = debug.getDebugForcedLives()
    override val debugForcedShape: Flow<String?> get() = debug.getDebugForcedShape()
    override val isAdFree: Flow<Boolean> get() = monetization.getIsAdFree()
    override val rewardAdCount: Flow<Int> get() = monetization.getRewardAdCount()
    override val gamesCompleted: Flow<Int> get() = gameplay.getGamesCompleted()
    override val introCompleted: Flow<Boolean> get() = gameplay.getIntroCompleted()
    override val isWinVideosEnabled: Flow<Boolean> get() = monetization.getIsWinVideosEnabled()

    override suspend fun saveThemePreference(theme: String) = core.updateTheme(theme)

    override suspend fun saveAnimationSpeed(speed: String) = core.updateAnimationSpeed(speed)

    override suspend fun saveVibrationPreference(enabled: Boolean) = core.updateVibrationEnabled(enabled)

    override suspend fun saveSoundsPreference(enabled: Boolean) = core.updateSoundsEnabled(enabled)

    override suspend fun saveFillBoardPreference(enabled: Boolean) = gameplay.updateFillBoardEnabled(enabled)

    override suspend fun saveLevelNumber(level: Int) = gameplay.updateLevelNumber(level)

    override suspend fun saveCurrentLives(lives: Int) = gameplay.updateCurrentLives(lives)

    override suspend fun saveDebugForcedWidth(width: Int?) = debug.updateDebugForcedWidth(width)

    override suspend fun saveDebugForcedHeight(height: Int?) = debug.updateDebugForcedHeight(height)

    override suspend fun saveDebugForcedLives(lives: Int?) = debug.updateDebugForcedLives(lives)

    override suspend fun saveDebugForcedShape(shape: String?) = debug.updateDebugForcedShape(shape)

    override suspend fun saveIsAdFree(isAdFree: Boolean) = monetization.updateIsAdFree(isAdFree)

    override suspend fun incrementRewardAdCount() = monetization.incrementRewardAdCount()

    override suspend fun resetRewardAdCount() = monetization.resetRewardAdCount()

    override suspend fun saveIntroCompleted(completed: Boolean) = gameplay.updateIntroCompleted(completed)

    override suspend fun saveWinVideosEnabled(enabled: Boolean) = monetization.updateWinVideosEnabled(enabled)

    override suspend fun incrementGamesCompleted() = gameplay.incrementGamesCompleted()
}
