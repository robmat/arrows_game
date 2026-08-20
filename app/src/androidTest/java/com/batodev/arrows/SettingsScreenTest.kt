package com.batodev.arrows

import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.data.IUserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
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
        val stillEnabled = runBlocking { repository.isVibrationEnabled.first() }
        assertFalse(stillEnabled)
    }

    @Test
    fun themeDialogOpensAndSelectingAThemeDismissesIt() {
        composeTestRule.onNodeWithText(context.getString(R.string.theme_label)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.choose_theme_title)).assertExists()

        // Not "Green": that's the default theme (UserPreferencesEntity), so its name is
        // already showing behind the dialog as the settings row's current value too.
        composeTestRule.onNodeWithText(context.getString(R.string.theme_red)).performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.choose_theme_title)).assertDoesNotExist()
    }

    @Test
    fun homeNavItemReturnsToHomeScreen() {
        composeTestRule.onNodeWithText(context.getString(R.string.home_label)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.play_label)).assertExists()
    }
}
