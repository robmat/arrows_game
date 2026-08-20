package com.batodev.arrows.engine

data class DebugDialogParams(
    val dialogToShow: String?,
    val levelNumber: Int,
    val forcedWidth: Int?,
    val forcedHeight: Int?,
    val forcedLives: Int?,
    val forcedShape: String?,
    val onDismiss: () -> Unit,
)
