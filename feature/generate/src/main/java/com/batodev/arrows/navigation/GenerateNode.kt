package com.batodev.arrows.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.batodev.arrows.CustomGameParams
import com.batodev.arrows.GenerateScreen
import com.batodev.arrows.ui.AppViewModel
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node

class GenerateNode(
    buildContext: BuildContext,
    private val appViewModel: AppViewModel,
    private val onStartCustomGame: (CustomGameParams) -> Unit,
    private val onBack: () -> Unit,
    private val onNavigateHome: () -> Unit,
    private val onNavigateToSettings: () -> Unit,
) : Node(buildContext) {
    @Composable
    override fun View(modifier: Modifier) {
        GenerateScreen(
            appViewModel = appViewModel,
            onStartCustomGame = onStartCustomGame,
            onBack = onBack,
            onNavigateHome = onNavigateHome,
            onNavigateToSettings = onNavigateToSettings,
        )
    }
}
