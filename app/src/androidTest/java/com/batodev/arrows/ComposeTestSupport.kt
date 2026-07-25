package com.batodev.arrows

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.batodev.arrows.data.GameLevelData
import com.batodev.arrows.data.GameStateDao
import com.batodev.arrows.data.IUserPreferencesRepository
import com.batodev.arrows.engine.Direction
import com.batodev.arrows.engine.GameLevel
import com.batodev.arrows.engine.Point
import com.batodev.arrows.engine.Snake
import com.batodev.arrows.engine.SolvabilityChecker
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

// Shared across HomeScreenTest/GameScreenTest/SettingsScreenTest/GenerateScreenTest.
//
// This is a fully Jetpack Compose + Appyx (node-based navigation) app - there is no
// XML/View tree at all, so every other app's onView(withId(...)) toolkit doesn't apply
// here. Screens are addressed via Compose's own test APIs (onNodeWithText /
// onNodeWithTag) instead.

@PublishedApi
internal object KoinAccess : KoinComponent

/** Typed access to any real singleton wired into the app's global Koin graph. */
inline fun <reified T : Any> koinInstance(): T = KoinAccess.get<T>()

/**
 * Resets the Room-backed preferences/save state (see data/di/DataModule.kt) to a known
 * baseline: no saved-in-progress level, level 1, ad-free (so ad-network variability
 * can't affect navigation), win celebration videos off (so a win returns to Home in a
 * few seconds instead of playing a real video).
 *
 * Deliberately does *not* force an Activity recreate() to pick the change up - every
 * affected value is read through a Room Flow + collectAsState() in the already-running
 * screen, which already reacts to the write on its own via Room's invalidation tracker.
 * (recreate() was tried first and reintroduced real Espresso.pressBack()
 * RootViewWithoutFocusException flakiness afterwards - the OS window manager doesn't
 * reliably hand focus back to the recreated window within any fixed margin - so callers
 * instead wait for the specific post-reset UI they need, the same as any other
 * async-settling condition in these tests.)
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.resetAppState() {
    // UserPreferencesDao's Koin `single {}` fires an async "insert default row if
    // absent" coroutine the first time anything resolves it - by @Before time that's
    // already been triggered by the rule's own MainActivity launch (AppViewModel is
    // read during its first composition), but this margin protects against the residual
    // race of that insert not having landed yet.
    Thread.sleep(300)

    val gameStateDao = KoinAccess.get<GameStateDao>()
    val repository = KoinAccess.get<IUserPreferencesRepository>()
    runBlocking {
        gameStateDao.clearAllSavedLevels()
        repository.saveLevelNumber(1)
        repository.saveIsAdFree(true)
        repository.saveWinVideosEnabled(false)
        // Room (unlike most other apps' SharedPreferences) survives across separate
        // `connectedDebugAndroidTest` invocations, not just across test methods within
        // one - this project doesn't set clearPackageData - so anything a test both
        // changes AND asserts a specific before/after value for has to be pinned back to
        // a known baseline here too, or it silently depends on what an earlier run (or
        // even a previous run of the same test) last left behind.
        repository.saveVibrationPreference(true)
        repository.saveThemePreference("Green")
    }
    waitForIdle()
}

/** GeneratorNavigationItem (AppNavigationBar.kt) stays locked below GENERATOR_UNLOCK_LEVEL (20). */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.unlockGenerator() {
    val repository = KoinAccess.get<IUserPreferencesRepository>()
    runBlocking { repository.saveLevelNumber(GameConstants.GENERATOR_UNLOCK_LEVEL) }
    waitForIdle()
}

/**
 * Deliberately drives back-navigation through the real OnBackPressedDispatcher
 * (matching how Appyx's NodeHost/BackStack itself intercepts back, same as every
 * predictive-back-aware screen) rather than Espresso.pressBack() - Home's two
 * `rememberInfiniteTransition` animations (logo rotation, button pulse) mean Espresso's
 * own IdlingResource-based root picker (EspressoLink) never reports idle on that screen,
 * so pressBack() reliably hits RootViewWithoutFocusException after its 10s wait even
 * though the window is genuinely focused and responsive to everything Compose's own
 * (infinite-animation-aware) test synchronization drives.
 */
fun <A : ComponentActivity> assertBackPressFinishesScenario(scenario: ActivityScenario<A>) {
    scenario.onActivity { activity -> activity.onBackPressedDispatcher.onBackPressed() }
    val deadline = System.currentTimeMillis() + 8_000
    while (scenario.state != Lifecycle.State.DESTROYED && System.currentTimeMillis() < deadline) {
        Thread.sleep(50)
    }
    assertEquals(Lifecycle.State.DESTROYED, scenario.state)
}

private fun GameLevelData.toDomain(): GameLevel = GameLevel(
    width = width,
    height = height,
    snakes = snakes.map { s ->
        Snake(
            id = s.id,
            body = s.bodyPoints.map { Point(it.x, it.y) },
            headDirection = Direction.valueOf(s.headDirection)
        )
    }
)

/**
 * GameEngine persists the level being played to Room under stateType "CURRENT"
 * (GameStateDao.getCurrentBoardCount()) as soon as it's (re)generated, slightly before
 * the UI itself finishes its loading transition - polled rather than read once since
 * that save happens on a background dispatcher.
 */
private suspend fun awaitCurrentLevel(gameStateDao: GameStateDao, timeoutMs: Long = 15_000): GameLevel {
    val deadline = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < deadline) {
        val data = gameStateDao.loadGameLevel("CURRENT")
        if (data != null && data.snakes.isNotEmpty()) return data.toDomain()
        delay(200)
    }
    error("No CURRENT level appeared in GameStateDao within ${timeoutMs}ms")
}

/**
 * Plays a real, freshly-(re)generated level to completion by tapping each snake for
 * real through the GameArea composable (GAME_AREA_TEST_TAG, GameScreen.kt) - not a
 * shortcut/mock of the win condition.
 *
 * The removal order is precomputed up front with the exact same SolvabilityChecker the
 * production Hint button and level generator use (a snake is safely tappable once
 * nothing remains in front of it), then each tap's on-screen position is derived with
 * the exact inverse of InputHandler.transformTapToGrid's math. That inversion assumes
 * scale=1/offset=(0,0), true only while the board's pan/zoom is at its reset default -
 * always true right after a level (re)loads, since nothing in this flow ever pans/zooms.
 */
fun ComposeTestRule.solveCurrentLevel(gameStateDao: GameStateDao) {
    waitUntil(15_000) { onAllNodesWithTag(GAME_AREA_TEST_TAG).fetchSemanticsNodes().isNotEmpty() }
    val level = runBlocking { awaitCurrentLevel(gameStateDao) }

    val boardNode: SemanticsNodeInteraction = onNodeWithTag(GAME_AREA_TEST_TAG)
    val containerSize = boardNode.fetchSemanticsNode().size
    val containerWidth = containerSize.width.toFloat()
    val containerHeight = containerSize.height.toFloat()

    val cellSize = minOf(containerWidth / level.width, containerHeight / level.height)
    val boardWidth = cellSize * level.width
    val boardHeight = cellSize * level.height
    val leftOffset = (containerWidth - boardWidth) / 2
    val topOffset = (containerHeight - boardHeight) / 2

    val remaining = level.snakes.toMutableList()
    while (remaining.isNotEmpty()) {
        val workingLevel = level.copy(snakes = remaining)
        val removableId = SolvabilityChecker.findRemovableSnake(workingLevel)
            ?: error("Level not solvable from current remaining snake set - ${remaining.size} left: $remaining")
        val snake = remaining.first { it.id == removableId }
        val head = snake.body.first()
        val gridX = head.x + GameConstants.CELL_CENTER +
            snake.headDirection.dx * GameConstants.TAP_AREA_OFFSET_FACTOR
        val gridY = head.y + GameConstants.CELL_CENTER +
            snake.headDirection.dy * GameConstants.TAP_AREA_OFFSET_FACTOR
        val screenX = gridX * cellSize + leftOffset
        val screenY = gridY * cellSize + topOffset

        boardNode.performTouchInput { click(Offset(screenX, screenY)) }
        waitForIdle()
        // Outlasts the removal animation (up to REMOVAL_DURATION_LOW = 900ms) so the
        // next tap's obstruction check always sees a clean, settled board.
        Thread.sleep(1_000)

        remaining.removeAll { it.id == removableId }
    }
    waitForIdle()
}
