package com.batodev.arrows.navigation

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.DebugViewModel
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.replace
import com.batodev.arrows.navigation.transitions.rememberRandomTransitionHandler

class RootNode(
    buildContext: BuildContext,
    private val appViewModel: AppViewModel,
    private val debugViewModel: DebugViewModel,
    private val backStack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.Home,
        savedStateMap = buildContext.savedStateMap
    )
) : ParentNode<NavTarget>(
    navModel = backStack,
    buildContext = buildContext
) {

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node = when (navTarget) {
        NavTarget.Home -> HomeNode(
            buildContext = buildContext,
            appViewModel = appViewModel,
            onPlay = { backStack.push(NavTarget.Game()) },
            onNavigateToGenerate = { backStack.push(NavTarget.Generate) },
            onNavigateToSettings = { backStack.push(NavTarget.Settings) }
        )
        is NavTarget.Game -> GameNode(
            buildContext = buildContext,
            appViewModel = appViewModel,
            customParams = navTarget.toCustomGameParams(),
            onBack = { backStack.pop() }
        )
        NavTarget.Generate -> GenerateNode(
            buildContext = buildContext,
            appViewModel = appViewModel,
            onStartCustomGame = { params ->
                backStack.push(NavTarget.Game(
                    isCustom = params.isCustom,
                    customWidth = params.customWidth,
                    customHeight = params.customHeight,
                    customShape = params.customShape
                ))
            },
            onBack = { backStack.pop() },
            onNavigateHome = { backStack.newRoot(NavTarget.Home) },
            onNavigateToSettings = { backStack.replace(NavTarget.Settings) }
        )
        NavTarget.Settings -> SettingsNode(
            buildContext = buildContext,
            appViewModel = appViewModel,
            debugViewModel = debugViewModel,
            onNavigateHome = { backStack.newRoot(NavTarget.Home) },
            onNavigateToGenerate = { backStack.replace(NavTarget.Generate) }
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        val themeColors = LocalThemeColors.current
        Children(
            navModel = backStack,
            modifier = modifier.background(themeColors.background),
            transitionHandler = rememberRandomTransitionHandler()
        )
    }
}
