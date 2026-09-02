package com.batodev.arrows.ui

import com.batodev.arrows.data.IUserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The basic display/input preference saves SettingsScreen binds directly to its widgets (theme,
 * animation speed, vibration, sounds, fill-board) - split out of [AppViewModel] purely to keep
 * its own declared-function count under detekt's TooManyFunctions threshold, same rationale as
 * the DAO/repository splits in :data.
 */
class PreferenceActions(
    private val scope: CoroutineScope,
    private val repository: IUserPreferencesRepository,
) {
    fun saveTheme(theme: String) {
        scope.launch { repository.saveThemePreference(theme) }
    }

    fun saveAnimationSpeed(speed: String) {
        scope.launch { repository.saveAnimationSpeed(speed) }
    }

    fun saveVibration(enabled: Boolean) {
        scope.launch { repository.saveVibrationPreference(enabled) }
    }

    fun saveSounds(enabled: Boolean) {
        scope.launch { repository.saveSoundsPreference(enabled) }
    }

    fun saveFillBoard(enabled: Boolean) {
        scope.launch { repository.saveFillBoardPreference(enabled) }
    }
}
