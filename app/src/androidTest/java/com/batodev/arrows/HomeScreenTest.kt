package com.batodev.arrows

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.batodev.arrows.core.resources.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        composeTestRule.resetAppState()
        // resetAppState() doesn't force a recreate() (see ComposeTestSupport.kt) - the
        // Home screen already showing (launched by the compose rule before this @Before
        // even runs) picks the DB write up reactively through Room's Flow, which can
        // lag a beat behind the write completing.
        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodesWithText(context.getString(R.string.play_label))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodesWithText(context.getString(R.string.level_label, 1))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun homeScreenShowsLogoLevelAndPlayButton() {
        composeTestRule.onNodeWithText(context.getString(R.string.level_label, 1)).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.play_label)).assertExists()
    }

    @Test
    fun playButtonNavigatesToGameScreen() {
        composeTestRule.onNodeWithText(context.getString(R.string.play_label)).performClick()

        composeTestRule.waitUntil(15_000) {
            composeTestRule.onAllNodesWithTag(GAME_AREA_TEST_TAG).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag(GAME_AREA_TEST_TAG).assertExists()
    }

    @Test
    fun generatorNavItemIsLockedBelowUnlockLevel() {
        val lockedLabel = context.getString(R.string.level_label, GameConstants.GENERATOR_UNLOCK_LEVEL)
        composeTestRule.onNodeWithText(lockedLabel).performClick()

        // Locked (levelNumber starts at 1) - tap is a no-op, still on Home.
        composeTestRule.onNodeWithText(context.getString(R.string.play_label)).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.generate_start_label)).assertDoesNotExist()
    }

    @Test
    fun settingsNavItemNavigatesToSettingsAndBackReturnsHome() {
        composeTestRule.onNodeWithText(context.getString(R.string.settings_label)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.vibrations_label)).assertExists()

        composeTestRule.onNodeWithText(context.getString(R.string.home_label)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.play_label)).assertExists()
    }

    @Test
    fun systemBackPressFromHomeExitsApp() {
        assertBackPressFinishesScenario(composeTestRule.activityRule.scenario)
    }
}
