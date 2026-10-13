package com.fireemu.domain.models

enum class VideoScaling(val id: Int, val label: String) {
    ASPECT_RATIO(0, "4:3 / Core Aspect"),
    INTEGER_SCALE(1, "Pixel Perfect (Integer)"),
    STRETCH_FULLSCREEN(2, "Stretch to 16:9")
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
    val isOnboardingCompleted: Boolean = false
)
