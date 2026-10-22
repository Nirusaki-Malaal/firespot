package com.fireemu.nativebridge

import android.view.Surface

object LibretroBridge {
    init { System.loadLibrary("retro_bridge") }

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

    @Synchronized external fun nativeInitCore(coreLibraryPath: String): Boolean
    @Synchronized external fun nativeSetSurface(surface: Surface, targetWidth: Int, targetHeight: Int): Boolean
    @Synchronized external fun nativeLoadGame(romPath: String, biosPath: String): Boolean
    @Synchronized external fun nativeRunFrame(inputBitmask: Int)
    @Synchronized external fun nativeSaveState(stateFilePath: String): Boolean
    @Synchronized external fun nativeLoadState(stateFilePath: String): Boolean
    @Synchronized external fun nativeGetTiming(): DoubleArray?
    @Synchronized external fun nativeSetScaling(mode: Int)
    @Synchronized external fun nativeBatteryRam(path: String, saving: Boolean): Boolean
    @Synchronized external fun nativeReset()
    @Synchronized external fun nativeDestroy()
}
