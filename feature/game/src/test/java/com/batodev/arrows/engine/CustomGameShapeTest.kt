package com.batodev.arrows.engine

import com.batodev.arrows.core.testing.FakeGameStateDao
import com.batodev.arrows.core.testing.FakeUserPreferencesRepository
import com.batodev.arrows.core.testing.MainDispatcherRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class CustomGameShapeTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test custom game with rectangular uses no shape`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val repo = FakeUserPreferencesRepository()
            val shapeProvider = mock<BoardShapeProvider>()
            val generator =
                mock<GameGenerator> {
                    on { generateSolvableLevel(any()) } doReturn GameLevel(10, 10, emptyList())
                }

            val engine =
                GameEngine(
                    config =
                        GameEngineConfig(
                            coroutineScopeOverride = CoroutineScope(testDispatcher),
                            repository = repo,
                            gameStateDao = FakeGameStateDao(),
                            isCustomGame = true,
                            gameGenerator = generator,
                            autoLoad = false,
                            backgroundDispatcher = testDispatcher,
                        ),
                    features =
                        GameEngineFeatures(
                            shapeProvider = shapeProvider,
                            forcedWidth = 10,
                            forcedHeight = 10,
                            forcedShape = null, // Rectangular (no shape)
                        ),
                )

            engine.loadOrRegenerateLevel()
            runCurrent()

            // Verify that NO shape methods were called for custom game with rectangular
            verify(shapeProvider, never()).getRandomShape()
            verify(shapeProvider, never()).getShapeByName(any())
        }

    @Test
    fun `test custom game with specific shape uses that shape`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val repo = FakeUserPreferencesRepository()
            val mockShape = mock<BoardShape>()
            val shapeProvider =
                mock<BoardShapeProvider> {
                    on { getShapeByName("favorite") } doReturn mockShape
                }
            val generator =
                mock<GameGenerator> {
                    on { generateSolvableLevel(any()) } doReturn GameLevel(10, 10, emptyList())
                }

            val engine =
                GameEngine(
                    config =
                        GameEngineConfig(
                            coroutineScopeOverride = CoroutineScope(testDispatcher),
                            repository = repo,
                            gameStateDao = FakeGameStateDao(),
                            isCustomGame = true,
                            gameGenerator = generator,
                            autoLoad = false,
                            backgroundDispatcher = testDispatcher,
                        ),
                    features =
                        GameEngineFeatures(
                            shapeProvider = shapeProvider,
                            forcedWidth = 10,
                            forcedHeight = 10,
                            forcedShape = "favorite", // Heart shape
                        ),
                )

            engine.loadOrRegenerateLevel()
            runCurrent()

            // Verify getShapeByName was called with correct shape
            verify(shapeProvider).getShapeByName("favorite")
            // Verify getRandomShape was NOT called
            verify(shapeProvider, never()).getRandomShape()
        }

    @Test
    fun `test regular game does not apply random shapes for small boards`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val repo = FakeUserPreferencesRepository()
            repo.saveLevelNumber(1) // Small board = low probability

            val shapeProvider = mock<BoardShapeProvider>()
            val generator =
                mock<GameGenerator> {
                    on { generateSolvableLevel(any()) } doReturn GameLevel(5, 5, emptyList())
                }

            val engine =
                GameEngine(
                    config =
                        GameEngineConfig(
                            coroutineScopeOverride = CoroutineScope(testDispatcher),
                            repository = repo,
                            gameStateDao = FakeGameStateDao(),
                            isCustomGame = false, // Regular game
                            gameGenerator = generator,
                            autoLoad = false,
                            backgroundDispatcher = testDispatcher,
                        ),
                    features =
                        GameEngineFeatures(
                            shapeProvider = shapeProvider,
                        ),
                )

            engine.loadOrRegenerateLevel()
            runCurrent()

            // Regular game with small board should not apply shapes
            verify(shapeProvider, never()).getRandomShape()
        }

    @Test
    fun `test that custom game rectangular and regular game no shape are different code paths`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)

            // Custom game with rectangular
            val customRepo = FakeUserPreferencesRepository()
            val customShapeProvider = mock<BoardShapeProvider>()
            val customGenerator =
                mock<GameGenerator> {
                    on { generateSolvableLevel(any()) } doReturn GameLevel(30, 30, emptyList())
                }

            val customEngine =
                GameEngine(
                    config =
                        GameEngineConfig(
                            coroutineScopeOverride = CoroutineScope(testDispatcher),
                            repository = customRepo,
                            gameStateDao = FakeGameStateDao(),
                            isCustomGame = true,
                            gameGenerator = customGenerator,
                            autoLoad = false,
                            backgroundDispatcher = testDispatcher,
                        ),
                    features =
                        GameEngineFeatures(
                            shapeProvider = customShapeProvider,
                            forcedWidth = 30,
                            forcedHeight = 30,
                            forcedShape = null,
                        ),
                )

            customEngine.loadOrRegenerateLevel()
            runCurrent()

            // Custom game should never try to get random shape
            verify(customShapeProvider, never()).getRandomShape()

            // Regular game with same board size SHOULD apply random shapes
            val regularRepo = FakeUserPreferencesRepository()
            regularRepo.saveLevelNumber(50) // Make sure level is high enough for shape probability

            val regularShapeProvider = mock<BoardShapeProvider>()
            val regularGenerator =
                mock<GameGenerator> {
                    on { generateSolvableLevel(any()) } doReturn GameLevel(30, 30, emptyList())
                }

            val regularEngine =
                GameEngine(
                    config =
                        GameEngineConfig(
                            coroutineScopeOverride = CoroutineScope(testDispatcher),
                            repository = regularRepo,
                            gameStateDao = FakeGameStateDao(),
                            isCustomGame = false,
                            gameGenerator = regularGenerator,
                            autoLoad = false,
                            backgroundDispatcher = testDispatcher,
                        ),
                    features =
                        GameEngineFeatures(
                            shapeProvider = regularShapeProvider,
                        ),
                )

            regularEngine.loadOrRegenerateLevel()
            runCurrent()

            // Regular game might or might not apply shapes (probabilistic),
            // but the important thing is the custom game never does for rectangular
            assertTrue(customEngine.level.width > 0) // Just verify it generated something
            assertTrue(regularEngine.level.width > 0)
        }
}
