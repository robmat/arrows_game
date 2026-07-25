package com.batodev.arrows

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.data.GameStateDao
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// GameEngine renders the whole board (snakes/arrows) on a single Canvas
// (ArrowsBoardRenderer) rather than individual Composables, so it's not addressable via
// Compose's semantics tree the way buttons/text are - solveCurrentLevel() (see
// ComposeTestSupport.kt) drives it with real synthesized taps at coordinates computed
// the same way InputHandler interprets a genuine user tap, actually playing and winning
// a real generated level rather than asserting around the board.
@RunWith(AndroidJUnit4::class)
class GameScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val gameStateDao: GameStateDao by lazy { koinInstance<GameStateDao>() }

    @Before
    fun setUp() {
        composeTestRule.resetAppState()
        // resetAppState() doesn't force a recreate() (see ComposeTestSupport.kt) - wait
        // for the already-showing Home screen to reactively pick the reset DB state up
        // (Play, not a leftover Continue from a previous test) before clicking it.
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText(context.getString(R.string.play_label)).fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithText(context.getString(R.string.play_label)).performClick()
        composeTestRule.waitUntil(15_000) {
            composeTestRule.onAllNodesWithTag(GAME_AREA_TEST_TAG).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun playingALevelToCompletionReturnsHomeOnLevelTwo() {
        composeTestRule.solveCurrentLevel(gameStateDao)

        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag(GAME_AREA_TEST_TAG).fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText(context.getString(R.string.level_label, 2)).fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun backButtonDuringGameplayReturnsHome() {
        composeTestRule.onNodeWithContentDescription(context.getString(R.string.content_description_back))
            .performClick()

        // Not play_label: GameEngine already persisted this level to Room the moment it
        // (re)generated (see GameEngine.regenerateLevel()'s onComplete), before this test
        // ever interacted with it, so Home now shows Continue instead of Play - the level
        // number label is unaffected by that and still proves we're really back on Home.
        composeTestRule.onNodeWithText(context.getString(R.string.level_label, 1)).assertExists()
    }

    @Test
    fun restartButtonKeepsPlayingTheSameBoard() {
        composeTestRule.onNodeWithContentDescription(context.getString(R.string.content_description_restart))
            .performClick()

        composeTestRule.onNodeWithTag(GAME_AREA_TEST_TAG).assertExists()
    }
}
