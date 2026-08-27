package com.batodev.arrows

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.batodev.arrows.engine.Direction
import com.batodev.arrows.engine.GameLevel
import com.batodev.arrows.engine.Point
import com.batodev.arrows.engine.Snake
import com.batodev.arrows.feature.game.BuildConfig
import com.batodev.arrows.ui.theme.FlashingRed
import com.batodev.arrows.ui.theme.LightGray
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.ThemeColors
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// Snake drawing is split into several small, named steps (anim state, head geometry, arrow
// head, body) rather than a few long functions, which is what keeps each function under the
// LongMethod threshold; that naturally pushes the function count over TooManyFunctions.
@Suppress("TooManyFunctions")
object SkeinBoardRenderer {
    private const val TAIL_FRACTION_EPSILON = 0.001f
    private const val ENTRY_FADE_IN_MULTIPLIER = 2.5f

    /**
     * Renders the game board with all snakes, arrows, and interactive elements.
     *
     * This composable draws:
     * - Tap areas (light gray circles) showing where users can tap to select snake heads (debug only)
     * - Snake bodies with curved corners at turns
     * - Arrow heads pointing in the snake's direction
     * - Animations for snake removal (moving head, fading, shrinking tail)
     *
     * @param level The game level containing the grid dimensions and snakes to render
     * @param modifier Modifier to be applied to the Canvas
     * @param flashingSnakeId ID of a snake that should be rendered in red (e.g., when obstructed)
     * @param removalProgress Map of snake IDs to their removal animation progress (0.0 to 1.0).
     *                        0.0 = not started, 1.0 = fully removed
     */
    @Composable
    fun Board(
        level: GameLevel,
        modifier: Modifier = Modifier,
        flashingSnakeId: Int? = null,
        removalProgress: Map<Int, Float> = emptyMap(),
        entryProgress: Map<Int, Float> = emptyMap(),
        guidanceAlpha: Float = 0f,
    ) {
        val themeColors = LocalThemeColors.current
        val infiniteTransition = rememberInfiniteTransition(label = "flash")
        val flashPulseAlpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = GameConstants.FLASH_MIN_ALPHA,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis = GameConstants.FLASH_PULSE_DURATION,
                            easing = LinearEasing,
                        ),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "flashAlpha",
        )

        Canvas(modifier = modifier) {
            val metrics = calculateBoardMetrics(level, size)
            val leftOffset = (size.width - metrics.boardWidth) / 2
            val topOffset = (size.height - metrics.boardHeight) / 2

            if (BuildConfig.DRAW_DEBUG_STUFF) {
                drawDebugBorder(leftOffset, topOffset, metrics)
            }

            drawContext.canvas.save()
            drawContext.canvas.translate(leftOffset, topOffset)

            if (guidanceAlpha > 0f) {
                val guidanceConfig =
                    GuidanceLineConfig(
                        guidanceAlpha = guidanceAlpha,
                        accentColor = themeColors.accent,
                        totalWidth = size.width,
                        totalHeight = size.height,
                        leftOffset = leftOffset,
                        topOffset = topOffset,
                    )
                drawGuidanceLines(level, metrics, removalProgress, entryProgress, guidanceConfig)
            }

            if (BuildConfig.DRAW_DEBUG_STUFF) {
                drawDebugTapAreas(level, metrics)
            }

            val drawingParams =
                SnakeDrawingParams(
                    level = level,
                    metrics = metrics,
                    removalProgress = removalProgress,
                    entryProgress = entryProgress,
                    flashingSnakeId = flashingSnakeId,
                    flashPulseAlpha = flashPulseAlpha,
                    themeColors = themeColors,
                )
            drawSnakes(drawingParams)

            drawContext.canvas.restore()
        }
    }

    private fun calculateBoardMetrics(
        level: GameLevel,
        canvasSize: Size,
    ): BoardMetrics {
        val cellSize = min(canvasSize.width / level.width, canvasSize.height / level.height)
        val boardWidth = cellSize * level.width
        val boardHeight = cellSize * level.height

        return BoardMetrics(
            cellWidth = cellSize,
            cellHeight = cellSize,
            strokeWidth = cellSize * GameConstants.BOARD_STROKE_WIDTH_FACTOR,
            cornerRadius = cellSize * GameConstants.BOARD_CORNER_RADIUS_FACTOR,
            arrowHeadSize = cellSize * GameConstants.ARROW_HEAD_SIZE_FACTOR,
            moveDist =
                max(
                    canvasSize.width,
                    canvasSize.height,
                ) * GameConstants.SNAKE_MOVE_DIST_FACTOR,
            boardWidth = boardWidth,
            boardHeight = boardHeight,
        )
    }

    private fun DrawScope.drawDebugBorder(
        leftOffset: Float,
        topOffset: Float,
        metrics: BoardMetrics,
    ) {
        drawRect(
            color = Color.Gray,
            topLeft = Offset(leftOffset, topOffset),
            size = Size(metrics.boardWidth, metrics.boardHeight),
            style = Stroke(width = GameConstants.BOARD_BORDER_WIDTH),
        )
    }

    private fun DrawScope.drawGuidanceLines(
        level: GameLevel,
        metrics: BoardMetrics,
        removalProgress: Map<Int, Float>,
        entryProgress: Map<Int, Float>,
        config: GuidanceLineConfig,
    ) {
        level.snakes.forEach { snake ->
            if (removalProgress.containsKey(snake.id) || entryProgress.containsKey(snake.id)) return@forEach

            val head = snake.body.first()
            val headCx = head.x * metrics.cellWidth + metrics.cellWidth / 2
            val headCy = head.y * metrics.cellHeight + metrics.cellHeight / 2

            val fullEndPoint =
                when (snake.headDirection) {
                    Direction.UP -> {
                        Offset(headCx, -config.topOffset)
                    }

                    Direction.DOWN -> {
                        Offset(
                            headCx,
                            metrics.boardHeight + (config.totalHeight - metrics.boardHeight - config.topOffset),
                        )
                    }

                    Direction.LEFT -> {
                        Offset(-config.leftOffset, headCy)
                    }

                    Direction.RIGHT -> {
                        Offset(
                            metrics.boardWidth + (config.totalWidth - metrics.boardWidth - config.leftOffset),
                            headCy,
                        )
                    }
                }

            val endPoint =
                Offset(
                    x = headCx + (fullEndPoint.x - headCx) * config.guidanceAlpha,
                    y = headCy + (fullEndPoint.y - headCy) * config.guidanceAlpha,
                )

            val alphaFactor = GameConstants.GUIDANCE_LINE_ALPHA_FACTOR * config.guidanceAlpha
            drawLine(
                color = config.accentColor.copy(alpha = alphaFactor),
                start = Offset(headCx, headCy),
                end = endPoint,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect =
                    PathEffect.dashPathEffect(
                        floatArrayOf(GameConstants.GUIDANCE_DASH_ON, GameConstants.GUIDANCE_DASH_OFF),
                        0f,
                    ),
            )
        }
    }

    private fun DrawScope.drawDebugTapAreas(
        level: GameLevel,
        metrics: BoardMetrics,
    ) {
        level.snakes.forEach { snake ->
            val head = snake.body.first()
            val headCx = head.x * metrics.cellWidth + metrics.cellWidth / 2
            val headCy = head.y * metrics.cellHeight + metrics.cellHeight / 2
            val tapRadius = GameConstants.DEFAULT_TOLERANCE * metrics.cellWidth

            val tapOffsetX =
                headCx + snake.headDirection.dx * metrics.cellWidth * GameConstants.TAP_AREA_OFFSET_FACTOR
            val tapOffsetY =
                headCy + snake.headDirection.dy * metrics.cellHeight * GameConstants.TAP_AREA_OFFSET_FACTOR

            drawCircle(
                color = LightGray.copy(alpha = GameConstants.TAP_AREA_ALPHA),
                radius = tapRadius,
                center = Offset(tapOffsetX, tapOffsetY),
            )
        }
    }

    private data class SnakeAnimState(
        val p: Float,
        val shift: Float,
        val alpha: Float,
    )

    private fun snakeAnimState(
        snake: Snake,
        params: SnakeDrawingParams,
    ): SnakeAnimState {
        val entryP = params.entryProgress[snake.id]
        if (entryP != null) {
            // Entry: body unfurls from head outward, head stays in place
            return SnakeAnimState(
                p = 1f - entryP.coerceIn(0f, 1f),
                shift = 0f,
                alpha = min(entryP * ENTRY_FADE_IN_MULTIPLIER, 1f), // Quick fade-in
            )
        }
        // Normal / removal
        val removalP = (params.removalProgress[snake.id] ?: 0f).coerceIn(0f, 1f)
        return SnakeAnimState(p = removalP, shift = params.metrics.moveDist * removalP, alpha = 1f - removalP)
    }

    private fun snakeHeadCoordinates(
        snake: Snake,
        metrics: BoardMetrics,
        shift: Float,
    ): SnakeHeadCoordinates {
        val head = snake.body.first()
        val headCx0 = head.x * metrics.cellWidth + metrics.cellWidth / 2
        val headCy0 = head.y * metrics.cellHeight + metrics.cellHeight / 2
        val headCx = headCx0 + snake.headDirection.dx * shift
        val headCy = headCy0 + snake.headDirection.dy * shift
        return SnakeHeadCoordinates(
            headCx0 = headCx0,
            headCy0 = headCy0,
            baseLineEndX0 = headCx0 + snake.headDirection.dx * metrics.cornerRadius,
            baseLineEndY0 = headCy0 + snake.headDirection.dy * metrics.cornerRadius,
            lineEndX = headCx + snake.headDirection.dx * metrics.cornerRadius,
            lineEndY = headCy + snake.headDirection.dy * metrics.cornerRadius,
        )
    }

    private fun DrawScope.drawSnakeArrowHead(
        snake: Snake,
        metrics: BoardMetrics,
        headCoords: SnakeHeadCoordinates,
        snakeColor: Color,
    ) {
        val triangleCenterX =
            headCoords.lineEndX + snake.headDirection.dx *
                (metrics.arrowHeadSize * GameConstants.ARROW_HEAD_CENTER_FACTOR)
        val triangleCenterY =
            headCoords.lineEndY + snake.headDirection.dy *
                (metrics.arrowHeadSize * GameConstants.ARROW_HEAD_CENTER_FACTOR)

        drawArrowHead(
            centerX = triangleCenterX,
            centerY = triangleCenterY,
            direction = snake.headDirection,
            arrowHeadSize = metrics.arrowHeadSize,
            color = snakeColor,
        )
    }

    private fun DrawScope.drawSnake(
        snake: Snake,
        params: SnakeDrawingParams,
    ) {
        val entryP = params.entryProgress[snake.id]
        val anim = snakeAnimState(snake, params)

        val isFlashing = snake.id == params.flashingSnakeId
        val baseColor = if (isFlashing) FlashingRed else params.themeColors.snake
        val animatedAlpha = if (isFlashing) anim.alpha * params.flashPulseAlpha else anim.alpha
        val snakeColor = baseColor.copy(alpha = animatedAlpha)

        val headCoords = snakeHeadCoordinates(snake, params.metrics, anim.shift)

        if (snake.body.size > 1) {
            drawSnakeBody(snake, anim.p, params.metrics, headCoords, snakeColor)
        }

        if (snake.body.size == 1) {
            drawSingleBlockSnakeTail(snake, params.metrics, headCoords.lineEndX, headCoords.lineEndY, snakeColor)
        }

        // Draw arrow head
        if (entryP == null) {
            drawSnakeArrowHead(snake, params.metrics, headCoords, snakeColor)
        }
    }

    private fun DrawScope.drawSnakes(params: SnakeDrawingParams) {
        params.level.snakes.forEach { snake -> drawSnake(snake, params) }
    }

    private fun DrawScope.drawSnakeBody(
        snake: Snake,
        p: Float,
        metrics: BoardMetrics,
        headCoords: SnakeHeadCoordinates,
        snakeColor: Color,
    ) {
        val path = Path()
        val body = snake.body

        // Float position of the tail tip along the body:
        //   p=0 → tailPosition = body.size-1 (full tail rendered)
        //   p=1 → tailPosition = 0 (tail shrunk all the way to head)
        val tailPosition = (body.size - 1).toFloat() * (1f - p)
        val tailFullIndex = tailPosition.toInt().coerceIn(0, body.size - 1)
        val tailFraction = tailPosition - tailFullIndex

        // Pixel position of the tail tip — linearly interpolated between the two adjacent
        // cell centres so the tail end moves sub-cell each frame instead of jumping.
        val tailTip = calculateTailTipOffset(body, tailFullIndex, tailFraction, metrics)
        path.moveTo(tailTip.x, tailTip.y)

        // When the tail tip sits within an interior segment (fractional offset), include
        // body[tailFullIndex] in the curve loop so the path bends correctly through it.
        val loopStart =
            if (tailFraction > TAIL_FRACTION_EPSILON && tailFullIndex in 1 until body.size - 1) {
                tailFullIndex
            } else {
                tailFullIndex - 1
            }

        path.appendBodyCurves(body, loopStart, metrics)

        val head = body[0]
        val prev = body[1]
        val headEntryX =
            headCoords.headCx0 + (prev.x - head.x).coerceIn(-1, 1) * metrics.cornerRadius
        val headEntryY =
            headCoords.headCy0 + (prev.y - head.y).coerceIn(-1, 1) * metrics.cornerRadius

        path.lineTo(headEntryX, headEntryY)
        path.quadraticTo(
            headCoords.headCx0,
            headCoords.headCy0,
            headCoords.baseLineEndX0,
            headCoords.baseLineEndY0,
        )

        // If removing (head is shifting), extend line to shifted head position
        if (headCoords.lineEndX != headCoords.baseLineEndX0 || headCoords.lineEndY != headCoords.baseLineEndY0) {
            path.lineTo(headCoords.lineEndX, headCoords.lineEndY)
        }

        drawPath(
            path = path,
            color = snakeColor,
            style =
                Stroke(
                    width = metrics.strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
        )
    }

    private fun calculateTailTipOffset(
        body: List<Point>,
        tailFullIndex: Int,
        tailFraction: Float,
        metrics: BoardMetrics,
    ): Offset =
        if (tailFraction < TAIL_FRACTION_EPSILON || tailFullIndex >= body.size - 1) {
            val cell = body[tailFullIndex]
            Offset(
                cell.x * metrics.cellWidth + metrics.cellWidth / 2,
                cell.y * metrics.cellHeight + metrics.cellHeight / 2,
            )
        } else {
            val fromCell = body[tailFullIndex]
            val toCell = body[tailFullIndex + 1]
            val fromCx = fromCell.x * metrics.cellWidth + metrics.cellWidth / 2
            val fromCy = fromCell.y * metrics.cellHeight + metrics.cellHeight / 2
            val toCx = toCell.x * metrics.cellWidth + metrics.cellWidth / 2
            val toCy = toCell.y * metrics.cellHeight + metrics.cellHeight / 2
            Offset(
                fromCx + (toCx - fromCx) * tailFraction,
                fromCy + (toCy - fromCy) * tailFraction,
            )
        }

    private fun Path.appendBodyCurves(
        body: List<Point>,
        loopStart: Int,
        metrics: BoardMetrics,
    ) {
        for (i in loopStart downTo 1) {
            val prev = body[i + 1]
            val current = body[i]
            val next = body[i - 1]

            val currX = current.x * metrics.cellWidth + metrics.cellWidth / 2
            val currY = current.y * metrics.cellHeight + metrics.cellHeight / 2

            val entryX = currX + (prev.x - current.x).coerceIn(-1, 1) * metrics.cornerRadius
            val entryY = currY + (prev.y - current.y).coerceIn(-1, 1) * metrics.cornerRadius

            val exitX = currX + (next.x - current.x).coerceIn(-1, 1) * metrics.cornerRadius
            val exitY = currY + (next.y - current.y).coerceIn(-1, 1) * metrics.cornerRadius

            lineTo(entryX, entryY)
            quadraticTo(currX, currY, exitX, exitY)
        }
    }

    private fun DrawScope.drawSingleBlockSnakeTail(
        snake: Snake,
        metrics: BoardMetrics,
        lineEndX: Float,
        lineEndY: Float,
        snakeColor: Color,
    ) {
        val tailLength = metrics.cellWidth * GameConstants.SINGLE_BLOCK_TAIL_FACTOR
        val tailStartX = lineEndX - snake.headDirection.dx * (tailLength + metrics.cornerRadius)
        val tailStartY = lineEndY - snake.headDirection.dy * (tailLength + metrics.cornerRadius)

        drawLine(
            color = snakeColor,
            start = Offset(tailStartX, tailStartY),
            end = Offset(lineEndX, lineEndY),
            strokeWidth = metrics.strokeWidth,
            cap = StrokeCap.Round,
        )
    }

    private data class BoardMetrics(
        val cellWidth: Float,
        val cellHeight: Float,
        val strokeWidth: Float,
        val cornerRadius: Float,
        val arrowHeadSize: Float,
        val moveDist: Float,
        val boardWidth: Float,
        val boardHeight: Float,
    )

    private data class SnakeDrawingParams(
        val level: GameLevel,
        val metrics: BoardMetrics,
        val removalProgress: Map<Int, Float>,
        val entryProgress: Map<Int, Float>,
        val flashingSnakeId: Int?,
        val flashPulseAlpha: Float,
        val themeColors: ThemeColors,
    )

    private data class GuidanceLineConfig(
        val guidanceAlpha: Float,
        val accentColor: Color,
        val totalWidth: Float,
        val totalHeight: Float,
        val leftOffset: Float,
        val topOffset: Float,
    )

    private data class SnakeHeadCoordinates(
        val headCx0: Float,
        val headCy0: Float,
        val baseLineEndX0: Float,
        val baseLineEndY0: Float,
        val lineEndX: Float,
        val lineEndY: Float,
    )

    /**
     * Draws an equilateral triangle arrow head pointing in the specified direction.
     *
     * The arrow head is drawn as a filled triangle with a stroke outline. The triangle
     * has equal sides (120-degree angles) with rounded corners for a softer appearance.
     *
     * @param centerX X-coordinate of the arrow head center (inscribed circle center)
     * @param centerY Y-coordinate of the arrow head center (inscribed circle center)
     * @param direction The direction the arrow should point (UP, DOWN, LEFT, or RIGHT)
     * @param arrowHeadSize The radius of the inscribed circle (distance from center to vertices)
     * @param color The color to draw the arrow head (respects alpha for fade animations)
     */
    private fun DrawScope.drawArrowHead(
        centerX: Float,
        centerY: Float,
        direction: Direction,
        arrowHeadSize: Float,
        color: Color,
    ) {
        // Convert direction to angle in radians (0° = RIGHT, 90° = DOWN, etc.)
        val angle =
            when (direction) {
                Direction.UP -> GameConstants.ANGLE_UP
                Direction.DOWN -> GameConstants.ANGLE_DOWN
                Direction.LEFT -> GameConstants.ANGLE_LEFT
                Direction.RIGHT -> GameConstants.ANGLE_RIGHT
            } * GameConstants.DEG_TO_RAD

        // Create triangular path with three vertices equally spaced around the center
        val path =
            Path().apply {
                // First vertex (points in the arrow direction)
                moveTo(
                    centerX + (arrowHeadSize * cos(angle)).toFloat(),
                    centerY + (arrowHeadSize * sin(angle)).toFloat(),
                )
                // Second vertex (120 degrees clockwise)
                lineTo(
                    centerX + (arrowHeadSize * cos(angle + GameConstants.ANGLE_TRIANGLE_OFFSET)).toFloat(),
                    centerY + (arrowHeadSize * sin(angle + GameConstants.ANGLE_TRIANGLE_OFFSET)).toFloat(),
                )
                // Third vertex (120 degrees counter-clockwise)
                lineTo(
                    centerX + (arrowHeadSize * cos(angle - GameConstants.ANGLE_TRIANGLE_OFFSET)).toFloat(),
                    centerY + (arrowHeadSize * sin(angle - GameConstants.ANGLE_TRIANGLE_OFFSET)).toFloat(),
                )
                close()
            }

        // Draw filled triangle
        drawPath(path, color)

        // Draw outline stroke for better definition and rounded corners
        drawPath(
            path = path,
            color = color,
            style =
                Stroke(
                    width = arrowHeadSize * GameConstants.ARROW_HEAD_STROKE_WIDTH_FACTOR,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
        )
    }
}
