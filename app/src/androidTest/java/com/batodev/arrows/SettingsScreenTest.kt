package com.batodev.arrows

import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.data.IUserPreferencesRepository
import kotlinx.coroutines.flow.first
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        composeTestRule.resetAppState()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_label)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.vibrations_label)).assertExists()
    }

    @Test
    fun settingsScreenShowsPreferenceSections() {
        composeTestRule.onNodeWithText(context.getString(R.string.sounds_label)).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.theme_label)).assertExists()
    }

    @Test
    fun togglingVibrationSwitchPersistsToRepository() {
        // The Switch is a sibling of its label Text (SettingsSwitchItem's Row has no
        // clickable of its own, and neither Row nor its parent Column merge descendant
        // semantics), so onNodeWithText(...).onParent() lands several levels too high -
        // Vibrations is the first of PreferencesSection's four switches, so it's reliably
        // the first toggleable node in the whole screen instead.
        composeTestRule.onAllNodes(isToggleable())[0].performClick()

        val repository = koinInstance<IUserPreferencesRepository>()
        awaitCondition("vibration to be switched off") { !repository.isVibrationEnabled.first() }
    }

    @Test
    fun themeDialogOpensAndSelectingAThemeDismissesIt() {
        composeTestRule.onNodeWithText(context.getString(R.string.theme_label)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.choose_theme_title)).assertExists()

        // Not "Green": that's the default theme (UserPreferencesEntity), so its name is
        // already showing behind the dialog as the settings row's current value too.
        composeTestRule.onNodeWithText(context.getString(R.string.theme_red)).performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.choose_theme_title)).assertDoesNotExist()
        val repository = koinInstance<IUserPreferencesRepository>()
        awaitCondition("theme Red to be saved") { repository.theme.first() == "Red" }
    }

    // PreferencesSection's switches are, in order: vibrations, sounds, win videos, fill board.
    @Test
    fun togglingSoundsSwitchPersistsToRepository() {
        composeTestRule.onAllNodes(isToggleable())[1].performScrollTo().performClick()

        val repository = koinInstance<IUserPreferencesRepository>()
        awaitCondition("sounds to be switched off") { !repository.isSoundsEnabled.first() }
    }

    @Test
    fun togglingWinVideosSwitchPersistsToRepository() {
        composeTestRule.onAllNodes(isToggleable())[2].performScrollTo().performClick()

        val repository = koinInstance<IUserPreferencesRepository>()
        awaitCondition("win videos to be switched on") { repository.isWinVideosEnabled.first() }
    }

    @Test
    fun togglingFillBoardSwitchPersistsToRepository() {
        composeTestRule.onAllNodes(isToggleable())[3].performScrollTo().performClick()

        val repository = koinInstance<IUserPreferencesRepository>()
        awaitCondition("fill board to be switched on") { repository.isFillBoardEnabled.first() }
    }

    @Test
    fun choosingAnAnimationSpeedPersistsIt() {
        composeTestRule
            .onNodeWithText(context.getString(R.string.animation_speed_label))
            .performScrollTo()
            .performClick()
        // Not "Medium": that's the baseline, already showing behind the dialog as the row's value.
        composeTestRule.onNodeWithText(context.getString(R.string.speed_high)).performClick()

        val repository = koinInstance<IUserPreferencesRepository>()
        awaitCondition("animation speed High to be saved") { repository.animationSpeed.first() == "High" }
    }

    @Test
    fun adFreePlayersSeeThatAdsAreRemoved() {
        composeTestRule.onNodeWithText(context.getString(R.string.ads_removed)).performScrollTo().assertExists()
    }

    @Test
    fun generatorNavItemOpensTheGeneratorOnceUnlocked() {
        composeTestRule.unlockGenerator()
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
    fun licensesDialogListsBundledLibraries() {
        // aboutlibraries loads its generated JSON by resource name, which the release resource
        // shrinker can't see - without feature/settings' raw/aboutlibraries_keep.xml the JSON was
        // stripped and opening this dialog crashed the app under -PminifiedTests (and in release).
        composeTestRule
            .onNodeWithText(context.getString(R.string.third_party_licenses_label))
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodesWithText("Apache License 2.0", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun homeNavItemReturnsToHomeScreen() {
        composeTestRule.onNodeWithText(context.getString(R.string.home_label)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.play_label)).assertExists()
    }
}
