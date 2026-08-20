package com.batodev.arrows.data

import com.batodev.arrows.core.testing.FakeUserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WinVideosPreferencesTest {
    @Test
    fun `test default winVideosEnabled is true`() =
        runTest {
            val repo = FakeUserPreferencesRepository()
            assertTrue(repo.isWinVideosEnabled.first())
        }

    @Test
    fun `test saveWinVideosEnabled disables videos`() =
        runTest {
            val repo = FakeUserPreferencesRepository()
            repo.saveWinVideosEnabled(false)
            assertFalse(repo.isWinVideosEnabled.first())
        }

    @Test
    fun `test saveWinVideosEnabled re-enables videos`() =
        runTest {
            val repo = FakeUserPreferencesRepository()
            repo.saveWinVideosEnabled(false)
            assertFalse(repo.isWinVideosEnabled.first())
            repo.saveWinVideosEnabled(true)
            assertTrue(repo.isWinVideosEnabled.first())
        }
}
