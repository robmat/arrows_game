package com.batodev.arrows.engine

data class GameGeneratorConfig(
    val width: Int,
    val height: Int,
    val maxSnakeLength: Int,
    val fillTheBoard: Boolean,
    val walls: Array<BooleanArray>,
)

data class GenerationContext(
    val config: GameGeneratorConfig,
    val occupied: Array<BooleanArray>,
    val snakes: MutableList<Snake>,
    val frontierCandidates: MutableSet<Pair<Point, Direction>>,
)
