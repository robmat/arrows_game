package com.batodev.arrows.engine

import com.batodev.arrows.core.testing.FakeGameStateDao
import com.batodev.arrows.core.testing.FakeUserPreferencesRepository
import com.batodev.arrows.core.testing.MainDispatcherRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class GameEngineShapeLogicTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test getRandomShape is called for large boards when probability rolls high`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val repo = FakeUserPreferencesRepository()
            repo.saveLevelNumber(100) // Large board

            val shapeProvider = mock<BoardShapeProvider>()
            val generator =
                mock<GameGenerator> {
                    on { generateSolvableLevel(any()) } doReturn GameLevel(20, 20, emptyList())
                }
            val random =
                mock<Random> {
                    on { nextFloat() } doReturn 0.5f // Less than 0.6
                }

            val engine =
                GameEngine(
                    config =
                        GameEngineConfig(
                            coroutineScopeOverride = CoroutineScope(testDispatcher),
                            repository = repo,
                            gameStateDao = FakeGameStateDao(),
                            gameGenerator = generator,
                            autoLoad = false,
                            backgroundDispatcher = testDispatcher,
                        ),
                    features =
                        GameEngineFeatures(
                            shapeProvider = shapeProvider,
                            random = random,
                        ),
                )

            runCurrent()
            engine.regenerateLevel()
            runCurrent()

            verify(shapeProvider, times(1)).getRandomShape()
        }

    @Test
    fun `test getRandomShape is NOT called for small boards`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val repo = FakeUserPreferencesRepository()
            repo.saveLevelNumber(1) // 5x5 board

            val shapeProvider = mock<BoardShapeProvider>()
            val generator =
                mock<GameGenerator> {
                    on { generateSolvableLevel(any()) } doReturn GameLevel(20, 20, emptyList())
                }
            val random =
                mock<Random> {
                    on { nextFloat() } doReturn 0.1f
                }

            val engine =
                GameEngine(
                    config =
                        GameEngineConfig(
                            coroutineScopeOverride = CoroutineScope(testDispatcher),
                            repository = repo,
                            gameStateDao = FakeGameStateDao(),
                            gameGenerator = generator,
                            autoLoad = false,
                            backgroundDispatcher = testDispatcher,
                        ),
                    features =
                        GameEngineFeatures(
                            shapeProvider = shapeProvider,
                            random = random,
                        ),
                )

            runCurrent()
            engine.regenerateLevel()
            runCurrent()

            verify(shapeProvider, never()).getRandomShape()
        }

    @Test
    fun `test getRandomShape is NOT called when probability rolls low`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val repo = FakeUserPreferencesRepository()
            repo.saveLevelNumber(100) // Large board

            val shapeProvider = mock<BoardShapeProvider>()
            val generator =
                mock<GameGenerator> {
                    on { generateSolvableLevel(any()) } doReturn GameLevel(20, 20, emptyList())
                }
            val random =
                mock<Random> {
                    on { nextFloat() } doReturn 0.7f // More than 0.6
                }

            val engine =
                GameEngine(
                    config =
                        GameEngineConfig(
                            coroutineScopeOverride = CoroutineScope(testDispatcher),
                            repository = repo,
                            gameStateDao = FakeGameStateDao(),
                            gameGenerator = generator,
                            autoLoad = false,
                            backgroundDispatcher = testDispatcher,
                        ),
                    features =
                        GameEngineFeatures(
                            shapeProvider = shapeProvider,
                            random = random,
                        ),
                )

            runCurrent()
            engine.regenerateLevel()
            runCurrent()

            verify(shapeProvider, never()).getRandomShape()
        }

    @Test
    fun `test getRandomShape is ALWAYS called for very large boards (100x100)`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val repo = FakeUserPreferencesRepository()

            // Force 100x100 board
            repo.saveDebugForcedWidth(100)
            repo.saveDebugForcedHeight(100)

            val shapeProvider = mock<BoardShapeProvider>()
            val generator =
                mock<GameGenerator> {
                    on { generateSolvableLevel(any()) } doReturn GameLevel(100, 100, emptyList())
                }
            val random =
                mock<Random> {
                    on { nextFloat() } doReturn 0.999f // Almost 1.0
                }

            val engine =
                GameEngine(
                    config =
                        GameEngineConfig(
                            coroutineScopeOverride = CoroutineScope(testDispatcher),
                            repository = repo,
                            gameStateDao = FakeGameStateDao(),
                            gameGenerator = generator,
                            autoLoad = false,
                            backgroundDispatcher = testDispatcher,
                        ),
                    features =
                        GameEngineFeatures(
                            shapeProvider = shapeProvider,
                            random = random,
                        ),
                )

            runCurrent()
            engine.regenerateLevel()
            runCurrent()

            verify(shapeProvider, times(1)).getRandomShape()
        }
}
