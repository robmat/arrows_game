package com.batodev.arrows.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "snake_body_points",
    foreignKeys = [
        ForeignKey(
            entity = SnakeEntity::class,
            parentColumns = ["snakeRowId"],
            childColumns = ["snakeRowId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("snakeRowId")],
)
data class SnakeBodyPointEntity(
    @PrimaryKey(autoGenerate = true) val pointId: Long = 0,
    val snakeRowId: Long,
    val orderIndex: Int,
    val x: Int,
    val y: Int,
)
