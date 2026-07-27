package com.batodev.arrows.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.batodev.arrows.SettingsScreen
import com.batodev.arrows.ads.ConsentManager
import com.batodev.arrows.ads.RewardAdManager
import com.batodev.arrows.data.AndroidResourceBoardShapeProvider
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.DebugViewModel
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsNode(
    buildContext: BuildContext,
    private val appViewModel: AppViewModel,
    private val debugViewModel: DebugViewModel,
    private val onNavigateHome: () -> Unit,
    private val onNavigateToGenerate: () -> Unit
) : Node(buildContext), KoinComponent {

    private val rewardAdManager: RewardAdManager by inject()
    private val consentManager: ConsentManager by inject()

    @Composable
    override fun View(modifier: Modifier) {
        val context = LocalContext.current
        appViewModel.shapeProvider = AndroidResourceBoardShapeProvider(context)
        SettingsScreen(
            viewModel = appViewModel,
            debugViewModel = debugViewModel,
            rewardAdManager = rewardAdManager,
            consentManager = consentManager,
            onNavigateHome = onNavigateHome,
            onNavigateToGenerate = onNavigateToGenerate
        )
    }
}
