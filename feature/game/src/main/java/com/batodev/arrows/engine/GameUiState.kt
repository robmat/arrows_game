package com.batodev.arrows.engine

/**
 * Sealed hierarchy representing the mutually exclusive top-level game phases.
 *
 * The UI is guaranteed to render exactly one state at a time, preventing
 * impossible combinations such as showing a game-won celebration while
 * simultaneously displaying the game-over dialog.
 */
sealed interface GameUiState {
    /** Level generation or initial load is in progress. */
    data class Loading(
        val progress: Float,
    ) : GameUiState

    /** The player is actively playing the level. */
    data class Playing(
        val level: GameLevel,
        val lives: Int,
        val maxLives: Int,
        val totalSnakes: Int,
    ) : GameUiState

    /** All snakes have been removed — the player won. */
    data object Won : GameUiState

    /** The player ran out of lives. */
    data object GameOver : GameUiState
}
