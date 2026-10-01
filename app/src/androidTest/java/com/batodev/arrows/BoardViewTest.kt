package com.batodev.arrows

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipe
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.data.IUserPreferencesRepository
import com.batodev.arrows.ui.game.INTRO_OVERLAY_TEST_TAG
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// What the board shows rather than what it holds: the hint flash, guidance lines and pan/zoom
// are drawn on the board's single Canvas, which has no semantics, so they're checked in the
// board's pixels - against a baseline capture of the settled CraftedLevel (BoardTestSupport.kt).
@RunWith(AndroidJUnit4::class)
class BoardViewTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val level = CraftedLevel.level

    @Before
    fun setUp() {
        composeTestRule.resetAppState()
        composeTestRule.startCraftedGame()
    }

    private fun awaitBoardCellsChange(baseline: IntArray) =
        composeTestRule.waitUntil(5_000) {
            composeTestRule.changedCells(baseline, composeTestRule.boardPixels(), level).isNotEmpty()
        }

    private fun awaitBoardEquals(expected: IntArray) =
        composeTestRule.waitUntil(8_000) { composeTestRule.boardPixels().contentEquals(expected) }

    @Test
    fun hintBrieflyHighlightsTheSnakeThatCanBeRemoved() {
        val baseline = composeTestRule.awaitStableBoard()

        composeTestRule.onNodeWithText(context.getString(R.string.hint_label)).performClick()

        // Only the free snake - column 3 of the 5-column board - changes while it flashes.
        val flashing = composeTestRule.awaitBoardChange(baseline)
        val flashedColumns = composeTestRule.changedCells(baseline, flashing, level).map { it.first }.toSet()
        assertEquals(setOf(3), flashedColumns)

        // FLASH_DURATION_MS later the board is back to normal.
        awaitBoardEquals(baseline)
    }

    @Test
    fun guidanceLinesToggleDrawsAndRemovesTheLines() {
        val baseline = composeTestRule.awaitStableBoard()
        val toggle =
            composeTestRule.onNodeWithContentDescription(context.getString(R.string.content_description_guidance_lines))

        toggle.performClick()
        awaitBoardCellsChange(baseline)

        toggle.performClick()
        awaitBoardEquals(baseline)
    }

    @Test
    fun resetViewUndoesPanningAndZooming() {
        val baseline = composeTestRule.awaitStableBoard()
        val board = composeTestRule.onNodeWithTag(GAME_AREA_TEST_TAG)
        val resetView =
            composeTestRule.onNodeWithContentDescription(context.getString(R.string.content_description_reset_view))

        board.performTouchInput { swipe(center, center + Offset(width / 4f, 0f), durationMillis = 300) }
        awaitBoardCellsChange(baseline)
        resetView.performClick()
        awaitBoardEquals(baseline)

        board.performTouchInput {
            pinch(
                start0 = center - Offset(50f, 0f),
                end0 = center - Offset(250f, 0f),
                start1 = center + Offset(50f, 0f),
                end1 = center + Offset(250f, 0f),
            )
        }
        awaitBoardCellsChange(baseline)
        resetView.performClick()
        awaitBoardEquals(baseline)
    }

    @Test
    fun introFingerShowsUntilTheFirstSnakeIsRemoved() {
        val repository = koinInstance<IUserPreferencesRepository>()
        runBlocking { repository.saveIntroCompleted(false) }

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithTag(INTRO_OVERLAY_TEST_TAG).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.tapSnake(CraftedLevel.freeSnake, level)

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithTag(INTRO_OVERLAY_TEST_TAG).fetchSemanticsNodes().isEmpty()
        }
        awaitCondition("intro to be recorded as completed") { repository.introCompleted.first() }
    }
}
