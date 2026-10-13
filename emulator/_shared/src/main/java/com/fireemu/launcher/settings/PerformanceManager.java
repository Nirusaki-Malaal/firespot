package com.fireemu.launcher.settings;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

public class PerformanceManager {
    public static final int PRESET_PERFORMANCE = 0;
    public static final int PRESET_BALANCED    = 1;
    public static final int PRESET_QUALITY     = 2;

    public static class DeviceCapability {
        public final String model;
        public final boolean is32BitOnly;
        public final long totalRamMb;
        public final int defaultPreset;
        public final boolean ps2Supported;

        public DeviceCapability(String model, boolean is32BitOnly, long totalRamMb, int defaultPreset, boolean ps2Supported) {
            this.model = model;
            this.is32BitOnly = is32BitOnly;
            this.totalRamMb = totalRamMb;
            this.defaultPreset = defaultPreset;
            this.ps2Supported = ps2Supported;
        }
    }

    private final Context context;
    private final SharedPreferences prefs;

    public PerformanceManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("fireemu_performance", Context.MODE_PRIVATE);
    }

    public DeviceCapability getDeviceCapability() {
        String model = Build.MODEL;
        boolean is32Bit = true;
        for (String abi : Build.SUPPORTED_ABIS) {
            if (abi.contains("arm64") || abi.contains("x86_64")) {
                // Check if userland itself is 64-bit or 32-bit zygote
                if (System.getProperty("os.arch", "").contains("64")) {
                    is32Bit = false;
                }
            }
        }

        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        if (actManager != null) {
            actManager.getMemoryInfo(memInfo);
        }
        long ramMb = memInfo.totalMem / (1024 * 1024);

        int preset = PRESET_BALANCED;
        // Fire TV Stick Lite / 3rd Gen (AFTR, AFTSSS) -> Auto Performance Preset
        if (model.toUpperCase().contains("AFTR") || model.toUpperCase().contains("AFTSSS") || ramMb <= 1024) {
            preset = PRESET_PERFORMANCE;
        } else if (model.toUpperCase().contains("AFTKA") || model.toUpperCase().contains("AFTE")) {
            preset = PRESET_QUALITY;
        }

        // PS2 is strictly unsupported on Fire TV Sticks due to 32-bit userland and <2GB RAM
        boolean ps2Viable = (!is32Bit && ramMb >= 4096);

        return new DeviceCapability(model, is32Bit, ramMb, preset, ps2Viable);
    }

    public int getPresetForConsole(String console) {
        DeviceCapability cap = getDeviceCapability();
        return prefs.getInt("preset_" + console, cap.defaultPreset);
    }

    public void setPresetForConsole(String console, int preset) {
        prefs.edit().putInt("preset_" + console, preset).apply();
    }

    public int getFrameskip(String console) {
        int preset = getPresetForConsole(console);
        if (console.equals("psp") && preset == PRESET_PERFORMANCE) {
            return 1; // Auto frameskip 1 for PSP on low-end Stick models
        }
        return 0; // 60 FPS full speed default
    }

    public int getAudioBufferSizeSamples() {
        // 48000 Hz / 60 Hz = 800 samples per video frame
        // 2x buffer = 1600 samples (~33 ms latency) for jitter immunity
        return 1600;
    }
}
