package io.github.briaoeuidhtns.daisywheel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

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
        turbineScope {
            val stateTurbine = viewModel.state.testIn(backgroundScope)
            val charTurbine = viewModel.characterSelected.testIn(backgroundScope)
            viewModel.selectPetal(0)
            stateTurbine.awaitItem()
            viewModel.selectChar(0)
            assertEquals('a', charTurbine.awaitItem())
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

    @Test
    fun `requesting backspace emits event`() = runTest {
        viewModel.backspaceRequested.test {
            // Ensure no initial events
            expectNoEvents()

            // Request backspace
            viewModel.requestBackspace()

            // Verify event was emitted
            awaitItem()

            // Ensure no more events
            expectNoEvents()
        }
    }
}