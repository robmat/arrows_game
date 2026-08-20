package com.batodev.arrows.ui

import com.batodev.arrows.GameConstants
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsLinksTest {
    @Test
    fun `GitHub repo URL points to correct repository`() {
        assertTrue(
            "GitHub URL should point to robmat/arrows_game",
            GameConstants.GITHUB_REPO_URL.contains("github.com/robmat/arrows_game"),
        )
    }

    @Test
    fun `GitHub repo URL uses HTTPS`() {
        assertTrue(
            "GitHub URL should use HTTPS",
            GameConstants.GITHUB_REPO_URL.startsWith("https://"),
        )
    }
}
