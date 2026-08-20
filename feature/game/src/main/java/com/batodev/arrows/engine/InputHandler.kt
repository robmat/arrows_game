package com.batodev.arrows.engine

import androidx.compose.ui.geometry.Offset
import com.batodev.arrows.GameConstants
import com.batodev.arrows.GameConstants.DEFAULT_TOLERANCE
import com.batodev.arrows.GameConstants.TAP_AREA_OFFSET_FACTOR
import kotlin.math.min

class InputHandler {
    fun transformTapToGrid(params: TapTransformationParams): Offset {
        // Inverse graphicsLayer transformation
        val centerX = params.containerWidth / 2
        val centerY = params.containerHeight / 2
        val transformedX = (params.tapOffset.x - params.offsetX - centerX) / params.scale + centerX
        val transformedY = (params.tapOffset.y - params.offsetY - centerY) / params.scale + centerY

        // Centered board bounds
        val cellSize = min(params.containerWidth / params.level.width, params.containerHeight / params.level.height)
        val boardWidth = cellSize * params.level.width
        val boardHeight = cellSize * params.level.height
        val leftOffset = (params.containerWidth - boardWidth) / 2
        val topOffset = (params.containerHeight - boardHeight) / 2

        // Grid coordinates
        val cellX = (transformedX - leftOffset) / cellSize
        val cellY = (transformedY - topOffset) / cellSize

        return Offset(cellX, cellY)
    }

    fun findTappedSnake(
        cellX: Float,
        cellY: Float,
        snakes: List<Snake>,
        isObstructed: (Snake) -> Boolean,
    ): Snake? =
        snakes
            .map { snake ->
                val head = snake.body.first()
                val cellOffset = GameConstants.CELL_CENTER + snake.headDirection.dx * TAP_AREA_OFFSET_FACTOR
                val tapAreaCenterX = head.x + cellOffset
                val cellOffsetY = GameConstants.CELL_CENTER + snake.headDirection.dy * TAP_AREA_OFFSET_FACTOR
                val tapAreaCenterY = head.y + cellOffsetY

                val dx = tapAreaCenterX - cellX
                val dy = tapAreaCenterY - cellY
                val distSq = dx * dx + dy * dy

                Triple(snake, distSq, isObstructed(snake))
            }.filter { it.second <= DEFAULT_TOLERANCE * DEFAULT_TOLERANCE }
            .minWithOrNull(compareBy({ it.third }, { it.second }))
            ?.first
}
