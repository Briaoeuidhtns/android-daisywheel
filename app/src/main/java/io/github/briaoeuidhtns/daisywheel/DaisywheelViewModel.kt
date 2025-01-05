package io.github.briaoeuidhtns.daisywheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.*
import java.util.EnumSet

data class DaisyPetal(
    val characters: List<Char>,
    val angle: Float,
    val isSelected: Boolean = false,
)

data class DaisywheelState(
    val selectedPetalIndex: Int = -1,
    val petals: List<DaisyPetal> = defaultLayout,
)

var defaultLayout =
        listOf(
            DaisyPetal(listOf('a', 'b', 'c', 'd'), 270f), // Top
            DaisyPetal(listOf('e', 'f', 'g', 'h'), 315f), // Top-right
            DaisyPetal(listOf('i', 'j', 'k', 'l'), 0f), // Right
            DaisyPetal(listOf('m', 'n', 'o', 'p'), 45f), // Bottom-right
            DaisyPetal(listOf('q', 'r', 's', 't'), 90f), // Bottom
            DaisyPetal(listOf('u', 'v', 'w', 'x'), 135f), // Bottom-left
            DaisyPetal(listOf('y', 'z', ',', '.'), 180f), // Left
            DaisyPetal(listOf(':', '/', '@', '-'), 225f), // Top-left
        )

enum class DaisywheelModifier {
    SHIFT,
    ALT,
}

class DaisywheelViewModel : ViewModel() {
    private val _petalSelected = MutableSharedFlow<Int>(replay = 1)
    private val _charSelected = MutableSharedFlow<Int>(replay = 1)
    private val _modifierSelected = MutableSharedFlow<Pair<DaisywheelModifier, Boolean>>(replay = 1)
    
    val modifiers = _modifierSelected
        .scan(EnumSet.noneOf(DaisywheelModifier::class.java)) { s, (modifier, enabled) -> s.apply {
                if (enabled) {
                    plus(modifier)
                } else {
                    minus(modifier)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EnumSet.noneOf(DaisywheelModifier::class.java)
        )

    val state: StateFlow<DaisywheelState> = _petalSelected
        .onStart { emit(-1) }  // Emit initial selection
        .combine(modifiers.map {
            defaultLayout
        }) { petal, layout -> 
            DaisywheelState(
                selectedPetalIndex = petal,
                petals = layout
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DaisywheelState()
        )

    val charIndexSelected: SharedFlow<Int> = _charSelected

    val characterSelected: Flow<Char> = charIndexSelected.map { char ->
        state.value.petals
            // could be a modifier layout that doesn't have all petals filled
            .getOrNull(state.value.selectedPetalIndex)
            ?.characters
            ?.getOrNull(char)
    }
        .filterNotNull()

    /**
     * Selects a petal by its index.
     * @param index The index of the petal to select or -1 for none.
     */
    fun selectPetal(index: Int) = _petalSelected.tryEmit(index)

    /**
     * Selects a character by its index.
     * @param dir The index of the character to select.
     */
    fun selectChar(dir: Int) = _charSelected.tryEmit(dir)

    /**
     * Enables or disables a modifier
     * @param modifier The modifier to change
     * @param enable Should the modifier be enabled or disabled
     */
    fun enableModifier(modifier: DaisywheelModifier, enable: Boolean = true) = _modifierSelected.tryEmit(Pair(modifier, enable))

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DaisywheelViewModel()
            }
        }
    }
}