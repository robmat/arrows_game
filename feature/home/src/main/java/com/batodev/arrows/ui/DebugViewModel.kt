package com.batodev.arrows.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batodev.arrows.GameConstants
import com.batodev.arrows.data.IUserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Backs the debug-only board-override menu (DebugMenu, BuildConfig.DRAW_DEBUG_STUFF). */
class DebugViewModel(
    private val userPreferencesRepository: IUserPreferencesRepository
) : ViewModel() {

    enum class DebugOption {
        WIDTH, HEIGHT, LIVES, SHAPE
    }

    val debugForcedWidth: StateFlow<Int?> = userPreferencesRepository.debugForcedWidth.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = null
    )

    val debugForcedHeight: StateFlow<Int?> = userPreferencesRepository.debugForcedHeight.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = null
    )

    val debugForcedLives: StateFlow<Int?> = userPreferencesRepository.debugForcedLives.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = null
    )

    val debugForcedShape: StateFlow<String?> = userPreferencesRepository.debugForcedShape.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(GameConstants.STOP_TIMEOUT_MILLIS),
        initialValue = null
    )

    fun saveDebugOption(option: DebugOption, value: Any?) {
        viewModelScope.launch {
            when (option) {
                DebugOption.WIDTH -> userPreferencesRepository.saveDebugForcedWidth(value as? Int)
                DebugOption.HEIGHT -> userPreferencesRepository.saveDebugForcedHeight(value as? Int)
                DebugOption.LIVES -> userPreferencesRepository.saveDebugForcedLives(value as? Int)
                DebugOption.SHAPE -> userPreferencesRepository.saveDebugForcedShape(value as? String)
            }
        }
    }
}
