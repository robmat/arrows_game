package com.batodev.arrows.ui.game

import com.batodev.arrows.WinCelebrationResources
import kotlin.random.Random

data class CelebrationContent(
    val labelResId: Int,
    val videoResId: Int,
)

object CelebrationContentSelector {
    fun selectContent(random: Random = Random): CelebrationContent {
        val label = WinCelebrationResources.CONGRATULATION_LABELS.random(random)
        val video = WinCelebrationResources.WIN_VIDEOS.random(random)
        return CelebrationContent(labelResId = label, videoResId = video)
    }
}
