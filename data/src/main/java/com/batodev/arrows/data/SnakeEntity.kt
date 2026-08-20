package com.batodev.arrows.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "snakes",
    foreignKeys = [
        ForeignKey(
            entity = GameBoardEntity::class,
            parentColumns = ["boardId"],
            childColumns = ["boardId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("boardId")],
)
data class SnakeEntity(
    @PrimaryKey(autoGenerate = true) val snakeRowId: Long = 0,
    val boardId: Long,
    val snakeId: Int,
    val headDirection: String,
)
