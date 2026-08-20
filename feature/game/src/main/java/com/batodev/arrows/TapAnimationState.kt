package com.batodev.arrows

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class TapAnimationState(
    val id: Long,
    val offset: Offset,
)

@Composable
fun TapRipple(
    offset: Offset,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    val currentOnFinish by rememberUpdatedState(onFinish)

    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(GameConstants.RIPPLE_DURATION))
        currentOnFinish()
    }

    val value = progress.value
    // Draw a small expanding circle that fades out at the exact tap location
    // Max radius 40px, fades to alpha 0

    Canvas(modifier = modifier.fillMaxSize()) {
        val radius = GameConstants.RIPPLE_MAX_RADIUS * value
        drawCircle(
            color = Color.White.copy(alpha = 1f - value),
            radius = radius,
            center = offset,
        )
    }
}
