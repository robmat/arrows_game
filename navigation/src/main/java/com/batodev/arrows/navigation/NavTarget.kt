package com.batodev.arrows.navigation

import android.os.Parcelable
import com.batodev.arrows.CustomGameParams
import kotlinx.parcelize.Parcelize

sealed class NavTarget : Parcelable {
    @Parcelize
    object Home : NavTarget()

    @Parcelize
    data class Game(
        val isCustom: Boolean = false,
        val customWidth: Int? = null,
        val customHeight: Int? = null,
        val customShape: String? = null,
    ) : NavTarget()

    @Parcelize
    object Generate : NavTarget()

    @Parcelize
    object Settings : NavTarget()
}

fun NavTarget.Game.toCustomGameParams(): CustomGameParams =
    CustomGameParams(
        isCustom = isCustom,
        customWidth = customWidth,
        customHeight = customHeight,
        customShape = customShape,
    )
