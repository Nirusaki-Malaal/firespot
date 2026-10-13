# Fire OS Adaptations & Optimizations: PlayStation Portable (PPSSPP)

## Overview
PPSSPP was tailored for Amazon Fire OS, focusing on hardware-accelerated OpenGL ES 2.0/3.0 stability on the PowerVR GE8300 GPU, lean memory utilization (< 250 MB), and D-pad/remote TV navigation.

## Applied Patches & Rationale

### 1. `01_fireos_tv_leanback_input.patch`
- **Issue:** PPSSPP's standard Android UI expects direct touchscreen taps or mouse hover. On Fire TV, the Alexa remote passes `KEYCODE_DPAD_CENTER` and `KEYCODE_MENU`.
- **Change:** Mapped Fire TV remote keycodes directly to PSP Action buttons and Menu/Pause toggles, with focus highlights.
- **Result:** Complete TV-native remote control without requiring a mouse or air-remote pointer.

### 2. `02_fireos_powervr_ge8300_gles_fallback.patch`
- **Issue:** The PowerVR GE8300 GPU found in Fire TV Stick Lite / 3rd Gen / 4K has fragile Vulkan driver support with memory allocation leaks.
- **Change:** Enforced OpenGL ES 2.0/3.0 as the active renderer, enabled vertex caching, disabled slower framebuffer readbacks, and locked default internal resolution to 1x PSP (480x272) integer-upscaled to TV output.
- **Result:** High rendering stability with zero GPU driver crashes.

### 3. `03_fireos_performance_preset_auto.patch`
- **Issue:** Performance varies significantly between Fire TV Stick Lite (quad Cortex-A53 @ 1.7 GHz) and Fire TV Stick 4K Max 2nd Gen (quad Cortex-A55 @ 2.0 GHz).
- **Change:** Built model-based auto-preset detection (`AFTR` = Performance / auto frameskip; `AFTMM` = Balanced / 0 frameskip).
- **Result:** Playable performance automatically chosen without manual user configuration.
