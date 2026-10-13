package com.fireemu.nativebridge

import android.view.Surface

object LibretroBridge {
    init {
        try {
            System.loadLibrary("retro_bridge")
        } catch (e: UnsatisfiedLinkError) {
            // Handled when dynamically located or in tests
        }
    }

    fun interface AudioCallback {
        fun onAudioSamples(samples: ShortArray, numSamples: Int)
    }

    private var audioCallback: AudioCallback? = null

    fun setAudioCallback(callback: AudioCallback?) {
        this.audioCallback = callback
    }

    @JvmStatic
    fun dispatchAudio(samples: ShortArray, numSamples: Int) {
        audioCallback?.onAudioSamples(samples, numSamples)
    }

    external fun nativeInitCore(coreLibraryPath: String): Boolean
    external fun nativeSetSurface(surface: Surface, targetWidth: Int, targetHeight: Int): Boolean
    external fun nativeLoadGame(romPath: String, biosPath: String): Boolean
    external fun nativeRunFrame(inputBitmask: Int)
    external fun nativeSaveState(stateFilePath: String): Boolean
    external fun nativeLoadState(stateFilePath: String): Boolean
    external fun nativeReset()
    external fun nativeDestroy()
}
