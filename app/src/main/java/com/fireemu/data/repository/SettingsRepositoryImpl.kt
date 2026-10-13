package com.fireemu.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.view.KeyEvent
import com.fireemu.data.local.datastore.SettingsStore
import com.fireemu.domain.models.AppSettings
import com.fireemu.domain.models.KeyBinding
import com.fireemu.domain.models.RetroButton
import com.fireemu.domain.models.VideoScaling
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val context: Context,
    private val settingsStore: SettingsStore
) : SettingsRepository {

    private val keyPrefs: SharedPreferences =
        context.getSharedPreferences("fireemu_key_bindings", Context.MODE_PRIVATE)

    override fun getSettings(): Flow<AppSettings> = settingsStore.settingsFlow

    override suspend fun updateSettings(settings: AppSettings) {
        settingsStore.updateSettings(settings)
    }

    override suspend fun addRomDirectory(path: String) {
        settingsStore.addRomDirectory(path)
    }

    override suspend fun removeRomDirectory(path: String) {
        settingsStore.removeRomDirectory(path)
    }

    override suspend fun setVideoScaling(scaling: VideoScaling) {
        settingsStore.setVideoScaling(scaling)
    }

    override suspend fun setScanlines(enabled: Boolean) {
        settingsStore.setScanlines(enabled)
    }

    override suspend fun setFpsVisible(enabled: Boolean) {
        settingsStore.setFpsVisible(enabled)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        settingsStore.setOnboardingCompleted(completed)
    }

    override suspend fun getKeyBindings(): Map<RetroButton, Int> {
        val bindings = mutableMapOf<RetroButton, Int>()
        for (btn in RetroButton.values()) {
            val defaultKey = getDefaultKeyCode(btn)
            val code = keyPrefs.getInt("btn_${btn.id}", defaultKey)
            bindings[btn] = code
        }
        return bindings
    }

    override suspend fun saveKeyBinding(button: RetroButton, keyCode: Int) {
        keyPrefs.edit().putInt("btn_${button.id}", keyCode).apply()
    }

    override suspend fun resetDefaultKeyBindings() {
        keyPrefs.edit().clear().apply()
    }

    private fun getDefaultKeyCode(button: RetroButton): Int {
        return when (button) {
            RetroButton.UP -> KeyEvent.KEYCODE_DPAD_UP
            RetroButton.DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
            RetroButton.LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
            RetroButton.RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
            RetroButton.A -> KeyEvent.KEYCODE_BUTTON_A
            RetroButton.B -> KeyEvent.KEYCODE_BUTTON_B
            RetroButton.X -> KeyEvent.KEYCODE_BUTTON_X
            RetroButton.Y -> KeyEvent.KEYCODE_BUTTON_Y
            RetroButton.SELECT -> KeyEvent.KEYCODE_BUTTON_SELECT
            RetroButton.START -> KeyEvent.KEYCODE_BUTTON_START
            RetroButton.L -> KeyEvent.KEYCODE_BUTTON_L1
            RetroButton.R -> KeyEvent.KEYCODE_BUTTON_R1
            RetroButton.L2 -> KeyEvent.KEYCODE_BUTTON_L2
            RetroButton.R2 -> KeyEvent.KEYCODE_BUTTON_R2
            RetroButton.L3 -> KeyEvent.KEYCODE_BUTTON_THUMBL
            RetroButton.R3 -> KeyEvent.KEYCODE_BUTTON_THUMBR
        }
    }
}
