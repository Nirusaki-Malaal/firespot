package com.fireemu.domain.models

enum class VideoScaling(val id: Int, val label: String) {
    ASPECT_RATIO(0, "Original aspect"),
    INTEGER_SCALE(1, "Integer scale"),
    STRETCH_FULLSCREEN(2, "Fill screen")
}

data class AppSettings(
    val romDirectories: List<String> = listOf("/sdcard/ROMs"),
    val biosDirectory: String = "/sdcard/ROMs/bios",
    val videoScaling: VideoScaling = VideoScaling.ASPECT_RATIO,
    val bilinearFilter: Boolean = true,
    val scanlinesShader: Boolean = false,
    val showFps: Boolean = false,
    val audioBufferSize: Int = 1600,
    val fastForwardSpeed: Float = 2.0f,
    val autoSaveOnExit: Boolean = true,
    val isOnboardingCompleted: Boolean = false,
    val uiAmbientEnabled: Boolean = true,
    val uiSoundsEnabled: Boolean = true
)
