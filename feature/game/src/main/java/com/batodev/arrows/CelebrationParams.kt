package com.batodev.arrows

data class CelebrationParams(
    val showCelebration: Boolean,
    val onCelebrationComplete: () -> Unit
)
