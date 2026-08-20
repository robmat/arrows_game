package com.batodev.arrows.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.batodev.arrows.GameConstants

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val theme: String = "Green",
    val animationSpeed: String = "Medium",
    val isVibrationEnabled: Boolean = true,
    val isSoundsEnabled: Boolean = true,
    val isFillBoardEnabled: Boolean = false,
    val levelNumber: Int = GameConstants.DEFAULT_LEVEL,
    val currentLives: Int = GameConstants.DEFAULT_LIVES,
    val debugForcedWidth: Int? = null,
    val debugForcedHeight: Int? = null,
    val debugForcedLives: Int? = null,
    val debugForcedShape: String? = null,
    val isAdFree: Boolean = false,
    val rewardAdCount: Int = 0,
    val gamesCompleted: Int = 0,
    val introCompleted: Boolean = false,
    val isWinVideosEnabled: Boolean = false,
)
