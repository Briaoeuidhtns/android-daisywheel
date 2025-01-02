package io.github.briaoeuidhtns.daisywheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DaisywheelState(
    val selectedPetalIndex: Int = 0,
    val selectedCharacterIndex: Int = -1,
    val currentText: String = "",
    val shift: Boolean = false
)

class DaisywheelViewModel : ViewModel() {
    private val _state = MutableStateFlow(DaisywheelState())
    val state: StateFlow<DaisywheelState> = _state.asStateFlow()

    fun onPetalSelected(index: Int) {
        _state.value = _state.value.copy(
            selectedPetalIndex = index,
            selectedCharacterIndex = -1
        )
    }

    fun onCharacterSelected(index: Int) {
        _state.value = _state.value.copy(selectedCharacterIndex = index)
    }

    fun onCharacterConfirmed(char: Char) {
        _state.value = _state.value.copy(
            currentText = _state.value.currentText + char,
            selectedCharacterIndex = -1
        )
    }

    fun onBackspace() {
        _state.value = _state.value.copy(
            currentText = _state.value.currentText.dropLast(1)
        )
    }

    fun onToggleShift() {
        _state.value = _state.value.copy(
            shift = !_state.value.shift
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DaisywheelViewModel() as T
            }
        }
    }
}