package com.batodev.arrows

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.batodev.arrows.core.resources.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Settings rows that leave the app. Every external intent is recorded and answered in place,
// so no browser or mail app opens on the device and takes window focus from later tests.
// Not covered: "Rate us" goes through Play's in-app review API rather than an intent, and
// "Manage consent" only appears where the consent form is required.
@RunWith(AndroidJUnit4::class)
class SettingsLinksTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        composeTestRule.resetAppState()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_label)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.vibrations_label)).assertExists()
        Intents.init()
        intending(not(isInternal())).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    private fun clickRow(label: Int) {
        composeTestRule.onNodeWithText(context.getString(label)).performScrollTo().performClick()
    }

    @Test
    fun writeUsOpensAnEmailToSupport() {
        clickRow(R.string.write_us_label)

        intended(allOf(hasAction(Intent.ACTION_SENDTO), hasData("mailto:"), hasExtraWithKey(Intent.EXTRA_EMAIL)))
    }

    @Test
    fun moreGamesOpensTheDeveloperPage() {
        clickRow(R.string.more_games_label)

        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData("https://play.google.com/store/apps/dev?id=8228670503574649511"),
            ),
        )
    }

    @Test
    fun sourceCodeOpensTheRepository() {
        clickRow(R.string.source_code_label)

        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(GameConstants.GITHUB_REPO_URL)))
    }

    @Test
    fun privacyPolicyOpensInTheBrowser() {
        clickRow(R.string.privacy_label)

        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData("https://robmat.github.io/privacy_policy.html")))
    }

    @Test
    fun licenseEntryLinkOpensTheLibraryWebsite() {
        clickRow(R.string.third_party_licenses_label)
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasText("http", substring = true)).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodes(hasText("http", substring = true))[0].performClick()

        intended(hasAction(Intent.ACTION_VIEW))
    }
}
