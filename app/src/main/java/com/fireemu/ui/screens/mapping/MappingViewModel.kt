package com.fireemu.ui.screens.mapping

import android.view.KeyEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireemu.data.repository.SettingsRepository
import com.fireemu.domain.models.RetroButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MappingUiState(
    val bindings: Map<RetroButton, Int> = emptyMap(),
    val listeningForButton: RetroButton? = null,
    val statusMessage: String = "Select a button with D-Pad, then press Select to rebind."
)

class MappingViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MappingUiState())
    val uiState: StateFlow<MappingUiState> = _uiState.asStateFlow()

    init {
        loadBindings()
    }

    fun loadBindings() {
        viewModelScope.launch {
            val map = settingsRepository.getKeyBindings()
            _uiState.value = _uiState.value.copy(bindings = map)
        }
    }

    fun startListeningFor(button: RetroButton) {
        _uiState.value = _uiState.value.copy(
            listeningForButton = button,
            statusMessage = "Press any key or button on your remote/gamepad for ${button.label}..."
        )
    }

    fun cancelListening() {
        _uiState.value = _uiState.value.copy(
            listeningForButton = null,
            statusMessage = "Mapping cancelled."
        )
    }

    fun bindKey(keyCode: Int) {
        val targetButton = _uiState.value.listeningForButton ?: return
        if(keyCode in listOf(KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_HOME, KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_F1)) {
            _uiState.value = _uiState.value.copy(statusMessage="Menu, Back, Home, Escape and F1 are reserved. Choose another button.")
            return
        }
        viewModelScope.launch {
            settingsRepository.saveKeyBinding(targetButton, keyCode)
            val updated = _uiState.value.bindings.toMutableMap()
            updated[targetButton] = keyCode
            _uiState.value = _uiState.value.copy(
                bindings = updated,
                listeningForButton = null,
                statusMessage = "Bound ${targetButton.label} to key code $keyCode (${KeyEvent.keyCodeToString(keyCode)})."
            )
        }
    }

    fun resetDefaults() {
        viewModelScope.launch {
            settingsRepository.resetDefaultKeyBindings()
            loadBindings()
            _uiState.value = _uiState.value.copy(
                statusMessage = "Reset all controller mappings to default."
            )
        }
    }
}
