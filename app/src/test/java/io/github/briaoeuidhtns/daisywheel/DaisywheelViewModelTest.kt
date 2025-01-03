package io.github.briaoeuidhtns.daisywheel

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class DaisywheelViewModelTest {
    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private lateinit var viewModel: DaisywheelViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DaisywheelViewModel()
    }

    @Test
    fun `initial state has no selection`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(-1, initialState.selectedPetalIndex)
            assertEquals(defaultLayout, initialState.petals)
        }
    }

    @Test
    fun `selecting petal updates state`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.selectPetal(0)
            val state = awaitItem()
            assertEquals(0, state.selectedPetalIndex)
        }
    }

    @Test
    fun `selecting character emits correct character`() = runTest {
        viewModel.characterSelected.test {
            viewModel.selectPetal(0)
            viewModel.selectChar(0)
            assertEquals('a', awaitItem())
        }
    }

    @Test
    fun `enabling modifier updates modifier state`() = runTest {
        viewModel.modifiers.test {
            assertTrue(awaitItem().isEmpty())
            viewModel.enableModifier(DaisywheelModifier.SHIFT)
            val modifiers = awaitItem()
            assertTrue(modifiers.contains(DaisywheelModifier.SHIFT))
        }
    }

    @Test
    fun `disabling modifier removes it from state`() = runTest {
        viewModel.modifiers.test {
            awaitItem()
            viewModel.enableModifier(DaisywheelModifier.SHIFT)
            awaitItem()
            viewModel.enableModifier(DaisywheelModifier.SHIFT, false)
            val modifiers = awaitItem()
            assertFalse(modifiers.contains(DaisywheelModifier.SHIFT))
        }
    }
}