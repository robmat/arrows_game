package com.batodev.arrows.engine

import com.batodev.arrows.GameConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EntryAnimatorTest {
    private fun TestScope.animator() = EntryAnimator(CoroutineScope(StandardTestDispatcher(testScheduler)))

    private fun snake(
        id: Int,
        x: Int,
        y: Int,
    ) = Snake(id, listOf(Point(x, y)), Direction.UP)

    @Test
    fun `every snake starts hidden and the animation ends once all have entered`() =
        runTest {
            val animator = animator()
            animator.animate(listOf(snake(1, 2, 2), snake(2, 0, 0)), 5, 5)
            runCurrent()

            assertTrue(animator.isEntryAnimating)
            // Not exactly 0: the animation also follows real time, a little of which has passed.
            assertEquals(setOf(1, 2), animator.entryProgress.keys)
            assertTrue(animator.entryProgress.values.all { it < 0.05f })

            advanceTimeBy(GameConstants.SNAKE_ENTRY_STAGGER_MS + GameConstants.SNAKE_ENTRY_DURATION_MS + 100)
            runCurrent()

            assertFalse(animator.isEntryAnimating)
            assertEquals(emptyMap<Int, Float>(), animator.entryProgress)
        }

    @Test
    fun `the entry ripples out from the centre of the board`() =
        runTest {
            val animator = animator()
            animator.animate(listOf(snake(1, 0, 0), snake(2, 2, 2)), 5, 5)

            advanceTimeBy(GameConstants.SNAKE_ENTRY_DURATION_MS / 2)
            runCurrent()

            val centre = animator.entryProgress.getValue(2)
            val corner = animator.entryProgress.getValue(1)
            assertTrue("centre $centre should be ahead of corner $corner", centre > corner)
            animator.clear()
        }

    @Test
    fun `a big board's entry still ends within two seconds`() =
        runTest {
            // At 50ms a snake this used to take 50s, with every tap ignored until it ended.
            val snakes = (0 until 1_000).map { snake(it, it % 40, it / 40) }
            val animator = animator()
            animator.animate(snakes, 40, 25)

            advanceTimeBy(2_000)
            runCurrent()

            assertFalse(animator.isEntryAnimating)
        }

    @Test
    fun `clear stops the animation`() =
        runTest {
            val animator = animator()
            animator.animate(listOf(snake(1, 2, 2)), 5, 5)
            runCurrent()

            animator.clear()
            advanceTimeBy(GameConstants.SNAKE_ENTRY_DURATION_MS)
            runCurrent()

            assertFalse(animator.isEntryAnimating)
            assertEquals(emptyMap<Int, Float>(), animator.entryProgress)
        }
}
