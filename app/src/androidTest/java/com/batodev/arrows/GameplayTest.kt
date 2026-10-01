package com.batodev.arrows

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.data.IUserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Every test here plays CraftedLevel (see BoardTestSupport.kt), so it knows exactly which
// snake is blocked and which are free. The board is one Canvas with no semantics of its own,
// so removals and lives are checked where they land: the saved game in Room.
@RunWith(AndroidJUnit4::class)
class GameplayTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val repository: IUserPreferencesRepository by lazy { koinInstance<IUserPreferencesRepository>() }
    private val level = CraftedLevel.level

    @Before
    fun setUp() {
        composeTestRule.resetAppState()
    }

    private fun awaitSavedLives(expected: Int) =
        awaitCondition("saved lives to be $expected") { repository.currentLives.first() == expected }

    private fun awaitGameOverDialog() =
        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodesWithText(context.getString(R.string.game_over_title))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

    @Test
    fun tappingAFreeSnakeRemovesIt() {
        composeTestRule.startCraftedGame()

        composeTestRule.tapSnake(CraftedLevel.freeSnake, level)

        awaitSavedSnakes(listOf(CraftedLevel.blockedSnake.id, CraftedLevel.extraSnake.id))
    }

    @Test
    fun tappingABlockedSnakeCostsALifeAndKeepsItOnTheBoard() {
        composeTestRule.startCraftedGame()

        composeTestRule.tapSnake(CraftedLevel.blockedSnake, level)

        awaitSavedLives(GameConstants.DEFAULT_INITIAL_LIVES - 1)
        // A wrong removal would only be saved once its removal animation had finished.
        Thread.sleep(1_000)
        awaitSavedSnakes(CraftedLevel.allIds)
    }

    @Test
    fun restartRestoresTheInitialBoardAndFullLives() {
        composeTestRule.startCraftedGame()
        composeTestRule.tapSnake(CraftedLevel.blockedSnake, level)
        awaitSavedLives(GameConstants.DEFAULT_INITIAL_LIVES - 1)
        composeTestRule.tapSnake(CraftedLevel.extraSnake, level)
        awaitSavedSnakes(listOf(CraftedLevel.blockedSnake.id, CraftedLevel.freeSnake.id))

        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.content_description_restart))
            .performClick()

        awaitSavedSnakes(CraftedLevel.allIds)
        awaitSavedLives(GameConstants.DEFAULT_INITIAL_LIVES)
    }

    @Test
    fun losingTheLastLifeShowsGameOverAndRestartBoardStartsTheLevelAgain() {
        composeTestRule.startCraftedGame(lives = 1)
        composeTestRule.tapSnake(CraftedLevel.extraSnake, level)
        awaitSavedSnakes(listOf(CraftedLevel.blockedSnake.id, CraftedLevel.freeSnake.id))

        composeTestRule.tapSnake(CraftedLevel.blockedSnake, level)
        awaitGameOverDialog()
        composeTestRule.onNodeWithText(context.getString(R.string.restart_board_label)).performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.game_over_title)).assertDoesNotExist()
        awaitSavedSnakes(CraftedLevel.allIds)
        awaitSavedLives(GameConstants.DEFAULT_INITIAL_LIVES)
    }

    @Test
    fun gameOverAddLifeLetsAnAdFreePlayerKeepPlaying() {
        composeTestRule.startCraftedGame(lives = 1)

        composeTestRule.tapSnake(CraftedLevel.blockedSnake, level)
        awaitGameOverDialog()
        composeTestRule.onNodeWithText(context.getString(R.string.add_life_label)).performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.game_over_title)).assertDoesNotExist()
        awaitSavedLives(1)
        // Still playing the same board, not restarted.
        composeTestRule.tapSnake(CraftedLevel.freeSnake, level)
        awaitSavedSnakes(listOf(CraftedLevel.blockedSnake.id, CraftedLevel.extraSnake.id))
    }

    @Test
    fun winningWithCelebrationVideosOnShowsTheCelebrationThenTheNextLevel() {
        runBlocking { repository.saveWinVideosEnabled(true) }
        composeTestRule.startCraftedGame()

        listOf(CraftedLevel.extraSnake, CraftedLevel.freeSnake, CraftedLevel.blockedSnake).forEach { snake ->
            composeTestRule.tapSnake(snake, level)
            // Outlasts the removal animation, so the next obstruction check sees a settled board.
            Thread.sleep(1_000)
        }

        composeTestRule.waitUntil(10_000) {
            WinCelebrationResources.CONGRATULATION_LABELS.any { label ->
                composeTestRule.onAllNodesWithText(context.getString(label)).fetchSemanticsNodes().isNotEmpty()
            }
        }
        composeTestRule.waitUntil(15_000) {
            composeTestRule
                .onAllNodesWithText(context.getString(R.string.level_label, 2))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
