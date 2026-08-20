package com.batodev.arrows.engine

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class GameGeneratorShapeTest {
    private val boardWidth = 40
    private val boardHeight = 40
    private val maxSnakeLen = 10

    @Test
    fun testGenerateLevelWithHeartShapeAndPrintAscii() {
        val possiblePaths =
            listOf(
                "../core/resources/src/main/res/drawable-nodpi/favorite_256dp_000000_fill1_wght400_grad0_opsz48.png",
                "core/resources/src/main/res/drawable-nodpi/favorite_256dp_000000_fill1_wght400_grad0_opsz48.png",
                "app/src/main/res/drawable-nodpi/favorite_256dp_000000_fill1_wght400_grad0_opsz48.png",
                "src/main/res/drawable-nodpi/favorite_256dp_000000_fill1_wght400_grad0_opsz48.png",
            )
        val path =
            possiblePaths.find { java.io.File(it).exists() }
                ?: throw java.io.FileNotFoundException("Could not find heart shape image in $possiblePaths")

        val shape = JvmBoardShape.fromFile(path)

        val generator = GameGenerator()
        val params = GenerationParams(boardWidth, boardHeight, maxSnakeLen, true, shape)
        val level = generator.generateSolvableLevel(params)

        val walls = shape.getWalls(level.width, level.height)

        printLevelAscii(level, walls)
        validateSnakesOnNonWallCells(level, walls)
    }

    private fun printLevelAscii(
        level: GameLevel,
        walls: Array<BooleanArray>,
    ) {
        val grid = Array(level.width) { IntArray(level.height) }
        val snakeMap = level.snakes.associateBy { it.id }
        level.snakes.forEach { s -> s.body.forEach { p -> grid[p.x][p.y] = s.id } }

        println("\nBoard Size: ${level.width} x ${level.height}\nNumber of Snakes: ${level.snakes.size}\n")
        for (y in 0 until level.height) {
            for (x in 0 until level.width) {
                printCell(x, y, grid[x][y], walls[x][y], snakeMap)
            }
            println()
        }
    }

    private fun printCell(
        x: Int,
        y: Int,
        snakeId: Int,
        isWall: Boolean,
        snakeMap: Map<Int, Snake>,
    ) {
        if (isWall) {
            print(" # ")
        } else if (snakeId == 0) {
            print(" . ")
        } else {
            val snake = snakeMap[snakeId]!!
            val point = Point(x, y)
            if (snake.body.first() == point) {
                print(
                    when (snake.headDirection) {
                        Direction.UP -> " ↑ "
                        Direction.DOWN -> " ↓ "
                        Direction.LEFT -> " ← "
                        Direction.RIGHT -> " → "
                    },
                )
            } else {
                print(String.format(Locale.US, "%2d ", snakeId % 100))
            }
        }
    }

    private fun validateSnakesOnNonWallCells(
        level: GameLevel,
        walls: Array<BooleanArray>,
    ) {
        level.snakes.forEach { snake ->
            snake.body.forEach { point ->
                assertTrue(
                    "Snake ${snake.id} body at (${point.x}, ${point.y}) is on a wall!",
                    !walls[point.x][point.y],
                )
            }
        }
    }
}
