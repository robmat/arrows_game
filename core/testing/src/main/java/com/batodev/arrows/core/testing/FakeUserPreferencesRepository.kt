package com.batodev.arrows.core.testing

import com.batodev.arrows.GameConstants
import com.batodev.arrows.data.ICorePreferences
import com.batodev.arrows.data.IDebugPreferences
import com.batodev.arrows.data.IGameplayPreferences
import com.batodev.arrows.data.IMonetizationPreferences
import com.batodev.arrows.data.IUserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Each class below owns its own MutableStateFlows and implements one Repositories.kt
// sub-interface's suspend mutators - split the same way the real UserPreferencesRepository is
// (see its own file, in :data), so no individual class's declared-function count approaches
// TooManyFunctions' threshold, and FakeUserPreferencesRepository's own primary constructor stays
// under LongParameterList's threshold (four collaborators, not sixteen individual flows).
private class FakeCorePreferences : ICorePreferences {
    val themeFlow = MutableStateFlow("Dark")
    val animationSpeedFlow = MutableStateFlow("Medium")
    val vibrationFlow = MutableStateFlow(true)
    val soundsFlow = MutableStateFlow(true)

    override val theme: Flow<String> get() = themeFlow
    override val animationSpeed: Flow<String> get() = animationSpeedFlow
    override val isVibrationEnabled: Flow<Boolean> get() = vibrationFlow
    override val isSoundsEnabled: Flow<Boolean> get() = soundsFlow

    override suspend fun saveThemePreference(theme: String) {
        themeFlow.value = theme
    }

    override suspend fun saveAnimationSpeed(speed: String) {
        animationSpeedFlow.value = speed
    }

    override suspend fun saveVibrationPreference(enabled: Boolean) {
        vibrationFlow.value = enabled
    }

    override suspend fun saveSoundsPreference(enabled: Boolean) {
        soundsFlow.value = enabled
    }
}

private class FakeGameplayPreferences : IGameplayPreferences {
    val fillBoardFlow = MutableStateFlow(false)
    val levelNumberFlow = MutableStateFlow(1)
    val currentLivesFlow = MutableStateFlow(GameConstants.DEFAULT_LIVES)
    val gamesCompletedFlow = MutableStateFlow(0)
    val introCompletedFlow = MutableStateFlow(false)

    override val isFillBoardEnabled: Flow<Boolean> get() = fillBoardFlow
    override val levelNumber: Flow<Int> get() = levelNumberFlow
    override val currentLives: Flow<Int> get() = currentLivesFlow
    override val gamesCompleted: Flow<Int> get() = gamesCompletedFlow
    override val introCompleted: Flow<Boolean> get() = introCompletedFlow

    override suspend fun saveFillBoardPreference(enabled: Boolean) {
        fillBoardFlow.value = enabled
    }

    override suspend fun saveLevelNumber(level: Int) {
        levelNumberFlow.value = level
    }

    override suspend fun saveCurrentLives(lives: Int) {
        currentLivesFlow.value = lives
    }

    override suspend fun incrementGamesCompleted() {
        gamesCompletedFlow.value += 1
    }

    override suspend fun saveIntroCompleted(completed: Boolean) {
        introCompletedFlow.value = completed
    }
}

private class FakeDebugPreferences : IDebugPreferences {
    val debugForcedWidthFlow = MutableStateFlow<Int?>(null)
    val debugForcedHeightFlow = MutableStateFlow<Int?>(null)
    val debugForcedLivesFlow = MutableStateFlow<Int?>(null)
    val debugForcedShapeFlow = MutableStateFlow<String?>(null)

    override val debugForcedWidth: Flow<Int?> get() = debugForcedWidthFlow
    override val debugForcedHeight: Flow<Int?> get() = debugForcedHeightFlow
    override val debugForcedLives: Flow<Int?> get() = debugForcedLivesFlow
    override val debugForcedShape: Flow<String?> get() = debugForcedShapeFlow

    override suspend fun saveDebugForcedWidth(width: Int?) {
        debugForcedWidthFlow.value = width
    }

    override suspend fun saveDebugForcedHeight(height: Int?) {
        debugForcedHeightFlow.value = height
    }

    override suspend fun saveDebugForcedLives(lives: Int?) {
        debugForcedLivesFlow.value = lives
    }

    override suspend fun saveDebugForcedShape(shape: String?) {
        debugForcedShapeFlow.value = shape
    }
}

private class FakeMonetizationPreferences : IMonetizationPreferences {
    val isAdFreeFlow = MutableStateFlow(false)
    val rewardAdCountFlow = MutableStateFlow(0)
    val winVideosEnabledFlow = MutableStateFlow(true)

    override val isAdFree: Flow<Boolean> get() = isAdFreeFlow
    override val rewardAdCount: Flow<Int> get() = rewardAdCountFlow
    override val isWinVideosEnabled: Flow<Boolean> get() = winVideosEnabledFlow

    override suspend fun saveIsAdFree(isAdFree: Boolean) {
        isAdFreeFlow.value = isAdFree
    }

    override suspend fun incrementRewardAdCount() {
        rewardAdCountFlow.value += 1
    }

    override suspend fun resetRewardAdCount() {
        rewardAdCountFlow.value = 0
    }

    override suspend fun saveWinVideosEnabled(enabled: Boolean) {
        winVideosEnabledFlow.value = enabled
    }
}

/**
 * Shared test double for [IUserPreferencesRepository].
 * Use in unit tests instead of mocking to stay aligned with the NIA testing pattern.
 */
class FakeUserPreferencesRepository private constructor(
    private val core: FakeCorePreferences,
    private val gameplay: FakeGameplayPreferences,
    private val debug: FakeDebugPreferences,
    private val monetization: FakeMonetizationPreferences,
) : IUserPreferencesRepository,
    ICorePreferences by core,
    IGameplayPreferences by gameplay,
    IDebugPreferences by debug,
    IMonetizationPreferences by monetization {
    // The primary constructor is private since its parameter types are file-private (Kotlin
    // won't let a public constructor expose them) - this is the only constructor callers use.
    constructor() : this(
        FakeCorePreferences(),
        FakeGameplayPreferences(),
        FakeDebugPreferences(),
        FakeMonetizationPreferences(),
    )

    // Direct flow access for tests to poke/observe, e.g. `fake.levelNumberFlow.value = 5`.
    val themeFlow: MutableStateFlow<String> get() = core.themeFlow
    val animationSpeedFlow: MutableStateFlow<String> get() = core.animationSpeedFlow
    val vibrationFlow: MutableStateFlow<Boolean> get() = core.vibrationFlow
    val soundsFlow: MutableStateFlow<Boolean> get() = core.soundsFlow
    val fillBoardFlow: MutableStateFlow<Boolean> get() = gameplay.fillBoardFlow
    val levelNumberFlow: MutableStateFlow<Int> get() = gameplay.levelNumberFlow
    val currentLivesFlow: MutableStateFlow<Int> get() = gameplay.currentLivesFlow
    val gamesCompletedFlow: MutableStateFlow<Int> get() = gameplay.gamesCompletedFlow
    val introCompletedFlow: MutableStateFlow<Boolean> get() = gameplay.introCompletedFlow
    val debugForcedWidthFlow: MutableStateFlow<Int?> get() = debug.debugForcedWidthFlow
    val debugForcedHeightFlow: MutableStateFlow<Int?> get() = debug.debugForcedHeightFlow
    val debugForcedLivesFlow: MutableStateFlow<Int?> get() = debug.debugForcedLivesFlow
    val debugForcedShapeFlow: MutableStateFlow<String?> get() = debug.debugForcedShapeFlow
    val isAdFreeFlow: MutableStateFlow<Boolean> get() = monetization.isAdFreeFlow
    val rewardAdCountFlow: MutableStateFlow<Int> get() = monetization.rewardAdCountFlow
    val winVideosEnabledFlow: MutableStateFlow<Boolean> get() = monetization.winVideosEnabledFlow
}
