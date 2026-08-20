package com.batodev.arrows.data

import com.batodev.arrows.core.testing.FakeUserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IntroPreferencesTest {
    @Test
    fun `test default introCompleted is false`() =
        runTest {
            val repo = FakeUserPreferencesRepository()
            assertFalse(repo.introCompleted.first())
        }

    @Test
    fun `test saveIntroCompleted updates flow to true`() =
        runTest {
            val repo = FakeUserPreferencesRepository()
            repo.saveIntroCompleted(true)
            assertTrue(repo.introCompleted.first())
        }

    @Test
    fun `test saveIntroCompleted can reset to false`() =
        runTest {
            val repo = FakeUserPreferencesRepository()
            repo.saveIntroCompleted(true)
            assertTrue(repo.introCompleted.first())
            repo.saveIntroCompleted(false)
            assertFalse(repo.introCompleted.first())
        }
}
