package com.batodev.arrows.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InputHandlerTest {
    private val inputHandler = InputHandler()

    // One-cell snakes on every other cell of a 20x20 board, all pointing right. The tap area of the
    // snake at (x, y) is centred on (x + 0.8, y + 0.5), so neighbours are 2 cells away - outside
    // the 1.3-cell tap tolerance.
    private val grid =
        (0 until 20 step 2).flatMap { x ->
            (0 until 20 step 2).map { y -> Snake(x * 100 + y, listOf(Point(x, y)), Direction.RIGHT) }
        }

    @Test
    fun `obstruction is checked only for snakes within tap range`() {
        val checked = mutableListOf<Snake>()

        val tapped =
            inputHandler.findTappedSnake(4.8f, 4.5f, grid) {
                checked += it
                false
            }

        assertEquals(404, tapped?.id)
        assertEquals(listOf(tapped), checked)
    }

    @Test
    fun `tap far from every snake checks nothing`() {
        val checked = mutableListOf<Snake>()

        val tapped =
            inputHandler.findTappedSnake(30f, 30f, grid) {
                checked += it
                false
            }

        assertNull(tapped)
        assertEquals(emptyList<Snake>(), checked)
    }

    @Test
    fun `unobstructed snake in range wins over a closer obstructed one`() {
        val closer = Snake(1, listOf(Point(2, 2)), Direction.RIGHT) // tap area centre (2.8, 2.5)
        val farther = Snake(2, listOf(Point(3, 2)), Direction.LEFT) // tap area centre (3.2, 2.5)

        val tapped = inputHandler.findTappedSnake(2.9f, 2.5f, listOf(closer, farther)) { it == closer }

        assertEquals(farther, tapped)
    }
}
