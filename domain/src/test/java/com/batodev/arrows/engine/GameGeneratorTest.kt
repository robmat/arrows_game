package com.batodev.arrows.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

private const val TEST_WIDTH = 20
private const val TEST_HEIGHT = 20
private const val TEST_SNAKE_LEN = 7
private const val SIMULATIONS_OCCUPIED = 10
private const val SIMULATIONS_SOLVABLE = 50

class GameGeneratorTest {
    @get:Rule
    val timeout: Timeout = Timeout(180, TimeUnit.SECONDS)

    @Test
    fun testEverySpotOccupiedAfterGenerationParallel() =
        runBlocking {
            val generator = GameGenerator()
            val failures = AtomicInteger(0)
            val dispatcher =
                Dispatchers.Default.limitedParallelism(
                    Runtime.getRuntime().availableProcessors(),
                )

            (1..SIMULATIONS_OCCUPIED)
                .map { _ ->
                    async(dispatcher) {
                        val params = GenerationParams(TEST_WIDTH, TEST_HEIGHT, TEST_SNAKE_LEN, true)
                        val level = generator.generateSolvableLevel(params)
                        val occupiedCount = level.snakes.sumOf { it.body.size }

                        if (occupiedCount != TEST_WIDTH * TEST_HEIGHT) {
                            failures.incrementAndGet()
                        }
                    }
                }.awaitAll()

            assertEquals("Some simulations failed: Not all spots occupied", 0, failures.get())
        }

    @Test
    fun testEveryPuzzleIsSolvable() =
        runBlocking {
            val generator = GameGenerator()
            val failures = AtomicInteger(0)
            val dispatcher =
                Dispatchers.Default.limitedParallelism(
                    Runtime.getRuntime().availableProcessors(),
                )

            (1..SIMULATIONS_SOLVABLE)
                .map { _ ->
                    async(dispatcher) {
                        val params = GenerationParams(TEST_WIDTH, TEST_HEIGHT, TEST_SNAKE_LEN)
                        val level = generator.generateSolvableLevel(params)
                        if (!SolvabilityChecker.isResolvable(level)) {
                            failures.incrementAndGet()
                        }
                    }
                }.awaitAll()

            assertEquals("Some puzzles were not solvable", 0, failures.get())
        }

    @Test
    fun testSequentialGenerationWithRandomShapesAndTiming() =
        runBlocking {
            val generator = GameGenerator()
            val shapeProvider = TestBoardShapeProvider()
            val numLevels = 10
            val levels = mutableListOf<GameLevel>()
            val times = mutableListOf<Long>()

            // Generate 10 levels one by one
            for (i in 1..numLevels) {
                val randomShape = shapeProvider.getRandomShape()
                val params = GenerationParams(TEST_WIDTH, TEST_HEIGHT, TEST_SNAKE_LEN, true, randomShape)

                val startTime = System.nanoTime()
                val level = generator.generateSolvableLevel(params)
                val endTime = System.nanoTime()

                val durationMs = (endTime - startTime) / 1_000_000
                times.add(durationMs)
                levels.add(level)

                println("Level $i generated in $durationMs ms")
            }

            // Calculate mean time
            val meanTime = times.average()
            println("Mean generation time: ${"%.2f".format(meanTime)} ms")

            // Check each level for solvability
            var solvableCount = 0
            levels.forEachIndexed { index, level ->
                if (SolvabilityChecker.isResolvable(level)) {
                    solvableCount++
                } else {
                    println("Level ${index + 1} is NOT solvable")
                }
            }

            println("$solvableCount out of $numLevels levels are solvable")
            assertEquals("All levels should be solvable", numLevels, solvableCount)
            assertTrue("Mean generation time should be reasonable (< 10 seconds)", meanTime < 10_000)
        }
}

/**
 * Simple test implementation of BoardShapeProvider that generates random shapes
 */
private class TestBoardShapeProvider : BoardShapeProvider {
    private val random = Random.Default

    override fun getRandomShape(): BoardShape {
        // Generate random shapes: circle, diamond, rectangle, or cross
        return when (random.nextInt(5)) {
            0 -> CircleShape()
            1 -> DiamondShape()
            2 -> RectangleShape()
            3 -> CrossShape()
            else -> null // null means no walls (square board)
        } ?: object : BoardShape {
            override fun getWalls(
                targetWidth: Int,
                targetHeight: Int,
            ): Array<BooleanArray> = Array(targetWidth) { BooleanArray(targetHeight) { false } }
        }
    }

    override fun getAllShapeNames(): List<String> = listOf("circle", "diamond", "rectangle", "cross", "square")

    override fun getShapeByName(name: String): BoardShape? = null

    // Circle shape
    private class CircleShape : BoardShape {
        override fun getWalls(
            targetWidth: Int,
            targetHeight: Int,
        ): Array<BooleanArray> {
            val walls = Array(targetWidth) { BooleanArray(targetHeight) { false } }
            val centerX = targetWidth / 2.0
            val centerY = targetHeight / 2.0
            val radius = minOf(targetWidth, targetHeight) / 2.0

            for (x in 0 until targetWidth) {
                for (y in 0 until targetHeight) {
                    val dx = x - centerX
                    val dy = y - centerY
                    if (dx * dx + dy * dy > radius * radius) {
                        walls[x][y] = true
                    }
                }
            }
            return walls
        }
    }

    // Diamond shape
    private class DiamondShape : BoardShape {
        override fun getWalls(
            targetWidth: Int,
            targetHeight: Int,
        ): Array<BooleanArray> {
            val walls = Array(targetWidth) { BooleanArray(targetHeight) { false } }
            val centerX = targetWidth / 2.0
            val centerY = targetHeight / 2.0

            for (x in 0 until targetWidth) {
                for (y in 0 until targetHeight) {
                    val dx = kotlin.math.abs(x - centerX)
                    val dy = kotlin.math.abs(y - centerY)
                    if (dx + dy > minOf(centerX, centerY)) {
                        walls[x][y] = true
                    }
                }
            }
            return walls
        }
    }

    // Rectangle shape (leaves border as walls)
    private class RectangleShape : BoardShape {
        override fun getWalls(
            targetWidth: Int,
            targetHeight: Int,
        ): Array<BooleanArray> {
            val walls = Array(targetWidth) { BooleanArray(targetHeight) { false } }
            val border = 2

            for (x in 0 until targetWidth) {
                for (y in 0 until targetHeight) {
                    val isBorderX = x < border || x >= targetWidth - border
                    val isBorderY = y < border || y >= targetHeight - border
                    if (isBorderX || isBorderY) {
                        walls[x][y] = true
                    }
                }
            }
            return walls
        }
    }

    // Cross shape
    private class CrossShape : BoardShape {
        override fun getWalls(
            targetWidth: Int,
            targetHeight: Int,
        ): Array<BooleanArray> {
            val walls = Array(targetWidth) { BooleanArray(targetHeight) { true } }
            val centerX = targetWidth / 2
            val centerY = targetHeight / 2
            val armWidth = targetWidth / 4

            // Horizontal arm
            for (x in 0 until targetWidth) {
                for (y in (centerY - armWidth).coerceAtLeast(0) until (centerY + armWidth).coerceAtMost(targetHeight)) {
                    walls[x][y] = false
                }
            }

            // Vertical arm
            for (y in 0 until targetHeight) {
                for (x in (centerX - armWidth).coerceAtLeast(0) until (centerX + armWidth).coerceAtMost(targetWidth)) {
                    walls[x][y] = false
                }
            }

            return walls
        }
    }
}
