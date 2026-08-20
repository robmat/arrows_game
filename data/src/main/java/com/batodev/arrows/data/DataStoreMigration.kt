package com.batodev.arrows.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.IOException

private val Context.legacyDataStore by preferencesDataStore(name = "settings")

private const val TAG = "DataStoreMigration"

private fun Preferences.toUserPreferencesEntity() =
    UserPreferencesEntity(
        id = 1,
        theme = this[stringPreferencesKey("theme")] ?: "Green",
        animationSpeed = this[stringPreferencesKey("animation_speed")] ?: "Medium",
        isVibrationEnabled = this[booleanPreferencesKey("vibration_enabled")] ?: true,
        isSoundsEnabled = this[booleanPreferencesKey("sounds_enabled")] ?: true,
        isFillBoardEnabled = this[booleanPreferencesKey("fill_board_enabled")] ?: false,
        levelNumber = this[intPreferencesKey("level_number")] ?: 1,
        currentLives = this[intPreferencesKey("current_lives")] ?: 5,
        debugForcedWidth = this[intPreferencesKey("debug_forced_width")],
        debugForcedHeight = this[intPreferencesKey("debug_forced_height")],
        debugForcedLives = this[intPreferencesKey("debug_forced_lives")],
        debugForcedShape = this[stringPreferencesKey("debug_forced_shape")],
        isAdFree = this[booleanPreferencesKey("is_ad_free")] ?: false,
        rewardAdCount = this[intPreferencesKey("reward_ad_count")] ?: 0,
        gamesCompleted = this[intPreferencesKey("games_completed")] ?: 0,
        introCompleted = this[booleanPreferencesKey("intro_completed")] ?: false,
        isWinVideosEnabled = this[booleanPreferencesKey("win_videos_enabled")] ?: false,
    )

private fun deleteLegacyDataStoreFiles(
    context: Context,
    dataStoreFile: File,
) {
    dataStoreFile.delete()
    val dataStoreDir = File(context.filesDir, "datastore")
    if (dataStoreDir.isDirectory && dataStoreDir.listFiles().isNullOrEmpty()) {
        dataStoreDir.delete()
    }
    Log.i(TAG, "Old DataStore files cleaned up")
}

suspend fun migrateFromDataStoreIfNeeded(
    context: Context,
    dao: CorePreferencesDao,
) {
    val dataStoreFile = File(context.filesDir, "datastore/settings.preferences_pb")
    if (!dataStoreFile.exists()) return

    Log.i(TAG, "Found legacy DataStore file, starting migration")

    try {
        val entity =
            context.legacyDataStore.data
                .first()
                .toUserPreferencesEntity()
        dao.upsert(entity)
        Log.i(TAG, "Migration complete: levelNumber=${entity.levelNumber}, gamesCompleted=${entity.gamesCompleted}")
        deleteLegacyDataStoreFiles(context, dataStoreFile)
    } catch (e: IOException) {
        Log.e(TAG, "DataStore migration failed, keeping old file for retry", e)
    } catch (e: CorruptionException) {
        Log.e(TAG, "DataStore migration failed, keeping old file for retry", e)
    }
}
