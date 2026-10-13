package com.fireemu.domain.usecases

import com.fireemu.data.repository.SettingsRepository
import com.fireemu.domain.models.AppSettings
import com.fireemu.domain.models.RetroButton
import com.fireemu.domain.models.VideoScaling
import kotlinx.coroutines.flow.Flow

class SettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    val settingsFlow: Flow<AppSettings> = settingsRepository.getSettings()

    suspend fun updateSettings(settings: AppSettings) = settingsRepository.updateSettings(settings)
    suspend fun addRomDirectory(path: String) = settingsRepository.addRomDirectory(path)
    suspend fun removeRomDirectory(path: String) = settingsRepository.removeRomDirectory(path)
    suspend fun setVideoScaling(scaling: VideoScaling) = settingsRepository.setVideoScaling(scaling)
    suspend fun setScanlines(enabled: Boolean) = settingsRepository.setScanlines(enabled)
    suspend fun setFpsVisible(enabled: Boolean) = settingsRepository.setFpsVisible(enabled)
    suspend fun setOnboardingCompleted(completed: Boolean) = settingsRepository.setOnboardingCompleted(completed)
    suspend fun getKeyBindings(): Map<RetroButton, Int> = settingsRepository.getKeyBindings()
    suspend fun saveKeyBinding(button: RetroButton, keyCode: Int) = settingsRepository.saveKeyBinding(button, keyCode)
    suspend fun resetDefaultKeyBindings() = settingsRepository.resetDefaultKeyBindings()
}
