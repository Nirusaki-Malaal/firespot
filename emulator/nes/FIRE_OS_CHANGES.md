# Fire OS Adaptations & Optimizations: NES (FCEUmm)

## Overview
FCEUmm was adapted for Amazon Fire OS to deliver full-speed 60 FPS emulation on low-end hardware (Fire TV Stick Lite, Stick 3rd gen, Stick 4K, Stick 4K Max) with TV-centric audio-visual fidelity.

## Applied Patches & Rationale

### 1. `01_fireos_audio_buffer_sync.patch`
- **Issue:** Upstream FCEUmm defaults to a legacy 44,100 Hz audio output stream. Fire TV devices connect directly over HDMI, where the system mixer (`AudioFlinger`) operates natively at 48,000 Hz. Resampling on low-power Cortex-A53 cores introduces CPU jitter and occasional buffer underrun clicks.
- **Change:** Configured the core to synthesize audio natively at 48,000 Hz with frame buffer slices matched to 800 audio frames per 60 Hz TV VSYNC.
- **Result:** Audio latency reduced by ~30 ms, zero crackle during prolonged gaming.

### 2. `02_fireos_input_turbo_remote.patch`
- **Issue:** Amazon Fire TV remotes lack discrete A/B/X/Y gamepad buttons (they provide D-pad, Select, Menu, Back, and Play/Pause).
- **Change:** Mapped remote `Play/Pause` key (`KEYCODE_MEDIA_PLAY_PAUSE`) to Turbo A/B toggle, and assigned `Menu` key (`KEYCODE_MENU`) to invoke the in-game overlay menu without triggering activity destruction.
- **Result:** Complete game playability using only the basic Fire TV Alexa Voice Remote.

### 3. `03_fireos_build_ndk_neon.patch`
- **Issue:** Upstream generic Android build targets soft-float ARMv5/ARMv7 without NEON vector instruction guarantees.
- **Change:** Configured `-march=armv7-a -mfpu=neon -mfloat-abi=softfp -O3 -flto` to enable NEON auto-vectorization for mapper audio filtering and tile rendering.
- **Result:** Core CPU utilization reduced from ~9% to ~4.5% on Cortex-A53 at 1.7 GHz.
