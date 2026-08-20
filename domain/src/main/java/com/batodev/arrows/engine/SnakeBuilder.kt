package com.batodev.arrows.engine

import com.batodev.arrows.GameConstants
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class SnakeBuilder(
    private val ids: AtomicInteger,
    private val rnd: Random,
    private var straightPreference: Float,
) {
    fun buildFirstSnake(
        config: GameGeneratorConfig,
        occupied: Array<BooleanArray>,
    ): Snake? {
        var head: Point
        var attempts = 0
        do {
            head = Point(rnd.nextInt(config.width), rnd.nextInt(config.height))
            if (attempts++ > GameConstants.FIRST_SNAKE_MAX_ATTEMPTS) return null
        } while (config.walls[head.x][head.y])

        val direction = Direction.entries.random(rnd)
        val forbidden = GenerationUtils.forbiddenPoints(head, direction, config.width, config.height)
        val params =
            SnakeRecursiveParams(
                config,
                occupied,
                mutableListOf(),
                listOf(head),
                forbidden,
                AlwaysTrueCriterion(),
                null,
            )
        val body = buildSnakeRecursive(params)
        return Snake(ids.incrementAndGet(), body, direction)
    }

    fun buildNextSnake(context: GenerationContext): Snake? {
        val candidates = context.frontierCandidates.toList().shuffled(rnd)
        var bestSnake: Snake? = null
        for ((head, direction) in candidates) {
            val snake = tryBuildNextSnake(context, head, direction) ?: continue
            if (snake.body.size >= context.config.maxSnakeLength) return snake
            if (bestSnake == null || snake.body.size > bestSnake.body.size) {
                bestSnake = snake
            }
        }
        return bestSnake
    }

    private fun tryBuildNextSnake(
        ctx: GenerationContext,
        head: Point,
        dir: Direction,
    ): Snake? {
        val isFree = GenerationUtils.isFreeAt(head, ctx.occupied, ctx.config)
        val hasLoS =
            isFree &&
                GenerationUtils.hasClearLoS(
                    head,
                    dir,
                    ctx.occupied,
                    ctx.config.width,
                    ctx.config.height,
                )

        return if (hasLoS) {
            val forbidden = GenerationUtils.forbiddenPoints(head, dir, ctx.config.width, ctx.config.height)
            val params =
                SnakeRecursiveParams(
                    ctx.config,
                    ctx.occupied,
                    ctx.snakes,
                    listOf(head),
                    forbidden,
                    NextToExistingSnakeCriterion(),
                    null,
                )
            Snake(ids.incrementAndGet(), buildSnakeRecursive(params), dir)
        } else {
            null
        }
    }

    fun buildLastSnake(context: GenerationContext): Snake? {
        val criterion = NextToExistingSnakeCriterion()
        val candidates = getFreeCandidates(context, criterion)

        return candidates
            .asSequence()
            .filter { (head, _) -> !context.config.walls[head.x][head.y] }
            .mapNotNull { (head, dir) -> tryBuildBestSnake(context, head, dir, criterion) }
            .firstOrNull { it.body.size >= context.config.maxSnakeLength }
            ?: findAnyResolvableSnake(context, candidates, criterion)
    }

    private fun getFreeCandidates(
        context: GenerationContext,
        crit: Criterion,
    ): List<Pair<Point, Direction>> =
        (0 until context.config.width)
            .flatMap { x ->
                (0 until context.config.height).filter { y -> !context.occupied[x][y] }.map { y -> Point(x, y) }
            }.filter { point ->
                val p =
                    CriterionParams(
                        emptyList(),
                        point,
                        context.snakes,
                        context.config.width,
                        context.config.height,
                        emptySet(),
                        context.occupied,
                    )
                crit.isSatisfied(p)
            }.flatMap { point -> Direction.entries.map { Pair(point, it) } }

    private fun tryBuildBestSnake(
        ctx: GenerationContext,
        head: Point,
        dir: Direction,
        crit: Criterion,
    ): Snake? {
        val forbidden = GenerationUtils.forbiddenPoints(head, dir, ctx.config.width, ctx.config.height)
        val params =
            SnakeRecursiveParams(
                ctx.config,
                ctx.occupied,
                ctx.snakes,
                listOf(head),
                forbidden,
                crit,
                null,
            )
        val body = buildSnakeRecursive(params)
        val snake = Snake(ids.incrementAndGet(), body, dir)
        val level = GameLevel(ctx.config.width, ctx.config.height, ctx.snakes + snake)
        return if (SolvabilityChecker.isResolvable(level)) {
            snake
        } else {
            null
        }
    }

    private fun findAnyResolvableSnake(
        ctx: GenerationContext,
        cands: List<Pair<Point, Direction>>,
        crit: Criterion,
    ): Snake? =
        cands
            .asSequence()
            .filter { (head, _) -> !ctx.config.walls[head.x][head.y] }
            .mapNotNull { (head, dir) -> tryBuildBestSnake(ctx, head, dir, crit) }
            .maxByOrNull { it.body.size }

    private fun buildSnakeRecursive(params: SnakeRecursiveParams): List<Point> {
        if (params.body.size >= params.config.maxSnakeLength) return params.body
        val tail = params.body.last()
        val possible =
            Direction.entries.shuffled(rnd).filter { dir ->
                canPlaceSegment(params, tail + dir)
            }

        return if (possible.isEmpty()) {
            params.body
        } else {
            findBestRecursiveSnake(params, possible)
        }
    }

    private fun findBestRecursiveSnake(
        params: SnakeRecursiveParams,
        possible: List<Direction>,
    ): List<Point> {
        val tail = params.body.last()
        val ordered =
            GenerationUtils.getOrderedDirections(
                possible,
                params.prevDir,
                straightPreference,
                rnd,
            )
        var best = params.body
        for (direction in ordered) {
            val nextParams = params.copy(body = params.body + (tail + direction), prevDir = direction)
            val candidate = buildSnakeRecursive(nextParams)
            if (candidate.size >= params.config.maxSnakeLength) return candidate
            if (candidate.size > best.size) best = candidate
        }
        return best
    }

    private fun canPlaceSegment(
        params: SnakeRecursiveParams,
        next: Point,
    ): Boolean {
        val isInside = GenerationUtils.isInside(next, params.config.width, params.config.height)
        if (!isInside) return false

        val isBasicFree =
            next !in params.forbidden && next !in params.body &&
                !params.config.walls[next.x][next.y] && !params.occupied[next.x][next.y]

        return if (isBasicFree) {
            val cParams =
                CriterionParams(
                    params.body,
                    next,
                    params.snakes,
                    params.config.width,
                    params.config.height,
                    params.forbidden,
                    params.occupied,
                )
            params.criterion.isSatisfied(cParams)
        } else {
            false
        }
    }
}
