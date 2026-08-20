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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// GeneratorNavigationItem (AppNavigationBar.kt) is locked below level 20
// (GameConstants.GENERATOR_UNLOCK_LEVEL) - reaching it for real through the bottom nav
// would mean grinding out 20 real levels first, so unlockGenerator() (see
// ComposeTestSupport.kt) sets the persisted level number directly through the same Room
// repository the app itself reads, then everything from there on is real navigation.
@RunWith(AndroidJUnit4::class)
class GenerateScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        composeTestRule.resetAppState()
        composeTestRule.unlockGenerator()
        // Neither reset helper forces a recreate() (see ComposeTestSupport.kt) - wait for
        // the already-showing Home screen to reactively pick up levelNumber=20 (unlocked,
        // showing "Generator" instead of the locked "Level 20") before clicking it.
        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodesWithText(context.getString(R.string.custom_gen_title))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithText(context.getString(R.string.custom_gen_title)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.generate_start_label)).assertExists()
    }

    @Test
    fun generatorScreenShowsSizeAndShapeControls() {
        composeTestRule.onNodeWithText(context.getString(R.string.width_label)).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.height_label)).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.shape_label)).assertExists()
    }

    @Test
    fun startingACustomGameNavigatesToGameScreen() {
        composeTestRule.onNodeWithText(context.getString(R.string.generate_start_label)).performClick()

        composeTestRule.waitUntil(20_000) {
            composeTestRule.onAllNodesWithTag(GAME_AREA_TEST_TAG).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag(GAME_AREA_TEST_TAG).assertExists()
    }

    @Test
    fun backButtonFromGeneratorReturnsHome() {
        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.content_description_back))
            .performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.play_label)).assertExists()
    }
}
