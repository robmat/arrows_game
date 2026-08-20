package com.batodev.arrows.engine

import com.batodev.arrows.GameConstants
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class GameGenerator {
    var straightPreference: Float = GameConstants.DEFAULT_STRAIGHT_PREFERENCE
        set(value) {
            require(value in 0f..1f) { "straightPreference must be in [0, 1]" }
            field = value
            snakeBuilder = SnakeBuilder(ids, rnd, value)
        }

    private val ids = AtomicInteger(0)
    private val rnd = Random(System.currentTimeMillis())
    private var snakeBuilder = SnakeBuilder(ids, rnd, straightPreference)

    fun generateSolvableLevel(params: GenerationParams): GameLevel {
        val width =
            if (params.fillTheBoard) {
                params.width.coerceAtMost(GameConstants.MAX_FILL_BOARD_SIZE)
            } else {
                params.width
            }
        val height =
            if (params.fillTheBoard) {
                params.height.coerceAtMost(GameConstants.MAX_FILL_BOARD_SIZE)
            } else {
                params.height
            }

        val walls =
            params.boardShape?.getWalls(width, height)
                ?: Array(width) { BooleanArray(height) }

        val config =
            GameGeneratorConfig(
                width,
                height,
                params.maxSnakeLength,
                params.fillTheBoard,
                walls,
            )
        val context =
            GenerationContext(
                config,
                Array(width) { BooleanArray(height) },
                mutableListOf(),
                mutableSetOf(),
            )

        val totalCells = GenerationUtils.countValidCells(width, height, walls)
        generateInitialSnakes(context, totalCells, params.onProgress)

        if (params.fillTheBoard) fillRemainingBoard(context, totalCells, params.onProgress)

        return GameLevel(width, height, context.snakes)
    }

    private fun generateInitialSnakes(
        context: GenerationContext,
        totalCells: Int,
        onProgress: (Float) -> Unit,
    ) {
        var snake = snakeBuilder.buildFirstSnake(context.config, context.occupied)
        while (snake != null) {
            addSnakeToContext(context, snake)
            onProgress(calculateProgress(context.snakes, totalCells))
            snake = snakeBuilder.buildNextSnake(context)
        }
    }

    private fun addSnakeToContext(
        context: GenerationContext,
        snake: Snake,
    ) {
        context.snakes.add(snake)
        snake.body.forEach { context.occupied[it.x][it.y] = true }
        snake.body.forEach { p ->
            Direction.entries.forEach { context.frontierCandidates.remove(Pair(p, it)) }
        }
        updateFrontierWithSnake(context, snake)
    }

    private fun updateFrontierWithSnake(
        context: GenerationContext,
        snake: Snake,
    ) {
        snake.body.forEach { segment ->
            Direction.entries.forEach { dir ->
                val neighbor = segment + dir
                if (isFreeAt(neighbor, context.occupied, context.config)) {
                    addFrontierCandidatesForPoint(context, neighbor)
                }
            }
        }
    }

    private fun addFrontierCandidatesForPoint(
        context: GenerationContext,
        p: Point,
    ) {
        Direction.entries.forEach { headDir ->
            val hasLoS =
                GenerationUtils.hasClearLoS(
                    p,
                    headDir,
                    context.occupied,
                    context.config.width,
                    context.config.height,
                )
            if (hasLoS) {
                context.frontierCandidates.add(Pair(p, headDir))
            }
        }
    }

    private fun fillRemainingBoard(
        context: GenerationContext,
        totalCells: Int,
        onProgress: (Float) -> Unit,
    ) {
        var lastSnake = snakeBuilder.buildLastSnake(context)
        while (lastSnake != null) {
            context.snakes.add(lastSnake)
            onProgress(calculateProgress(context.snakes, totalCells))
            lastSnake.body.forEach { context.occupied[it.x][it.y] = true }
            lastSnake = snakeBuilder.buildLastSnake(context)
        }
    }

    private fun isFreeAt(
        p: Point,
        occupied: Array<BooleanArray>,
        config: GameGeneratorConfig,
    ): Boolean =
        GenerationUtils.isInside(p, config.width, config.height) &&
            !occupied[p.x][p.y] && !config.walls[p.x][p.y]

    private fun calculateProgress(
        snakes: List<Snake>,
        totalCells: Int,
    ): Float = (snakes.sumOf { it.body.size }.toFloat() / totalCells).coerceIn(0f, GameConstants.PROGRESS_FACTOR)
}
