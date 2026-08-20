package com.batodev.arrows.ui

import com.batodev.arrows.GameConstants
import com.batodev.arrows.WinCelebrationResources
import com.batodev.arrows.ui.game.CelebrationContentSelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CelebrationContentSelectorTest {
    @Test
    fun `selectContent returns label from CONGRATULATION_LABELS`() {
        val content = CelebrationContentSelector.selectContent()
        assertTrue(
            "Label should be from CONGRATULATION_LABELS",
            content.labelResId in WinCelebrationResources.CONGRATULATION_LABELS,
        )
    }

    @Test
    fun `selectContent returns video from WIN_VIDEOS`() {
        val content = CelebrationContentSelector.selectContent()
        assertTrue(
            "Video should be from WIN_VIDEOS",
            content.videoResId in WinCelebrationResources.WIN_VIDEOS,
        )
    }

    @Test
    fun `selectContent with same seed produces same result`() {
        val content1 = CelebrationContentSelector.selectContent(Random(42))
        val content2 = CelebrationContentSelector.selectContent(Random(42))
        assertEquals("Same seed should produce same label", content1.labelResId, content2.labelResId)
        assertEquals("Same seed should produce same video", content1.videoResId, content2.videoResId)
    }

    @Test
    fun `selectContent with different seeds produces different results`() {
        val results =
            (1..50).map { seed ->
                CelebrationContentSelector.selectContent(Random(seed))
            }
        val distinctLabels = results.map { it.labelResId }.distinct()
        val distinctVideos = results.map { it.videoResId }.distinct()
        assertTrue("Should produce multiple distinct labels", distinctLabels.size > 1)
        assertTrue("Should produce multiple distinct videos", distinctVideos.size > 1)
    }

    @Test
    fun `selectContent covers all labels over many calls`() {
        val allLabels =
            (0..999)
                .map { seed ->
                    CelebrationContentSelector.selectContent(Random(seed)).labelResId
                }.distinct()
                .toSet()
        assertEquals(
            "All congratulation labels should be reachable",
            WinCelebrationResources.CONGRATULATION_LABELS.size,
            allLabels.size,
        )
    }

    @Test
    fun `selectContent covers all videos over many calls`() {
        val allVideos =
            (0..999)
                .map { seed ->
                    CelebrationContentSelector.selectContent(Random(seed)).videoResId
                }.distinct()
                .toSet()
        assertEquals(
            "All win videos should be reachable",
            WinCelebrationResources.WIN_VIDEOS.size,
            allVideos.size,
        )
    }

    @Test
    fun `WIN_VIDEOS_COUNT matches actual list size`() {
        assertEquals(
            "WIN_VIDEOS_COUNT should match WIN_VIDEOS list size",
            GameConstants.WIN_VIDEOS_COUNT,
            WinCelebrationResources.WIN_VIDEOS.size,
        )
    }
}
