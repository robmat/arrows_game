package com.batodev.arrows.core.testing

import com.batodev.arrows.GameConstants
import com.batodev.arrows.data.IUserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Shared test double for [IUserPreferencesRepository].
 * Use in unit tests instead of mocking to stay aligned with the NIA testing pattern.
 */
class FakeUserPreferencesRepository : IUserPreferencesRepository {
    val themeFlow = MutableStateFlow("Dark")
    override val theme: Flow<String> = themeFlow

    val vibrationFlow = MutableStateFlow(true)
    override val isVibrationEnabled: Flow<Boolean> = vibrationFlow

    val soundsFlow = MutableStateFlow(true)
    override val isSoundsEnabled: Flow<Boolean> = soundsFlow

    val fillBoardFlow = MutableStateFlow(false)
    override val isFillBoardEnabled: Flow<Boolean> = fillBoardFlow

    val animationSpeedFlow = MutableStateFlow("Medium")
    override val animationSpeed: Flow<String> = animationSpeedFlow

    val levelNumberFlow = MutableStateFlow(1)
    override val levelNumber: Flow<Int> = levelNumberFlow

    val currentLivesFlow = MutableStateFlow(GameConstants.DEFAULT_LIVES)
    override val currentLives: Flow<Int> = currentLivesFlow

    val debugForcedWidthFlow = MutableStateFlow<Int?>(null)
    override val debugForcedWidth: Flow<Int?> = debugForcedWidthFlow

    val debugForcedHeightFlow = MutableStateFlow<Int?>(null)
    override val debugForcedHeight: Flow<Int?> = debugForcedHeightFlow

    val debugForcedLivesFlow = MutableStateFlow<Int?>(null)
    override val debugForcedLives: Flow<Int?> = debugForcedLivesFlow

    val debugForcedShapeFlow = MutableStateFlow<String?>(null)
    override val debugForcedShape: Flow<String?> = debugForcedShapeFlow

    val isAdFreeFlow = MutableStateFlow(false)
    override val isAdFree: Flow<Boolean> = isAdFreeFlow

    val rewardAdCountFlow = MutableStateFlow(0)
    override val rewardAdCount: Flow<Int> = rewardAdCountFlow

    val gamesCompletedFlow = MutableStateFlow(0)
    override val gamesCompleted: Flow<Int> = gamesCompletedFlow

    val introCompletedFlow = MutableStateFlow(false)
    override val introCompleted: Flow<Boolean> = introCompletedFlow

    val winVideosEnabledFlow = MutableStateFlow(true)
    override val isWinVideosEnabled: Flow<Boolean> = winVideosEnabledFlow

    override suspend fun saveThemePreference(theme: String) {
        themeFlow.value = theme
    }

    override suspend fun saveVibrationPreference(enabled: Boolean) {
        vibrationFlow.value = enabled
    }

    override suspend fun saveSoundsPreference(enabled: Boolean) {
        soundsFlow.value = enabled
    }

    override suspend fun saveFillBoardPreference(enabled: Boolean) {
        fillBoardFlow.value = enabled
    }

    override suspend fun saveAnimationSpeed(speed: String) {
        animationSpeedFlow.value = speed
    }

    override suspend fun saveLevelNumber(level: Int) {
        levelNumberFlow.value = level
    }

    override suspend fun saveCurrentLives(lives: Int) {
        currentLivesFlow.value = lives
    }

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

    override suspend fun saveIsAdFree(isAdFree: Boolean) {
        isAdFreeFlow.value = isAdFree
    }

    override suspend fun incrementRewardAdCount() {
        rewardAdCountFlow.value += 1
    }

    override suspend fun resetRewardAdCount() {
        rewardAdCountFlow.value = 0
    }

    override suspend fun incrementGamesCompleted() {
        gamesCompletedFlow.value += 1
    }

    override suspend fun saveIntroCompleted(completed: Boolean) {
        introCompletedFlow.value = completed
    }

    override suspend fun saveWinVideosEnabled(enabled: Boolean) {
        winVideosEnabledFlow.value = enabled
    }
}
