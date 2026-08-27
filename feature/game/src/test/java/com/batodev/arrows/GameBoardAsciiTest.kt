package com.batodev.arrows

import com.batodev.arrows.engine.Direction
import com.batodev.arrows.engine.GameGenerator
import com.batodev.arrows.engine.GenerationParams
import com.batodev.arrows.engine.Point
import com.batodev.arrows.engine.Snake
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.util.Locale
import java.util.concurrent.TimeUnit

class GameBoardAsciiTest {
    @get:Rule
    val timeout: Timeout = Timeout(30, TimeUnit.SECONDS)

    @Test
    fun printGameBoardAsAsciiArt() {
        val engine = GameGenerator()
        val params =
            GenerationParams(width = 39, height = 39, maxSnakeLength = 18) { progress ->
                println("Progress: $progress")
            }
        val level = engine.generateSolvableLevel(params)

        println("\n" + "=".repeat(60))
        println("Generated Skein Puzzle - ASCII Art")
        println("Board Size: ${level.width} x ${level.height}")
        println("Number of Snakes: ${level.snakes.size}\n")

        val grid = Array(level.width) { IntArray(level.height) }
        val snakeMap = level.snakes.associateBy { it.id }
        level.snakes.forEach { s -> s.body.forEach { p -> grid[p.x][p.y] = s.id } }

        for (y in 0 until level.height) {
            for (x in 0 until level.width) {
                printAsciiCell(x, y, grid[x][y], snakeMap)
            }
            println()
        }

        println("\nSnake Details:")
        level.snakes.forEach { s ->
            println("Snake ${s.id}: Length = ${s.body.size}, Head at ${s.body.first()}, Direction = ${s.headDirection}")
        }
    }

    private fun printAsciiCell(
        x: Int,
        y: Int,
        snakeId: Int,
        snakeMap: Map<Int, Snake>,
    ) {
        if (snakeId == 0) {
            print(" . ")
        } else {
            val snake = snakeMap[snakeId]!!
            if (snake.body.first() == Point(x, y)) {
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

    @Test
    fun printMultipleBoards() {
        for (i in 1..3) {
            val params = GenerationParams(width = 6, height = 8, maxSnakeLength = 4)
            val level = GameGenerator().generateSolvableLevel(params)
            println("\nBoard #$i - ${level.width}x${level.height} with ${level.snakes.size} snakes")

            val grid = Array(level.width) { IntArray(level.height) }
            val snakeMap = level.snakes.associateBy { it.id }
            level.snakes.forEach { s -> s.body.forEach { p -> grid[p.x][p.y] = s.id } }

            for (y in 0 until level.height) {
                for (x in 0 until level.width) {
                    printAsciiCell(x, y, grid[x][y], snakeMap)
                }
                println()
            }
        }
    }
}
