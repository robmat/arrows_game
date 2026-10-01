package com.batodev.arrows

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.platform.app.InstrumentationRegistry
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.data.GameStateDao
import com.batodev.arrows.data.IUserPreferencesRepository
import com.batodev.arrows.data.PointData
import com.batodev.arrows.data.SnakeSaveData
import com.batodev.arrows.engine.Direction
import com.batodev.arrows.engine.GameLevel
import com.batodev.arrows.engine.Point
import com.batodev.arrows.engine.Snake
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.math.floor

// Board-level helpers for tests that need to know exactly what is on the board. A generated
// level is random, so these tests play a fixed, hand-made one instead, loaded the same way
// the app loads any game in progress: from GameStateDao, through Home's Continue button.

/**
 * A fixed 5x5 board. [blockedSnake] points right, straight into [freeSnake]'s body;
 * [freeSnake] points up and [extraSnake] left, both with nothing in front of them. Their
 * tap areas are all more than InputHandler's 1.3-cell tolerance apart, so a tap on one
 * can't resolve to another.
 */
object CraftedLevel {
    val blockedSnake = Snake(1, listOf(Point(1, 2), Point(0, 2)), Direction.RIGHT)
    val freeSnake = Snake(2, listOf(Point(3, 2), Point(3, 3), Point(3, 4)), Direction.UP)
    val extraSnake = Snake(3, listOf(Point(1, 4), Point(2, 4)), Direction.LEFT)
    val level = GameLevel(5, 5, listOf(blockedSnake, freeSnake, extraSnake))
    val allIds = level.snakes.map { it.id }
}

/** Saves [CraftedLevel] as the game in progress, with [lives] left, so Home offers Continue. */
fun saveCraftedLevelInProgress(lives: Int = GameConstants.DEFAULT_INITIAL_LIVES) {
    val level = CraftedLevel.level
    val snakes =
        level.snakes.map { snake ->
            SnakeSaveData(snake.id, snake.headDirection.name, snake.body.map { PointData(it.x, it.y) })
        }
    val gameStateDao = koinInstance<GameStateDao>()
    val repository = koinInstance<IUserPreferencesRepository>()
    runBlocking {
        gameStateDao.saveGameLevel("INITIAL", level.width, level.height, snakes)
        gameStateDao.saveGameLevel("CURRENT", level.width, level.height, snakes)
        repository.saveCurrentLives(lives)
    }
}

/** Saves [CraftedLevel] as the game in progress and continues it from Home. */
fun ComposeTestRule.startCraftedGame(lives: Int = GameConstants.DEFAULT_INITIAL_LIVES) {
    saveCraftedLevelInProgress(lives)
    continueSavedGame()
}

/** From Home, taps Continue and waits until the saved board is on screen and settled. */
fun ComposeTestRule.continueSavedGame() {
    val continueLabel = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.continue_label)
    waitUntil(5_000) { onAllNodesWithText(continueLabel).fetchSemanticsNodes().isNotEmpty() }
    onNodeWithText(continueLabel).performClick()
    waitUntil(15_000) { onAllNodesWithTag(GAME_AREA_TEST_TAG).fetchSemanticsNodes().isNotEmpty() }
    awaitStableBoard()
}

/**
 * Taps [snake]'s arrowhead - the point InputHandler.transformTapToGrid maps back onto it.
 * Valid while the board's pan/zoom is at its reset default.
 */
fun ComposeTestRule.tapSnake(
    snake: Snake,
    level: GameLevel,
) {
    val board = onNodeWithTag(GAME_AREA_TEST_TAG)
    val size = board.fetchSemanticsNode().size
    val containerWidth = size.width.toFloat()
    val containerHeight = size.height.toFloat()
    val cellSize = minOf(containerWidth / level.width, containerHeight / level.height)
    val leftOffset = (containerWidth - cellSize * level.width) / 2
    val topOffset = (containerHeight - cellSize * level.height) / 2

    val head = snake.body.first()
    val gridX = head.x + GameConstants.CELL_CENTER + snake.headDirection.dx * GameConstants.TAP_AREA_OFFSET_FACTOR
    val gridY = head.y + GameConstants.CELL_CENTER + snake.headDirection.dy * GameConstants.TAP_AREA_OFFSET_FACTOR
    board.performTouchInput { click(Offset(gridX * cellSize + leftOffset, gridY * cellSize + topOffset)) }
    waitForIdle()
}

/** The game area's pixels, row by row. */
fun ComposeTestRule.boardPixels(): IntArray {
    val image = onNodeWithTag(GAME_AREA_TEST_TAG).captureToImage()
    val pixels = IntArray(image.width * image.height)
    image.readPixels(pixels)
    return pixels
}

/**
 * Waits until two captures of the board 300ms apart are identical, and returns that image.
 * The entry, removal and tap-ripple animations run on plain coroutine delays, which Compose's
 * own idling doesn't wait for.
 */
fun ComposeTestRule.awaitStableBoard(): IntArray {
    val deadline = System.currentTimeMillis() + 10_000
    var previous = boardPixels()
    while (System.currentTimeMillis() < deadline) {
        Thread.sleep(300)
        val current = boardPixels()
        if (current.contentEquals(previous)) return current
        previous = current
    }
    error("The board was still changing after 10s")
}

/** Polls until the board differs from [baseline], and returns that capture. */
fun ComposeTestRule.awaitBoardChange(baseline: IntArray): IntArray {
    var changed: IntArray? = null
    waitUntil(5_000) {
        changed = boardPixels().takeUnless { it.contentEquals(baseline) }
        changed != null
    }
    return checkNotNull(changed)
}

/**
 * The board cells, as (column, row), whose pixels differ between two captures. Pixels outside
 * the board itself - the margins and the corner buttons - are ignored.
 */
fun ComposeTestRule.changedCells(
    before: IntArray,
    after: IntArray,
    level: GameLevel,
): Set<Pair<Int, Int>> {
    val size = onNodeWithTag(GAME_AREA_TEST_TAG).fetchSemanticsNode().size
    val cellSize = minOf(size.width.toFloat() / level.width, size.height.toFloat() / level.height)
    val leftOffset = (size.width - cellSize * level.width) / 2
    val topOffset = (size.height - cellSize * level.height) / 2
    return before.indices
        .filter { before[it] != after[it] }
        .map { index ->
            val column = floor((index % size.width - leftOffset) / cellSize).toInt()
            val row = floor((index / size.width - topOffset) / cellSize).toInt()
            column to row
        }.filter { (column, row) -> column in 0 until level.width && row in 0 until level.height }
        .toSet()
}

/** Waits until the saved game in progress holds exactly the snakes [expectedIds]. */
fun awaitSavedSnakes(expectedIds: List<Int>) {
    val gameStateDao = koinInstance<GameStateDao>()
    awaitCondition("saved board to hold snakes $expectedIds") {
        val saved = gameStateDao.loadGameLevel("CURRENT")?.snakes.orEmpty()
        saved.map { it.id }.sorted() == expectedIds.sorted()
    }
}

/** Polls [condition] - typically a Room read of something the app saves on a background dispatcher. */
fun awaitCondition(
    description: String,
    timeoutMs: Long = 5_000,
    condition: suspend () -> Boolean,
) {
    val deadline = System.currentTimeMillis() + timeoutMs
    runBlocking {
        while (!condition()) {
            check(System.currentTimeMillis() < deadline) { "Timed out after ${timeoutMs}ms waiting for: $description" }
            delay(100)
        }
    }
}
