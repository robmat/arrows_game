package com.batodev.arrows.engine

import androidx.compose.ui.geometry.Offset

data class TapTransformationParams(
    val tapOffset: Offset,
    val containerWidth: Float,
    val containerHeight: Float,
    val level: GameLevel,
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
)
