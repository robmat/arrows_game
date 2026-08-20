package com.batodev.arrows.ui.game

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.batodev.arrows.GameConstants
import com.batodev.arrows.engine.GameLevel
import com.batodev.arrows.engine.SolvabilityChecker

private val FINGER_SIZE: Dp = 120.dp

@Composable
fun IntroFingerOverlay(
    level: GameLevel,
    modifier: Modifier = Modifier,
) {
    val removableId = SolvabilityChecker.findRemovableSnake(level)
    val snake = level.snakes.firstOrNull { it.id == removableId }
    val head = snake?.body?.firstOrNull()
    if (snake == null || head == null) return

    val transition = rememberInfiniteTransition(label = "introFlash")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 600),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "fingerAlpha",
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val cellSizeW = maxWidth / level.width
        val cellSizeH = maxHeight / level.height
        val cellSize: Dp = if (cellSizeW < cellSizeH) cellSizeW else cellSizeH

        val boardWidth = cellSize * level.width
        val boardHeight = cellSize * level.height
        val leftOffset = (maxWidth - boardWidth) / 2 + 4.dp
        val topOffset = (maxHeight - boardHeight) / 2 + 25.dp

        // Arrowhead center, matching TAP_AREA_OFFSET_FACTOR used by the renderer
        val arrowHeadX: Dp =
            leftOffset + cellSize * head.x + cellSize / 2 +
                cellSize * snake.headDirection.dx * GameConstants.TAP_AREA_OFFSET_FACTOR
        val arrowHeadY: Dp =
            topOffset + cellSize * head.y + cellSize / 2 +
                cellSize * snake.headDirection.dy * GameConstants.TAP_AREA_OFFSET_FACTOR

        Icon(
            imageVector = Icons.Filled.TouchApp,
            contentDescription = null,
            tint = Color.White,
            modifier =
                Modifier
                    .size(FINGER_SIZE)
                    .offset(x = arrowHeadX - FINGER_SIZE / 2, y = arrowHeadY - FINGER_SIZE / 2)
                    .graphicsLayer { this.alpha = alpha },
        )
    }
}
