package com.fireemu.launcher.jni;

import android.view.Surface;

public class LibretroBridge {
    static {
        try {
            System.loadLibrary("retro_bridge");
        } catch (UnsatisfiedLinkError e) {
            // Handled gracefully if loaded dynamically
        }
    }

    public interface AudioCallback {
        void onAudioSamples(short[] samples, int numSamples);
    }

    private static AudioCallback audioCallback;

    public static void setAudioCallback(AudioCallback cb) {
        audioCallback = cb;
    }

    // Called from Native C layer
    public static void dispatchAudio(short[] samples, int numSamples) {
        if (audioCallback != null) {
            audioCallback.onAudioSamples(samples, numSamples);
        }
    }

    public static native boolean nativeInitCore(String coreLibraryPath);
    public static native boolean nativeSetSurface(Surface surface, int targetWidth, int targetHeight);
    public static native boolean nativeLoadGame(String romPath, String biosPath);
    public static native void nativeRunFrame(int inputBitmask);
    public static native boolean nativeSaveState(String stateFilePath);
    public static native boolean nativeLoadState(String stateFilePath);
    public static native void nativeReset();
    public static native void nativeDestroy();
}
