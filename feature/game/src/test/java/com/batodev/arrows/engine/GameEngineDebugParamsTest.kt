package com.batodev.arrows.engine

import com.batodev.arrows.core.testing.FakeGameStateDao
import com.batodev.arrows.core.testing.FakeUserPreferencesRepository
import com.batodev.arrows.core.testing.MainDispatcherRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class GameEngineDebugParamsTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test forced debug parameters override calculation`() =
        runTest {
            // Since we moved logic to LevelProgression, we test it directly
            val config =
                LevelProgression.calculateLevelConfiguration(
                    levelNum = 1,
                    forcedWidth = 20,
                    forcedHeight = 30,
                    forcedLives = 10,
                )

            assertEquals(20, config.width)
            assertEquals(30, config.height)
            assertEquals(10, config.maxLives)
        }

    @Test
    fun `test forced shape is used in regeneration`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val repo = FakeUserPreferencesRepository()
            repo.saveDebugForcedShape("heart")

            val shapeProvider = mock<BoardShapeProvider>()
            val generator =
                mock<GameGenerator> {
                    on { generateSolvableLevel(org.mockito.kotlin.any()) } doReturn GameLevel(20, 20, emptyList())
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
                        ),
                )

            runCurrent()
            engine.regenerateLevel()
            runCurrent()

            verify(shapeProvider).getShapeByName("heart")
            verify(shapeProvider, never()).getRandomShape()
        }
}
