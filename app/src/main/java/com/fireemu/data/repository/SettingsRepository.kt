package com.fireemu.data.repository

import com.fireemu.domain.models.AppSettings
import com.fireemu.domain.models.KeyBinding
import com.fireemu.domain.models.RetroButton
import com.fireemu.domain.models.VideoScaling
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateSettings(settings: AppSettings)
    suspend fun addRomDirectory(path: String)
    suspend fun removeRomDirectory(path: String)
    suspend fun setVideoScaling(scaling: VideoScaling)
    suspend fun setScanlines(enabled: Boolean)
    suspend fun setFpsVisible(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun getKeyBindings(): Map<RetroButton, Int>
    suspend fun saveKeyBinding(button: RetroButton, keyCode: Int)
    suspend fun resetDefaultKeyBindings()
}
