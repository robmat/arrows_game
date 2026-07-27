package com.batodev.arrows.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private const val SQL_CREATE_GAME_BOARDS = """CREATE TABLE IF NOT EXISTS game_boards (
                boardId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                stateType TEXT NOT NULL,
                width INTEGER NOT NULL,
                height INTEGER NOT NULL
            )"""

private const val SQL_CREATE_SNAKES = """CREATE TABLE IF NOT EXISTS snakes (
                snakeRowId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                boardId INTEGER NOT NULL,
                snakeId INTEGER NOT NULL,
                headDirection TEXT NOT NULL,
                FOREIGN KEY (boardId) REFERENCES game_boards(boardId) ON DELETE CASCADE
            )"""

private const val SQL_CREATE_SNAKES_INDEX =
    "CREATE INDEX IF NOT EXISTS index_snakes_boardId ON snakes(boardId)"

private const val SQL_CREATE_SNAKE_BODY_POINTS = """CREATE TABLE IF NOT EXISTS snake_body_points (
                pointId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                snakeRowId INTEGER NOT NULL,
                orderIndex INTEGER NOT NULL,
                x INTEGER NOT NULL,
                y INTEGER NOT NULL,
                FOREIGN KEY (snakeRowId) REFERENCES snakes(snakeRowId) ON DELETE CASCADE
            )"""

private const val SQL_CREATE_BODY_POINTS_INDEX =
    "CREATE INDEX IF NOT EXISTS index_snake_body_points_snakeRowId ON snake_body_points(snakeRowId)"

private const val SQL_CREATE_USER_PREFS_NEW = """CREATE TABLE user_preferences_new (
                id INTEGER PRIMARY KEY NOT NULL,
                theme TEXT NOT NULL DEFAULT 'Green',
                animationSpeed TEXT NOT NULL DEFAULT 'Medium',
                isVibrationEnabled INTEGER NOT NULL DEFAULT 1,
                isSoundsEnabled INTEGER NOT NULL DEFAULT 1,
                isFillBoardEnabled INTEGER NOT NULL DEFAULT 0,
                levelNumber INTEGER NOT NULL DEFAULT 1,
                currentLives INTEGER NOT NULL DEFAULT 5,
                debugForcedWidth INTEGER,
                debugForcedHeight INTEGER,
                debugForcedLives INTEGER,
                debugForcedShape TEXT,
                isAdFree INTEGER NOT NULL DEFAULT 0,
                rewardAdCount INTEGER NOT NULL DEFAULT 0,
                gamesCompleted INTEGER NOT NULL DEFAULT 0,
                introCompleted INTEGER NOT NULL DEFAULT 0,
                isWinVideosEnabled INTEGER NOT NULL DEFAULT 0
            )"""

private const val SQL_INSERT_USER_PREFS_FROM_OLD = """INSERT INTO user_preferences_new (
                id, theme, animationSpeed, isVibrationEnabled, isSoundsEnabled,
                isFillBoardEnabled, levelNumber, currentLives,
                debugForcedWidth, debugForcedHeight, debugForcedLives, debugForcedShape,
                isAdFree, rewardAdCount, gamesCompleted, introCompleted, isWinVideosEnabled
            ) SELECT
                id, theme, animationSpeed, isVibrationEnabled, isSoundsEnabled,
                isFillBoardEnabled, levelNumber, currentLives,
                debugForcedWidth, debugForcedHeight, debugForcedLives, debugForcedShape,
                isAdFree, rewardAdCount, gamesCompleted, introCompleted, isWinVideosEnabled
            FROM user_preferences"""

private const val SQL_DROP_USER_PREFS = "DROP TABLE user_preferences"
private const val SQL_RENAME_USER_PREFS = "ALTER TABLE user_preferences_new RENAME TO user_preferences"

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(SQL_CREATE_GAME_BOARDS)
        db.execSQL(SQL_CREATE_SNAKES)
        db.execSQL(SQL_CREATE_SNAKES_INDEX)
        db.execSQL(SQL_CREATE_SNAKE_BODY_POINTS)
        db.execSQL(SQL_CREATE_BODY_POINTS_INDEX)
        db.execSQL(SQL_CREATE_USER_PREFS_NEW)
        db.execSQL(SQL_INSERT_USER_PREFS_FROM_OLD)
        db.execSQL(SQL_DROP_USER_PREFS)
        db.execSQL(SQL_RENAME_USER_PREFS)
    }
}

@Database(
    entities = [
        UserPreferencesEntity::class,
        GameBoardEntity::class,
        SnakeEntity::class,
        SnakeBodyPointEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun corePreferencesDao(): CorePreferencesDao
    abstract fun gameplayPreferencesDao(): GameplayPreferencesDao
    abstract fun debugPreferencesDao(): DebugPreferencesDao
    abstract fun monetizationPreferencesDao(): MonetizationPreferencesDao
    abstract fun gameStateDao(): GameStateDao
}
