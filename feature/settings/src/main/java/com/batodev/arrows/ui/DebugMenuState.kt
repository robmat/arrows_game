package com.batodev.arrows.ui

data class DebugMenuState(
    val levelNumber: Int,
    val forcedWidth: Int?,
    val forcedHeight: Int?,
    val forcedLives: Int?,
    val forcedShape: String?,
    val shapes: List<String?>,
    val onRegenerateLevel: () -> Unit,
    val onSaveLevelNumber: (Int) -> Unit,
    val onSaveDebugOption: (DebugViewModel.DebugOption, Any?) -> Unit,
)
