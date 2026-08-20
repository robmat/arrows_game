package com.batodev.arrows.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.batodev.arrows.MainScreen
import com.batodev.arrows.ui.AppViewModel
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node

class HomeNode(
    buildContext: BuildContext,
    private val appViewModel: AppViewModel,
    private val onPlay: () -> Unit,
    private val onNavigateToGenerate: () -> Unit,
    private val onNavigateToSettings: () -> Unit,
) : Node(buildContext) {
    @Composable
    override fun View(modifier: Modifier) {
        MainScreen(
            appViewModel = appViewModel,
            onPlay = onPlay,
            onNavigateToGenerate = onNavigateToGenerate,
            onNavigateToSettings = onNavigateToSettings,
        )
    }
}
