package com.fireemu.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fireemu.domain.models.AppSettings
import com.fireemu.domain.models.VideoScaling
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fireemu_settings")

class SettingsStore(private val context: Context) {
    private val KEY_ROM_DIRS = stringPreferencesKey("rom_directories")
    private val KEY_BIOS_DIR = stringPreferencesKey("bios_directory")
    private val KEY_SCALING = intPreferencesKey("video_scaling")
    private val KEY_BILINEAR = booleanPreferencesKey("bilinear_filter")
    private val KEY_SCANLINES = booleanPreferencesKey("scanlines_shader")
    private val KEY_SHOW_FPS = booleanPreferencesKey("show_fps")
    private val KEY_AUDIO_BUF = intPreferencesKey("audio_buffer_size")
    private val KEY_FAST_FORWARD = floatPreferencesKey("fast_forward_speed")
    private val KEY_AUTO_SAVE = booleanPreferencesKey("auto_save_exit")
    private val KEY_ONBOARDING = booleanPreferencesKey("onboarding_completed")

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val dirsStr = prefs[KEY_ROM_DIRS] ?: "/sdcard/ROMs"
        val dirs = dirsStr.split(";").filter { it.isNotBlank() }
        val scalingId = prefs[KEY_SCALING] ?: VideoScaling.ASPECT_RATIO.id
        val scaling = VideoScaling.values().firstOrNull { it.id == scalingId } ?: VideoScaling.ASPECT_RATIO

        AppSettings(
            romDirectories = if (dirs.isEmpty()) listOf("/sdcard/ROMs") else dirs,
            biosDirectory = prefs[KEY_BIOS_DIR] ?: "/sdcard/ROMs/bios",
            videoScaling = scaling,
            bilinearFilter = prefs[KEY_BILINEAR] ?: true,
            scanlinesShader = prefs[KEY_SCANLINES] ?: false,
            showFps = prefs[KEY_SHOW_FPS] ?: false,
            audioBufferSize = prefs[KEY_AUDIO_BUF] ?: 1600,
            fastForwardSpeed = prefs[KEY_FAST_FORWARD] ?: 2.0f,
            autoSaveOnExit = prefs[KEY_AUTO_SAVE] ?: true,
            isOnboardingCompleted = prefs[KEY_ONBOARDING] ?: false
        )
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ROM_DIRS] = settings.romDirectories.joinToString(";")
            prefs[KEY_BIOS_DIR] = settings.biosDirectory
            prefs[KEY_SCALING] = settings.videoScaling.id
            prefs[KEY_BILINEAR] = settings.bilinearFilter
            prefs[KEY_SCANLINES] = settings.scanlinesShader
            prefs[KEY_SHOW_FPS] = settings.showFps
            prefs[KEY_AUDIO_BUF] = settings.audioBufferSize
            prefs[KEY_FAST_FORWARD] = settings.fastForwardSpeed
            prefs[KEY_AUTO_SAVE] = settings.autoSaveOnExit
            prefs[KEY_ONBOARDING] = settings.isOnboardingCompleted
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING] = completed
        }
    }

    suspend fun setVideoScaling(scaling: VideoScaling) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SCALING] = scaling.id
        }
    }

    suspend fun setScanlines(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SCANLINES] = enabled
        }
    }

    suspend fun setFpsVisible(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_FPS] = enabled
        }
    }

    suspend fun addRomDirectory(path: String) {
        context.dataStore.edit { prefs ->
            val existing = (prefs[KEY_ROM_DIRS] ?: "/sdcard/ROMs").split(";").filter { it.isNotBlank() }.toMutableList()
            if (!existing.contains(path)) {
                existing.add(path)
                prefs[KEY_ROM_DIRS] = existing.joinToString(";")
            }
        }
    }

    suspend fun removeRomDirectory(path: String) {
        context.dataStore.edit { prefs ->
            val existing = (prefs[KEY_ROM_DIRS] ?: "/sdcard/ROMs").split(";").filter { it.isNotBlank() }.toMutableList()
            existing.remove(path)
            prefs[KEY_ROM_DIRS] = existing.joinToString(";")
        }
    }
}
