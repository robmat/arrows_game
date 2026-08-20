package com.batodev.arrows.data

data class PointData(
    val x: Int,
    val y: Int,
)

data class SnakeSaveData(
    val id: Int,
    val headDirection: String,
    val bodyPoints: List<PointData>,
)

data class SnakeData(
    val id: Int,
    val headDirection: String,
    val bodyPoints: List<PointData>,
)

data class GameLevelData(
    val width: Int,
    val height: Int,
    val snakes: List<SnakeData>,
)
