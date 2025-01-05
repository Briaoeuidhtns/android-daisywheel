package io.github.briaoeuidhtns.daisywheel

import android.inputmethodservice.InputMethodService
import android.os.Debug
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.hypot

class DaisywheelInputMethodService : InputMethodService(), ViewModelStoreOwner, LifecycleOwner,
    SavedStateRegistryOwner {

    override val viewModelStore by lazy { ViewModelStore() }
    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }
    private val savedStateRegistryController by lazy { SavedStateRegistryController.create(this) }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()

        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        val viewModel = ViewModelProvider(this, DaisywheelViewModel.Factory)[DaisywheelViewModel::class.java]

        lifecycleScope.launch {
            viewModel.characterSelected.collect { char ->
                currentInputConnection?.commitText(char.toString(), 1)
            }
        }
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }

        return ComposeView(this).apply {
            setContent {
                DaisywheelKeyboardScreen()
            }
        }
    }

    @Composable
    private fun DaisywheelKeyboardScreen(
        viewModel: DaisywheelViewModel = viewModel(factory = DaisywheelViewModel.Factory)
    ) {
        Debug.waitForDebugger()
        DaisyWheelKeyboard(
            viewModel = viewModel,
        )
    }

    companion object {
        private const val JOYSTICK_DEADZONE = 0.5f

        /**
         * Calculates the petal index from joystick coordinates.
         * @param x X-axis position (-1 to 1)
         * @param y Y-axis position (-1 to 1)
         * @return Petal index (0-7) or -1 if within deadzone
         */
        fun calculatePetalIndex(x: Float, y: Float): Int {
            val magnitude = hypot(x, y)
            if (magnitude <= JOYSTICK_DEADZONE) return -1

            // Calculate angle in radians, starting from right (0°)
            val angle = atan2(y, x)
            // Convert to degrees and normalize to 0-360
            val degrees = Math.toDegrees(angle.toDouble())
            val normalizedDegrees = (degrees + 360.0) % 360.0
            
            // Find the closest petal angle
            // Layout: Top(270°)=0, TopRight(315°)=1, Right(0°)=2, BottomRight(45°)=3,
            //        Bottom(90°)=4, BottomLeft(135°)=5, Left(180°)=6, TopLeft(225°)=7
            return when {
                normalizedDegrees >= 337.5 || normalizedDegrees < 22.5 -> 2  // Right
                normalizedDegrees < 67.5 -> 3   // Bottom-Right
                normalizedDegrees < 112.5 -> 4  // Bottom
                normalizedDegrees < 157.5 -> 5  // Bottom-Left
                normalizedDegrees < 202.5 -> 6  // Left
                normalizedDegrees < 247.5 -> 7  // Top-Left
                normalizedDegrees < 292.5 -> 0  // Top
                else -> 1  // Top-Right (292.5-337.5)
            }
        }

        /**
         * Processes a motion event to extract joystick position.
         * @param event The motion event to process
         * @return Pair of (isJoystickMove, petalIndex) where petalIndex is -1 if not applicable
         */
        fun processJoystickEvent(event: MotionEvent): Pair<Boolean, Int> {
            if (event.source and InputDevice.SOURCE_JOYSTICK != InputDevice.SOURCE_JOYSTICK) {
                return Pair(false, -1)
            }

            if (event.action != MotionEvent.ACTION_MOVE) {
                return Pair(false, -1)
            }

            val xAxis = event.getAxisValue(MotionEvent.AXIS_X)
            val yAxis = event.getAxisValue(MotionEvent.AXIS_Y)
            return Pair(true, calculatePetalIndex(xAxis, yAxis))
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        val (isJoystickMove, petalIndex) = processJoystickEvent(event)
        if (!isJoystickMove) return false

        val viewModel = ViewModelProvider(this, DaisywheelViewModel.Factory)[DaisywheelViewModel::class.java]
        viewModel.selectPetal(petalIndex)
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val viewModel = ViewModelProvider(this, DaisywheelViewModel.Factory)[DaisywheelViewModel::class.java]
        
        return when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> {
                viewModel.selectChar(2)
                true
            }
            KeyEvent.KEYCODE_BUTTON_B -> {
                viewModel.selectChar(3)
                true
            }
            KeyEvent.KEYCODE_BUTTON_X -> {
                viewModel.selectChar(1)
                true
            }
            KeyEvent.KEYCODE_BUTTON_Y -> {
                viewModel.selectChar(0)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        viewModelStore.clear()
    }

    override fun onWindowShown() {
        super.onWindowShown()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }
}