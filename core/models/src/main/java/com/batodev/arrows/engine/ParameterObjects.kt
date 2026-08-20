package com.batodev.arrows.engine

data class RegenerationParams(
    val forcedWidth: Int?,
    val forcedHeight: Int?,
    val forcedLives: Int?,
    val forcedShape: String?,
    val isCustomGame: Boolean = false,
    val onProgress: (Float) -> Unit,
    val onComplete: (GameLevel, LevelConfiguration) -> Unit,
)

data class LoSParams(
    val head: Point,
    val dir: Direction,
    val sId: Int,
    val grid: Array<IntArray>,
    val w: Int,
    val h: Int,
)
