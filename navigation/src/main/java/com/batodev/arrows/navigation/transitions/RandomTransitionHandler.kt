package com.batodev.arrows.navigation.transitions

import android.annotation.SuppressLint
import androidx.compose.animation.core.Transition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.bumble.appyx.core.navigation.transition.ModifierTransitionHandler
import com.bumble.appyx.core.navigation.transition.TransitionDescriptor
import com.bumble.appyx.navmodel.backstack.BackStack

class RandomTransitionHandler<T>(
    private val picker: TransitionPicker = TransitionPicker(),
) : ModifierTransitionHandler<T, BackStack.State>() {
    // Lint recommends Modifier factory functions be extension functions, but this is an
    // override of ModifierTransitionHandler.createModifier() from the Appyx library —
    // the signature is fixed by the library contract and cannot be changed.
    @SuppressLint("ModifierFactoryExtensionFunction")
    override fun createModifier(
        modifier: Modifier,
        transition: Transition<BackStack.State>,
        descriptor: TransitionDescriptor<T, BackStack.State>,
    ): Modifier =
        modifier.composed {
            val type = remember { picker.pick() }
            applyNavTransition(transition, type, descriptor.params)
        }
}

@Composable
fun <T : Any> rememberRandomTransitionHandler(): ModifierTransitionHandler<T, BackStack.State> {
    val picker = remember { TransitionPicker() }
    return remember(picker) { RandomTransitionHandler(picker) }
}
