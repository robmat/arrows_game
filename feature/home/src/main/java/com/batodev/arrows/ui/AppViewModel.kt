package com.batodev.arrows.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batodev.arrows.GameConstants
import com.batodev.arrows.data.GameStateDao
import com.batodev.arrows.data.IUserPreferencesRepository
import com.batodev.arrows.data.hasSavedLevel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val userPreferencesRepository: IUserPreferencesRepository,
    private val gameStateDao: GameStateDao
) : ViewModel() {

    var shapeProvider: com.batodev.arrows.engine.BoardShapeProvider? = null

    val theme: StateFlow<String> = userPreferencesRepository.theme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = "Dark"
    )

    val animationSpeed: StateFlow<String> = userPreferencesRepository.animationSpeed.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = "Medium"
    )

    val isVibrationEnabled: StateFlow<Boolean> = userPreferencesRepository.isVibrationEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = true
    )

    val isSoundsEnabled: StateFlow<Boolean> = userPreferencesRepository.isSoundsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = true
    )

    val isFillBoardEnabled: StateFlow<Boolean> = userPreferencesRepository.isFillBoardEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = false
    )

    val isWinVideosEnabled: StateFlow<Boolean> = userPreferencesRepository.isWinVideosEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = true
    )

    val levelNumber: StateFlow<Int> = userPreferencesRepository.levelNumber.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = 1
    )

    val hasSavedLevel: StateFlow<Boolean> = gameStateDao.hasSavedLevel().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = false
    )

    val isAdFree: StateFlow<Boolean> = userPreferencesRepository.isAdFree.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = false
    )

    val rewardAdCount: StateFlow<Int> = userPreferencesRepository.rewardAdCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = 0
    )

    val gamesCompleted: StateFlow<Int> = userPreferencesRepository.gamesCompleted.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = 0
    )

    val introCompleted: StateFlow<Boolean?> = userPreferencesRepository.introCompleted.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = null
    )

    fun saveTheme(theme: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveThemePreference(theme)
        }
    }

    fun saveVibration(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveVibrationPreference(enabled)
        }
    }

    fun saveSounds(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveSoundsPreference(enabled)
        }
    }

    fun saveFillBoard(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveFillBoardPreference(enabled)
        }
    }

    fun saveWinVideosEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveWinVideosEnabled(enabled)
        }
    }

    fun saveAnimationSpeed(speed: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveAnimationSpeed(speed)
        }
    }

    fun saveLevelNumber(level: Int) {
        viewModelScope.launch {
            userPreferencesRepository.saveLevelNumber(level)
        }
    }

    fun regenerateCurrentLevel() {
        viewModelScope.launch {
            gameStateDao.clearAllSavedLevels()
        }
    }

    /** Records a completed rewarded-ad view and grants ad-free once enough have been watched. */
    fun handleRewardedAdWatched() {
        viewModelScope.launch {
            userPreferencesRepository.incrementRewardAdCount()
            val newCount = userPreferencesRepository.rewardAdCount.first()
            if (newCount >= GameConstants.REQUIRED_AD_COUNT_FOR_AD_FREE) {
                userPreferencesRepository.saveIsAdFree(true)
                userPreferencesRepository.resetRewardAdCount()
            }
        }
    }

    fun incrementGamesCompleted() {
        viewModelScope.launch {
            userPreferencesRepository.incrementGamesCompleted()
        }
    }

    fun saveIntroCompleted(completed: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveIntroCompleted(completed)
        }
    }
}
