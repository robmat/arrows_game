package com.batodev.arrows.navigation.transitions

import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import com.bumble.appyx.core.navigation.transition.TransitionParams
import com.bumble.appyx.navmodel.backstack.BackStack
import kotlin.math.roundToInt

private const val TRANSITION_DURATION_MS = 350

// @Composable rather than built on `composed {}` (which these used to use): the animated
// values below come from `Transition.animateFloat`, itself @Composable, so the function
// producing this Modifier needs composable access regardless. Being @Composable directly -
// called from within the `modifier.composed {}` bridge that RandomTransitionHandler.kt's
// createModifier() still needs, since ModifierTransitionHandler's non-@Composable override
// signature is fixed by the Appyx library - reads the animated State normally instead of
// through composed{}'s per-recomposition re-invocation, which is what made it a target for
// deprecation in the first place.
@Composable
internal fun Modifier.applyNavTransition(
    transition: Transition<BackStack.State>,
    type: NavTransitionType,
    params: TransitionParams,
): Modifier =
    when (type) {
        NavTransitionType.FADE -> fadeTransition(transition)
        NavTransitionType.SLIDE_HORIZONTAL -> slideHorizontalTransition(transition, params)
        NavTransitionType.SLIDE_VERTICAL -> slideVerticalTransition(transition, params)
        NavTransitionType.SCALE_FADE -> scaleFadeTransition(transition)
        NavTransitionType.ROTATE_FADE -> rotateFadeTransition(transition)
    }

/**
 * Fades out towards the backstack edges, staying opaque only while [BackStack.State.ACTIVE].
 */
@Composable
private fun Transition<BackStack.State>.animateVisibilityAlpha(label: String): State<Float> =
    animateFloat(
        transitionSpec = { tween(TRANSITION_DURATION_MS) },
        label = label,
        targetValueByState = { state ->
            when (state) {
                BackStack.State.ACTIVE -> 1f
                else -> 0f
            }
        },
    )

/**
 * Fraction used to slide an element in/out along one axis: off-screen ahead while
 * [BackStack.State.CREATED]/[BackStack.State.DESTROYED], centered while
 * [BackStack.State.ACTIVE], and slightly behind while [BackStack.State.STASHED].
 */
@Composable
private fun Transition<BackStack.State>.animateEdgeFraction(label: String): State<Float> =
    animateFloat(
        transitionSpec = { tween(TRANSITION_DURATION_MS) },
        label = label,
        targetValueByState = { state ->
            when (state) {
                BackStack.State.CREATED -> 1f
                BackStack.State.ACTIVE -> 0f
                BackStack.State.STASHED -> -0.25f
                BackStack.State.DESTROYED -> 1f
            }
        },
    )

@Composable
private fun Modifier.fadeTransition(transition: Transition<BackStack.State>): Modifier {
    val alpha by transition.animateVisibilityAlpha("fade-alpha")
    return graphicsLayer { this.alpha = alpha }
}

@Composable
private fun Modifier.slideHorizontalTransition(
    transition: Transition<BackStack.State>,
    params: TransitionParams,
): Modifier {
    val xFraction by transition.animateEdgeFraction("slide-h-x")
    val alpha by transition.animateVisibilityAlpha("slide-h-alpha")
    val widthDp = params.bounds.width.value
    return layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(
                x = (widthDp * xFraction * density).roundToInt(),
                y = 0,
            )
        }
    }.graphicsLayer { this.alpha = alpha }
}

@Composable
private fun Modifier.slideVerticalTransition(
    transition: Transition<BackStack.State>,
    params: TransitionParams,
): Modifier {
    val yFraction by transition.animateEdgeFraction("slide-v-y")
    val alpha by transition.animateVisibilityAlpha("slide-v-alpha")
    val heightDp = params.bounds.height.value
    return layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(
                x = 0,
                y = (heightDp * yFraction * density).roundToInt(),
            )
        }
    }.graphicsLayer { this.alpha = alpha }
}

@Composable
private fun Modifier.scaleFadeTransition(transition: Transition<BackStack.State>): Modifier {
    val scale by transition.animateFloat(
        transitionSpec = { tween(TRANSITION_DURATION_MS) },
        label = "scale-scale",
        targetValueByState = { state ->
            when (state) {
                BackStack.State.ACTIVE -> 1f
                else -> 0.85f
            }
        },
    )
    val alpha by transition.animateVisibilityAlpha("scale-alpha")
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

@Composable
private fun Modifier.rotateFadeTransition(transition: Transition<BackStack.State>): Modifier {
    val rotation by transition.animateFloat(
        transitionSpec = { tween(TRANSITION_DURATION_MS) },
        label = "rotate-rotation",
        targetValueByState = { state ->
            when (state) {
                BackStack.State.CREATED -> 8f
                BackStack.State.ACTIVE -> 0f
                BackStack.State.STASHED -> -8f
                BackStack.State.DESTROYED -> 8f
            }
        },
    )
    val alpha by transition.animateVisibilityAlpha("rotate-alpha")
    return graphicsLayer {
        rotationZ = rotation
        this.alpha = alpha
    }
}
