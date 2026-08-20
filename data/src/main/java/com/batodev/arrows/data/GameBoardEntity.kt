package com.batodev.arrows.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_boards")
data class GameBoardEntity(
    @PrimaryKey(autoGenerate = true) val boardId: Long = 0,
    val stateType: String,
    val width: Int,
    val height: Int,
)
