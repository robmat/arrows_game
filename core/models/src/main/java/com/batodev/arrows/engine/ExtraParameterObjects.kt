package com.batodev.arrows.engine

data class SnakeRecursiveParams(
    val config: GameGeneratorConfig,
    val occupied: Array<BooleanArray>,
    val snakes: List<Snake>,
    val body: List<Point>,
    val forbidden: Set<Point>,
    val criterion: Criterion,
    val prevDir: Direction?,
)

data class TapParams(
    val snake: Snake,
    val isVibrationEnabled: Boolean,
    val obstructed: Boolean,
    val lives: Int,
    val onPenalty: () -> Unit,
    val onSuccess: () -> Unit,
)
