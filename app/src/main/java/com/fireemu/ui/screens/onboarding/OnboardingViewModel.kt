package com.fireemu.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireemu.data.repository.SettingsRepository
import com.fireemu.domain.usecases.ScanRomsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val selectedFolder: String = "/sdcard/ROMs",
    val isScanning: Boolean = false,
    val scanCount: Int = 0,
    val isDone: Boolean = false
)

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
    private val scanRomsUseCase: ScanRomsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun selectInternalFolder() {
        _uiState.value = _uiState.value.copy(selectedFolder = "/sdcard/ROMs")
    }

    fun selectUsbFolder(usbPath: String) {
        _uiState.value = _uiState.value.copy(selectedFolder = usbPath)
    }

    fun startInitialScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            settingsRepository.addRomDirectory(_uiState.value.selectedFolder)
            try {
                val found = scanRomsUseCase()
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    scanCount = found.size,
                    isDone = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    isDone = true
                )
            }
        }
    }

    fun completeOnboarding(onCompleted: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
            onCompleted()
        }
    }
}
