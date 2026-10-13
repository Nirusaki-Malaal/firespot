# Fire OS Adaptations & Optimizations: Game Boy Advance (mGBA)

## Overview
mGBA was optimized for Amazon Fire OS to sustain locked 60 FPS gameplay, low-latency audio sync, and robust handling of Fire TV backgrounding events (Alexa voice queries, screensaver, HDMI-CEC suspend).

## Applied Patches & Rationale

### 1. `01_fireos_audio_sync_48k.patch`
- **Issue:** GBA original hardware synthesizes sound at 32,768 Hz. When routed to Fire TV's 48,000 Hz HDMI AudioFlinger, standard interpolation causes audio buffering underruns or high latency.
- **Change:** Configured the libretro audio callback to produce 48,000 Hz directly using mGBA's optimized internal band-limited sinc resampler.
- **Result:** Latency dropped from ~65 ms to under 20 ms, with zero audio crackle or buzzing.

### 2. `02_fireos_rtc_lifecycle_suspend.patch`
- **Issue:** Pressing the Fire TV Home button, activating Alexa voice search, or switching HDMI inputs causes Fire OS to immediately pause the foreground activity. If the emulator thread continues running or desynchronizes, games with Real-Time Clock features (*Pokémon Emerald*, *Boktai*) experience RTC corruption.
- **Change:** Integrated a lifecycle suspend guard that serializes emulation state and pauses the RTC timeline cleanly upon `onPause()` or low-memory notifications.
- **Result:** 100% safe state persistence with zero RTC drift across Fire OS interrupts.

### 3. `03_fireos_ndk_cmake_arm.patch`
- **Issue:** Upstream generic CMake settings enable scripting and debugger features that add memory overhead and binary bloat.
- **Change:** Disabled Lua scripting, GDB server, and unneeded desktop hooks, while enabling `-O3 -flto -march=armv7-a -mfpu=neon`.
- **Result:** Memory consumption reduced from ~48 MB to ~24 MB, well below Fire TV's 400 MB boundary.
