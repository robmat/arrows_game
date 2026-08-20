package com.batodev.arrows.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batodev.arrows.GameConstants
import com.batodev.arrows.SoundManager
import com.batodev.arrows.data.GameStateDao
import com.batodev.arrows.data.IUserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class GameEngineConfig(
    val repository: IUserPreferencesRepository,
    val gameStateDao: GameStateDao,
    val gameGenerator: GameGenerator = GameGenerator(),
    val autoLoad: Boolean = true,
    val isCustomGame: Boolean = false,
    val backgroundDispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Default,
    val coroutineScopeOverride: CoroutineScope? = null,
)

data class GameEngineFeatures(
    val onVibrate: () -> Unit = {},
    val soundManager: SoundManager? = null,
    val shapeProvider: BoardShapeProvider? = null,
    val random: kotlin.random.Random = kotlin.random.Random.Default,
    val forcedWidth: Int? = null,
    val forcedHeight: Int? = null,
    val forcedShape: String? = null,
)

class GameEngine(
    config: GameEngineConfig,
    features: GameEngineFeatures = GameEngineFeatures(),
) : ViewModel() {
    private val coroutineScope = config.coroutineScopeOverride ?: viewModelScope
    private val repository = config.repository
    private val isCustomGame = config.isCustomGame
    private val backgroundDispatcher = config.backgroundDispatcher
    private val soundManager = features.soundManager
    private val inputHandler = InputHandler()
    private val levelManager =
        LevelManager(
            repository,
            config.gameGenerator,
            features.shapeProvider,
            features.random,
            config.gameStateDao,
        )
    internal val transformationState = TransformationState()
    private val removalAnimator = RemovalAnimator(coroutineScope)
    private val entryAnimator = EntryAnimator(coroutineScope)
    private val tapHandler = TapHandler(coroutineScope, soundManager, features.onVibrate)

    private var initialLevel: GameLevel? = null
    private var isVibrationEnabled = true
    private var animationSpeed = "Medium"
    private var forcedWidth: Int? = features.forcedWidth
    private var forcedHeight: Int? = features.forcedHeight
    private var forcedLives: Int? = null
    private var forcedShape: String? = features.forcedShape

    var levelNumber by mutableIntStateOf(1)
        private set
    var level by mutableStateOf(GameLevel(1, 1, emptyList()))
        private set
    var isLoading by mutableStateOf(true)
        private set
    var totalSnakesInLevel by mutableIntStateOf(0)
        private set
    var isGameWon by mutableStateOf(false)
        private set
    var loadingProgress by mutableFloatStateOf(0f)
        private set
    var lives by mutableIntStateOf(GameConstants.INITIAL_LIVES)
        private set
    var maxLives by mutableIntStateOf(GameConstants.INITIAL_LIVES)
        private set

    val scale get() = transformationState.scale
    val offsetX get() = transformationState.offsetX
    val offsetY get() = transformationState.offsetY
    val removalProgress get() = removalAnimator.removalProgress
    val entryProgress get() = entryAnimator.entryProgress
    val isEntryAnimating get() = entryAnimator.isEntryAnimating
    val flashingSnakeId get() = tapHandler.flashingSnakeId

    /**
     * A single sealed state representing the current game phase.
     * Mutually exclusive — the UI can switch on this type without reading
     * multiple individual properties that might produce impossible combinations.
     * Animation details (entryProgress, removalProgress, flashingSnakeId) are
     * exposed separately since they change at frame rate and do not affect phase.
     */
    val uiState: GameUiState
        get() =
            when {
                isLoading -> {
                    GameUiState.Loading(loadingProgress)
                }

                isGameWon -> {
                    GameUiState.Won
                }

                lives <= 0 -> {
                    GameUiState.GameOver
                }

                else -> {
                    GameUiState.Playing(
                        level = level,
                        lives = lives,
                        maxLives = maxLives,
                        totalSnakes = totalSnakesInLevel,
                    )
                }
            }

    init {
        observePreferences()
        if (config.autoLoad) loadOrRegenerateLevel()
    }

    private fun observePreferences() {
        coroutineScope.launch { repository.isVibrationEnabled.collect { isVibrationEnabled = it } }
        coroutineScope.launch { repository.levelNumber.collect { levelNumber = it } }
        coroutineScope.launch {
            repository.isSoundsEnabled.collect {
                soundManager?.setSoundsEnabled(
                    it,
                )
            }
        }
        coroutineScope.launch { repository.animationSpeed.collect { animationSpeed = it } }
        coroutineScope.launch {
            repository.debugForcedWidth.collect {
                if (forcedWidth == null) forcedWidth = it
            }
        }
        coroutineScope.launch {
            repository.debugForcedHeight.collect {
                if (forcedHeight == null) forcedHeight = it
            }
        }
        coroutineScope.launch { repository.debugForcedLives.collect { forcedLives = it } }
        coroutineScope.launch {
            repository.debugForcedShape.collect {
                if (forcedShape == null) forcedShape = it
            }
        }
    }

    fun loadOrRegenerateLevel() {
        coroutineScope.launch(backgroundDispatcher) {
            levelManager.loadLevel(
                onSuccess = { initial, current, maxL, currentL ->
                    initialLevel = initial
                    level = current
                    totalSnakesInLevel =
                        initial.snakes.size
                    isGameWon = level.snakes.isEmpty()
                    maxLives = maxL
                    lives =
                        currentL
                    isLoading = false
                    animateEntry()
                },
                onFailure = { regenerateLevel() },
            )
        }
    }

    fun restartLevel() {
        initialLevel?.let {
            level = it
            totalSnakesInLevel = it.snakes.size
            isGameWon = false
            lives = maxLives
            transformationState.reset()
            tapHandler.clearFlash()
            removalAnimator.clear()
            entryAnimator.clear()
            saveState()
            animateEntry()
        }
    }

    fun showHint() {
        transformationState.reset()
        SolvabilityChecker.findRemovableSnake(level)?.let { removableId ->
            tapHandler.flashSnake(removableId)
        }
    }

    fun onTransform(
        pan: Offset,
        zoom: Float,
    ) = transformationState.transform(pan, zoom)

    fun addLife() {
        if (lives < maxLives) {
            lives++
            saveState()
        }
    }

    fun onTap(
        tapOffset: Offset,
        containerWidthPx: Float,
        containerHeightPx: Float,
    ) {
        if (isLoading || lives <= 0 || isEntryAnimating) return
        val gridCoords =
            inputHandler.transformTapToGrid(
                TapTransformationParams(
                    tapOffset,
                    containerWidthPx,
                    containerHeightPx,
                    level,
                    scale,
                    offsetX,
                    offsetY,
                ),
            )
        val tappedSnake =
            inputHandler.findTappedSnake(gridCoords.x, gridCoords.y, level.snakes) {
                SolvabilityChecker.isLineOfSightObstructed(
                    level,
                    it,
                    removalAnimator.removalProgress.keys,
                )
            }
        if (tappedSnake != null) {
            val isObstructed =
                SolvabilityChecker.isLineOfSightObstructed(
                    level,
                    tappedSnake,
                    removalAnimator.removalProgress.keys,
                )
            tapHandler.handleSnakeTap(
                TapParams(
                    tappedSnake,
                    isVibrationEnabled,
                    isObstructed,
                    lives,
                    onPenalty = {
                        lives--
                        saveState()
                    },
                    onSuccess = {
                        if (!removalProgress.containsKey(tappedSnake.id)) {
                            removalAnimator.animate(
                                tappedSnake.id,
                                animationSpeed,
                                { _, _ -> },
                                { id ->
                                    onSnakeRemoved(id)
                                },
                            )
                        }
                    },
                ),
            )
        }
    }

    private fun onSnakeRemoved(id: Int) {
        level = level.copy(snakes = level.snakes.filter { it.id != id })
        if (level.snakes.isEmpty()) {
            isGameWon = true
            soundManager?.playGameWon()
            // Clear saved game for both custom and regular games
            coroutineScope.launch(backgroundDispatcher) {
                if (isCustomGame) {
                    // For custom games, only clear the save without advancing level
                    levelManager.clearSavedGame()
                } else {
                    // For regular games, advance to next level and clear the save
                    levelManager.advanceLevel(levelNumber)
                }
            }
        } else {
            soundManager?.playSnakeRemoved()
            saveState()
        }
    }

    fun regenerateLevel() {
        isLoading = true
        loadingProgress = 0f
        coroutineScope.launch(backgroundDispatcher) {
            levelManager.regenerateLevel(
                RegenerationParams(
                    forcedWidth,
                    forcedHeight,
                    forcedLives,
                    forcedShape,
                    isCustomGame,
                    onProgress = { loadingProgress = it },
                    onComplete = { newLevel, config ->
                        initialLevel = newLevel
                        level = newLevel
                        totalSnakesInLevel =
                            newLevel.snakes.size
                        isGameWon = false
                        maxLives = config.maxLives
                        lives = config.maxLives
                        transformationState.reset()
                        tapHandler.clearFlash()
                        removalAnimator.clear()
                        entryAnimator.clear()
                        isLoading = false
                        animateEntry()
                        coroutineScope.launch(backgroundDispatcher) {
                            levelManager.saveInitialState(newLevel, lives)
                        }
                    },
                ),
            )
        }
    }

    private fun animateEntry() {
        entryAnimator.animate(level.snakes, level.width, level.height)
    }

    private fun saveState() {
        coroutineScope.launch(backgroundDispatcher) { levelManager.saveState(level, lives) }
    }

    class Factory(
        private val config: GameEngineConfig,
        private val features: GameEngineFeatures = GameEngineFeatures(),
    ) : ViewModelProvider.Factory {
        // Cast is safe: guarded by isAssignableFrom check above.
        // Required boilerplate for manual ViewModelProvider.Factory without a DI framework.
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameEngine::class.java)) {
                return GameEngine(config, features) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
