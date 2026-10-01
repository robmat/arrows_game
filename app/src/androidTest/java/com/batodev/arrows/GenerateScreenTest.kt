package com.batodev.arrows

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.data.GameStateDao
import com.batodev.arrows.data.IUserPreferencesRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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

    // The width and height sliders, in that order.
    private val isSlider = SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)

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
    fun sizeSlidersSetTheCustomBoardSize() {
        val sliders = composeTestRule.onAllNodes(isSlider)
        sliders[0].performSemanticsAction(SemanticsActions.SetProgress) { it(21f) }
        sliders[1].performSemanticsAction(SemanticsActions.SetProgress) { it(23f) }
        composeTestRule.onNodeWithText("21").assertExists()
        composeTestRule.onNodeWithText("23").assertExists()

        composeTestRule.onNodeWithText(context.getString(R.string.generate_start_label)).performClick()
        composeTestRule.waitUntil(20_000) {
            composeTestRule.onAllNodesWithTag(GAME_AREA_TEST_TAG).fetchSemanticsNodes().isNotEmpty()
        }

        val gameStateDao = koinInstance<GameStateDao>()

        fun savedSize() = runBlocking { gameStateDao.loadGameLevel("CURRENT") }?.let { "${it.width}x${it.height}" }
        // The board is saved once generation finishes, a moment after the game screen appears.
        runCatching { composeTestRule.waitUntil(20_000) { savedSize() == "21x23" } }
        assertEquals("21x23", savedSize())
    }

    @Test
    fun fillBoardModeCapsTheBoardSizeSliders() {
        val widthSlider = composeTestRule.onAllNodes(isSlider)[0]

        fun maxSize() =
            widthSlider
                .fetchSemanticsNode()
                .config[SemanticsProperties.ProgressBarRangeInfo]
                .range.endInclusive
        assertEquals(GameConstants.GENERATOR_MAX_SIZE, maxSize(), 0f)

        runBlocking { koinInstance<IUserPreferencesRepository>().saveFillBoardPreference(true) }

        composeTestRule.waitUntil(5_000) { maxSize() == GameConstants.GENERATOR_MAX_SIZE_FILL_BOARD }
    }

    @Test
    fun tappingAShapeSelectsIt() {
        val rectangular = composeTestRule.onNodeWithContentDescription(context.getString(R.string.shape_rectangular))
        // "bolt" is the first shape after Rectangular, so its pop-in animation finishes first.
        val bolt = composeTestRule.onNodeWithContentDescription("bolt")
        composeTestRule.waitUntil(5_000) { bolt.fetchSemanticsNode().boundsInRoot.width > 0f }
        rectangular.assertIsSelected()

        bolt.performClick()

        bolt.assertIsSelected()
        rectangular.assertIsNotSelected()
    }

    @Test
    fun startingOverASavedGameAsksBeforeDiscardingIt() {
        saveCraftedLevelInProgress()
        // Room's invalidation reaches the screen's hasSavedLevel a moment after the write.
        Thread.sleep(500)
        val start = composeTestRule.onNodeWithText(context.getString(R.string.generate_start_label))
        val warningTitle = context.getString(R.string.custom_gen_warning_title)

        start.performClick()
        composeTestRule.onNodeWithText(warningTitle).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.cancel_label)).performClick()
        composeTestRule.onNodeWithText(warningTitle).assertDoesNotExist()
        start.assertExists() // still on the generator
        val gameStateDao = koinInstance<GameStateDao>()
        awaitCondition("the saved game to be kept") {
            gameStateDao.loadGameLevel("CURRENT")?.snakes?.size == CraftedLevel.level.snakes.size
        }

        start.performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.proceed_label)).performClick()
        composeTestRule.waitUntil(20_000) {
            composeTestRule.onAllNodesWithTag(GAME_AREA_TEST_TAG).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun settingsNavItemFromGeneratorOpensSettings() {
        composeTestRule.onNodeWithText(context.getString(R.string.settings_label)).performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.vibrations_label)).assertExists()
    }

    @Test
    fun homeNavItemFromGeneratorReturnsHome() {
        composeTestRule.onNodeWithText(context.getString(R.string.home_label)).performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.play_label)).assertExists()
    }

    @Test
    fun backButtonFromGeneratorReturnsHome() {
        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.content_description_back))
            .performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.play_label)).assertExists()
    }
}
