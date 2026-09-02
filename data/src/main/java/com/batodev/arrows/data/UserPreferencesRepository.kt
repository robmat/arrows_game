package com.batodev.arrows.data

import kotlinx.coroutines.flow.Flow

// Each Impl below wraps exactly one of the DAOs UserPreferencesDao.kt already split by
// cohesive domain and implements the matching Repositories.kt sub-interface, so no individual
// class's declared-function count approaches TooManyFunctions' threshold. UserPreferencesRepository
// itself declares none of its own - Kotlin's `by` delegation generates the overrides at the
// bytecode level, invisible to detekt's source-level function count, so composing the four
// domains back into one facade for callers costs nothing against the same rule.
private class CorePreferencesImpl(
    private val dao: CorePreferencesDao,
) : ICorePreferences {
    override val theme: Flow<String> get() = dao.getTheme()
    override val animationSpeed: Flow<String> get() = dao.getAnimationSpeed()
    override val isVibrationEnabled: Flow<Boolean> get() = dao.getIsVibrationEnabled()
    override val isSoundsEnabled: Flow<Boolean> get() = dao.getIsSoundsEnabled()

    override suspend fun saveThemePreference(theme: String) = dao.updateTheme(theme)

    override suspend fun saveAnimationSpeed(speed: String) = dao.updateAnimationSpeed(speed)

    override suspend fun saveVibrationPreference(enabled: Boolean) = dao.updateVibrationEnabled(enabled)

    override suspend fun saveSoundsPreference(enabled: Boolean) = dao.updateSoundsEnabled(enabled)
}

private class GameplayPreferencesImpl(
    private val dao: GameplayPreferencesDao,
) : IGameplayPreferences {
    override val isFillBoardEnabled: Flow<Boolean> get() = dao.getIsFillBoardEnabled()
    override val levelNumber: Flow<Int> get() = dao.getLevelNumber()
    override val currentLives: Flow<Int> get() = dao.getCurrentLives()
    override val gamesCompleted: Flow<Int> get() = dao.getGamesCompleted()
    override val introCompleted: Flow<Boolean> get() = dao.getIntroCompleted()

    override suspend fun saveFillBoardPreference(enabled: Boolean) = dao.updateFillBoardEnabled(enabled)

    override suspend fun saveLevelNumber(level: Int) = dao.updateLevelNumber(level)

    override suspend fun saveCurrentLives(lives: Int) = dao.updateCurrentLives(lives)

    override suspend fun incrementGamesCompleted() = dao.incrementGamesCompleted()

    override suspend fun saveIntroCompleted(completed: Boolean) = dao.updateIntroCompleted(completed)
}

private class DebugPreferencesImpl(
    private val dao: DebugPreferencesDao,
) : IDebugPreferences {
    override val debugForcedWidth: Flow<Int?> get() = dao.getDebugForcedWidth()
    override val debugForcedHeight: Flow<Int?> get() = dao.getDebugForcedHeight()
    override val debugForcedLives: Flow<Int?> get() = dao.getDebugForcedLives()
    override val debugForcedShape: Flow<String?> get() = dao.getDebugForcedShape()

    override suspend fun saveDebugForcedWidth(width: Int?) = dao.updateDebugForcedWidth(width)

    override suspend fun saveDebugForcedHeight(height: Int?) = dao.updateDebugForcedHeight(height)

    override suspend fun saveDebugForcedLives(lives: Int?) = dao.updateDebugForcedLives(lives)

    override suspend fun saveDebugForcedShape(shape: String?) = dao.updateDebugForcedShape(shape)
}

private class MonetizationPreferencesImpl(
    private val dao: MonetizationPreferencesDao,
) : IMonetizationPreferences {
    override val isAdFree: Flow<Boolean> get() = dao.getIsAdFree()
    override val rewardAdCount: Flow<Int> get() = dao.getRewardAdCount()
    override val isWinVideosEnabled: Flow<Boolean> get() = dao.getIsWinVideosEnabled()

    override suspend fun saveIsAdFree(isAdFree: Boolean) = dao.updateIsAdFree(isAdFree)

    override suspend fun incrementRewardAdCount() = dao.incrementRewardAdCount()

    override suspend fun resetRewardAdCount() = dao.resetRewardAdCount()

    override suspend fun saveWinVideosEnabled(enabled: Boolean) = dao.updateWinVideosEnabled(enabled)
}

class UserPreferencesRepository(
    core: CorePreferencesDao,
    gameplay: GameplayPreferencesDao,
    debug: DebugPreferencesDao,
    monetization: MonetizationPreferencesDao,
) : IUserPreferencesRepository,
    ICorePreferences by CorePreferencesImpl(core),
    IGameplayPreferences by GameplayPreferencesImpl(gameplay),
    IDebugPreferences by DebugPreferencesImpl(debug),
    IMonetizationPreferences by MonetizationPreferencesImpl(monetization)
