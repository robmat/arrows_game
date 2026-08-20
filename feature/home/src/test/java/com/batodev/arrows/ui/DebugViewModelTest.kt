package com.batodev.arrows.ui

import com.batodev.arrows.core.testing.FakeUserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DebugViewModelTest {
    private val repository = FakeUserPreferencesRepository()
    private lateinit var viewModel: DebugViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = DebugViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test saveDebugOption width updates state`() =
        runTest {
            val collectJob =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.debugForcedWidth.collect {}
                }

            viewModel.saveDebugOption(DebugViewModel.DebugOption.WIDTH, 10)
            assertEquals(10, repository.debugForcedWidthFlow.value)
            assertEquals(10, viewModel.debugForcedWidth.value)

            collectJob.cancel()
        }

    @Test
    fun `test saveDebugOption shape updates state`() =
        runTest {
            val collectJob =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.debugForcedShape.collect {}
                }

            viewModel.saveDebugOption(DebugViewModel.DebugOption.SHAPE, "heart")
            assertEquals("heart", repository.debugForcedShapeFlow.value)
            assertEquals("heart", viewModel.debugForcedShape.value)

            collectJob.cancel()
        }
}
