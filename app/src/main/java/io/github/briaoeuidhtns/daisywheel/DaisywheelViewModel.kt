package io.github.briaoeuidhtns.daisywheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import java.util.EnumSet

data class DaisyPetal(
    val characters: List<Char>,
    val isSelected: Boolean = false,
)

data class DaisywheelState(
    val selectedPetalIndex: Int = -1,
    val petals: List<DaisyPetal> = defaultLayout,
)

var defaultLayout =
    listOf(
        DaisyPetal(listOf('a', 'b', 'c', 'd')), // Top
        DaisyPetal(listOf('e', 'f', 'g', 'h')), // Top-right
        DaisyPetal(listOf('i', 'j', 'k', 'l')), // Right
        DaisyPetal(listOf('m', 'n', 'o', 'p')), // Bottom-right
        DaisyPetal(listOf('q', 'r', 's', 't')), // Bottom
        DaisyPetal(listOf('u', 'v', 'w', 'x')), // Bottom-left
        DaisyPetal(listOf('y', 'z', ',', '.')), // Left
        DaisyPetal(listOf('\'', '"', '-', '@')), // Top-left
    )

var uppercaseLayout =
    listOf(
        DaisyPetal(listOf('A', 'B', 'C', 'D')), // Top
        DaisyPetal(listOf('E', 'F', 'G', 'H')), // Top-right
        DaisyPetal(listOf('I', 'J', 'K', 'L')), // Right
        DaisyPetal(listOf('M', 'N', 'O', 'P')), // Bottom-right
        DaisyPetal(listOf('Q', 'R', 'S', 'T')), // Bottom
        DaisyPetal(listOf('U', 'V', 'W', 'X')), // Bottom-left
        DaisyPetal(listOf('Y', 'Z', '!', '?')), // Left
        DaisyPetal(listOf('_', ':', ';', '/')), // Top-left
    )

var numberLayout =
    listOf(
        DaisyPetal(listOf('1', '2', '3', '+')), // Top
        DaisyPetal(listOf('4', '5', '6', '-')), // Top-right
        DaisyPetal(listOf('7', '8', '9', '*')), // Right
        DaisyPetal(listOf('0', '.', '=', '/')), // Bottom-right
        DaisyPetal(listOf('(', ')', '[', ']')), // Bottom
        DaisyPetal(listOf('<', '>', '{', '}')), // Bottom-left
        DaisyPetal(listOf('\\', '|', ',', '.')), // Left
        DaisyPetal(listOf('#', '$', '%', '^')), // Top-left
    )

var symbolLayout =
    listOf(
        DaisyPetal(listOf('!', '@', '#', '$')), // Top
        DaisyPetal(listOf('%', '^', '&', '*')), // Top-right
        DaisyPetal(listOf('~', '`', '±', '§')), // Right
        DaisyPetal(listOf('©', '®', '™', '°')), // Bottom-right
        DaisyPetal(listOf('£', '€', '¥', '¢')), // Bottom
        DaisyPetal(listOf('¿', '¡', '¶', '†')), // Bottom-left
        DaisyPetal(listOf('‹', '›', '«', '»')), // Left
        DaisyPetal(listOf('•', '…', '¤', '∞')), // Top-left
    )

enum class DaisywheelModifier {
    SHIFT,
    ALT,
}

class DaisywheelViewModel : ViewModel() {
    private val _petalSelected = MutableSharedFlow<Int>(replay = 1)
    private val _charSelected = MutableSharedFlow<Int>(replay = 1)
    private val _modifierSelected = MutableSharedFlow<Pair<DaisywheelModifier, Boolean>>(replay = 1)
    private val _backspaceRequested = MutableSharedFlow<Unit>(replay = 1)

    val modifiers = _modifierSelected
        .scan(EnumSet.noneOf(DaisywheelModifier::class.java) as Set<DaisywheelModifier>) { s, (modifier, enabled) ->
            if (enabled) s + modifier else s - (modifier)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EnumSet.noneOf(DaisywheelModifier::class.java)
        )

    val state: StateFlow<DaisywheelState> = _petalSelected
        .onStart { emit(-1) }  // Emit initial selection
        .combine(modifiers.map { activeModifiers ->
            when {
                activeModifiers.containsAll(setOf(DaisywheelModifier.ALT, DaisywheelModifier.SHIFT)) -> symbolLayout
                activeModifiers.contains(DaisywheelModifier.ALT) -> numberLayout
                activeModifiers.contains(DaisywheelModifier.SHIFT) -> uppercaseLayout
                else -> defaultLayout
            }
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

    val characterSelected: SharedFlow<Char> = charIndexSelected.map { char ->
        state.value.petals
            // could be a modifier layout that doesn't have all petals filled
            .getOrNull(state.value.selectedPetalIndex)
            ?.characters
            ?.getOrNull(char)
    }
        .filterNotNull()
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
        )

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
    fun enableModifier(modifier: DaisywheelModifier, enable: Boolean = true) =
        _modifierSelected.tryEmit(Pair(modifier, enable))

    val backspaceRequested: SharedFlow<Unit> = _backspaceRequested
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
        )

    /**
     * Requests a backspace operation
     */
    fun requestBackspace() = _backspaceRequested.tryEmit(Unit)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DaisywheelViewModel()
            }
        }
    }
}