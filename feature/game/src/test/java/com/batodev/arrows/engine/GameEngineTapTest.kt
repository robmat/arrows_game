package com.batodev.arrows.engine

import androidx.compose.ui.geometry.Offset
import com.batodev.arrows.core.testing.FakeGameStateDao
import com.batodev.arrows.core.testing.FakeUserPreferencesRepository
import com.batodev.arrows.data.PointData
import com.batodev.arrows.data.SnakeSaveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameEngineTapTest {
    private fun GameLevel.toSaveData(): List<SnakeSaveData> =
        snakes.map { snake ->
            SnakeSaveData(
                id = snake.id,
                headDirection = snake.headDirection.name,
                bodyPoints = snake.body.map { PointData(it.x, it.y) },
            )
        }

    private suspend fun saveLevelToDao(
        dao: FakeGameStateDao,
        level: GameLevel,
    ) {
        val saveData = level.toSaveData()
        dao.saveGameLevel("INITIAL", level.width, level.height, saveData)
        dao.saveGameLevel("CURRENT", level.width, level.height, saveData)
    }

    @Test
    fun `test closest snake is selected within tolerance`() =
        runTest {
            val snake1 = Snake(1, listOf(Point(1, 1)), Direction.RIGHT)
            val snake2 = Snake(2, listOf(Point(3, 1)), Direction.LEFT)
            val level = GameLevel(5, 5, listOf(snake1, snake2))

            val repo = FakeUserPreferencesRepository()
            val fakeDao = FakeGameStateDao()
            saveLevelToDao(fakeDao, level)
            repo.saveVibrationPreference(false)
            repo.saveSoundsPreference(true)
            repo.saveFillBoardPreference(false)
            repo.saveLevelNumber(1)
            repo.saveCurrentLives(5)
            repo.saveAnimationSpeed("Medium")

            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val engine =
                GameEngine(
                    config =
                        GameEngineConfig(
                            coroutineScopeOverride = CoroutineScope(testDispatcher),
                            repository = repo,
                            gameStateDao = fakeDao,
                            autoLoad = false,
                            backgroundDispatcher = testDispatcher,
                        ),
                )

            // Load level
            engine.loadOrRegenerateLevel()
            runCurrent()
            testScheduler.advanceTimeBy(1000)
            runCurrent()

            // Ensure level loaded
            assertEquals(2, engine.level.snakes.size)

            // Board setup
            val boardSize = 500f
            val cellWidth = boardSize / 5f // 100f

            val tapOffset = Offset(2.4f * cellWidth, 1.5f * cellWidth)
            engine.onTap(tapOffset, boardSize, boardSize)

            // Check if S1 flashed (id 1)
            assertEquals("Should have picked snake 1", 1, engine.flashingSnakeId)

            // Reset flashing
            engine.restartLevel()
            runCurrent()
            testScheduler.advanceTimeBy(1000)
            runCurrent()

            val tapOffset2 = Offset(2.6f * cellWidth, 1.5f * cellWidth)
            engine.onTap(tapOffset2, boardSize, boardSize)

            // S2 should flash (id 2)
            assertEquals("Should have picked snake 2", 2, engine.flashingSnakeId)
        }

    @Test
    fun `test animating snake does not obstruct others`() =
        runTest {
            val s1 = Snake(1, listOf(Point(1, 1)), Direction.RIGHT)
            val s2 = Snake(2, listOf(Point(3, 1)), Direction.RIGHT)
            val level = GameLevel(5, 5, listOf(s1, s2))

            val repo = FakeUserPreferencesRepository()
            val fakeDao = FakeGameStateDao()
            saveLevelToDao(fakeDao, level)
            repo.saveVibrationPreference(false)
            repo.saveSoundsPreference(false)
            repo.saveFillBoardPreference(false)
            repo.saveLevelNumber(1)
            repo.saveCurrentLives(5)
            repo.saveAnimationSpeed("Medium")

            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val engine =
                GameEngine(
                    config =
                        GameEngineConfig(
                            coroutineScopeOverride = CoroutineScope(testDispatcher),
                            repository = repo,
                            gameStateDao = fakeDao,
                            autoLoad = false,
                            backgroundDispatcher = testDispatcher,
                        ),
                )

            engine.loadOrRegenerateLevel()
            runCurrent()
            testScheduler.advanceTimeBy(1000)
            runCurrent()

            assertEquals("Engine should not be loading", false, engine.isLoading)
            assertEquals("Level should have 2 snakes", 2, engine.level.snakes.size)

            // Board setup
            val boardSize = 500f
            val cellWidth = boardSize / 5f // 100f

            // Tap S1 (obstructed by S2)
            val tapS1 = Offset(1.8f * cellWidth, 1.5f * cellWidth)
            engine.onTap(tapS1, boardSize, boardSize)

            assertEquals("S1 should flash when obstructed", 1, engine.flashingSnakeId)
            engine.restartLevel()
            runCurrent()
            testScheduler.advanceTimeBy(1000)
            runCurrent()

            // Tap S2 (not obstructed)
            val tapS2 = Offset(3.8f * cellWidth, 1.5f * cellWidth)
            engine.onTap(tapS2, boardSize, boardSize)

            // S2 should be in removal progress
            assert(engine.removalProgress.containsKey(2))

            // Now tap S1 again while S2 is animating
            engine.onTap(tapS1, boardSize, boardSize)

            // S1 should NOT flash, and it should now be in removal progress because S2 is ignored
            assertEquals("S1 should not flash", null, engine.flashingSnakeId)
            assert(engine.removalProgress.containsKey(1))
        }
}
