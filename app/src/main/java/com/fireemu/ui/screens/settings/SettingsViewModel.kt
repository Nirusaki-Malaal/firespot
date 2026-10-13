package com.fireemu.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireemu.domain.models.AppSettings
import com.fireemu.domain.models.VideoScaling
import com.fireemu.domain.usecases.ScanRomsUseCase
import com.fireemu.domain.usecases.SettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsUseCase: SettingsUseCase,
    private val scanRomsUseCase: ScanRomsUseCase
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsUseCase.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanMessage = MutableStateFlow<String?>(null)
    val scanMessage: StateFlow<String?> = _scanMessage.asStateFlow()

    fun triggerScan() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanMessage.value = "Scanning storage directories..."
            try {
                val scanned = scanRomsUseCase()
                _scanMessage.value = "Scan complete! Found ${scanned.size} games."
            } catch (e: Exception) {
                _scanMessage.value = "Scan failed: ${e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun cycleScaling() {
        viewModelScope.launch {
            val current = settings.value.videoScaling
            val next = when (current) {
                VideoScaling.ASPECT_RATIO -> VideoScaling.INTEGER_SCALE
                VideoScaling.INTEGER_SCALE -> VideoScaling.STRETCH_FULLSCREEN
                VideoScaling.STRETCH_FULLSCREEN -> VideoScaling.ASPECT_RATIO
            }
            settingsUseCase.setVideoScaling(next)
        }
    }

    fun toggleScanlines() {
        viewModelScope.launch {
            val current = settings.value.scanlinesShader
            settingsUseCase.setScanlines(!current)
        }
    }

    fun toggleFps() {
        viewModelScope.launch {
            val current = settings.value.showFps
            settingsUseCase.setFpsVisible(!current)
        }
    }
}
