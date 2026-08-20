package com.batodev.arrows.data

import com.batodev.arrows.core.testing.FakeUserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdPreferencesTest {
    @Test
    fun `test default isAdFree is false`() =
        runTest {
            val repo = FakeUserPreferencesRepository()
            val isAdFree = repo.isAdFree.first()
            assertFalse(isAdFree)
        }

    @Test
    fun `test default rewardAdCount is 0`() =
        runTest {
            val repo = FakeUserPreferencesRepository()
            val count = repo.rewardAdCount.first()
            assertEquals(0, count)
        }

    @Test
    fun `test saveIsAdFree updates flow`() =
        runTest {
            val repo = FakeUserPreferencesRepository()
            repo.saveIsAdFree(true)
            val isAdFree = repo.isAdFree.first()
            assertTrue(isAdFree)
        }

    @Test
    fun `test incrementRewardAdCount increases count`() =
        runTest {
            val repo = FakeUserPreferencesRepository()

            repo.incrementRewardAdCount()
            assertEquals(1, repo.rewardAdCount.first())

            repo.incrementRewardAdCount()
            assertEquals(2, repo.rewardAdCount.first())

            repo.incrementRewardAdCount()
            assertEquals(3, repo.rewardAdCount.first())
        }

    @Test
    fun `test resetRewardAdCount sets count to 0`() =
        runTest {
            val repo = FakeUserPreferencesRepository()

            repo.incrementRewardAdCount()
            repo.incrementRewardAdCount()
            repo.incrementRewardAdCount()
            assertEquals(3, repo.rewardAdCount.first())

            repo.resetRewardAdCount()
            assertEquals(0, repo.rewardAdCount.first())
        }

    @Test
    fun `test ad removal flow - watch 30 ads`() =
        runTest {
            val repo = FakeUserPreferencesRepository()

            // Initially not ad-free
            assertFalse(repo.isAdFree.first())
            assertEquals(0, repo.rewardAdCount.first())

            // Watch 29 ads
            repeat(29) {
                repo.incrementRewardAdCount()
            }
            assertEquals(29, repo.rewardAdCount.first())
            assertFalse(repo.isAdFree.first())

            // Watch 30th ad and become ad-free
            repo.incrementRewardAdCount()
            assertEquals(30, repo.rewardAdCount.first())

            // Mark as ad-free and reset count
            repo.saveIsAdFree(true)
            repo.resetRewardAdCount()

            assertTrue(repo.isAdFree.first())
            assertEquals(0, repo.rewardAdCount.first())
        }
}
