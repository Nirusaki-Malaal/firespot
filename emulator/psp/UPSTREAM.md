# Upstream Specification: Sony PlayStation Portable (PSP)

## Selected Emulator Core
- **Core Name:** PPSSPP (`ppsspp`)
- **Upstream Repository:** https://github.com/hrydgard/ppsspp
- **Pinned Tag / Commit:** `v1.18.1` (`98a8583b1783af766423ec799e82d5829a7f496d`)
- **Upstream License:** GNU General Public License v2.0 or later (GPL-2.0-or-later)
- **Last Commit Date:** 2024-11-04
- **Upstream Maintainer:** Henrik Rydgård (hrydgard) and contributors

## Selection Rationale & Architectural Comparison
The PSP emulation landscape has one universally acknowledged open-source standard: **PPSSPP**.

| Criteria | PPSSPP (Selected) | Jpcsp |
| :--- | :--- | :--- |
| **Android Architecture** | Highly tuned C++ with ARMv7 & ARM64 JIT | Java-based (Archived / Non-functional on Android) |
| **Rendering Backends** | OpenGL ES 2.0/3.x, Vulkan | Desktop OpenGL |
| **CPU Utilization (Cortex-A53/A55)** | ~40–70% (Playable with Performance preset) | N/A (Unusable) |
| **RAM Footprint** | ~140–260 MB (Safely within 400 MB limit) | >800 MB (Immediate OOM) |
| **Performance Tuning for Fire TV** | 1x PSP native resolution, vertex cache, frameskip | None |
| **License** | GPL-2.0-or-later | GPL-2.0 |

### Why PPSSPP Was Selected
1. **Unrivaled Android JIT Maturity:** PPSSPP contains a battle-tested MIPS-to-ARM recompiler supporting both 32-bit `armeabi-v7a` and 64-bit `arm64-v8a`. It generates optimal ARM code sequences for Allegrex processor instructions.
2. **Granular GPU Pipeline Controls:** On Fire TV's PowerVR GE8300 GPU, Vulkan is unstable. PPSSPP's OpenGL ES 2.0/3.0 path offers critical controls: disable slow effects, hardware transform, vertex cache, and lazy texture caching.
3. **Audio-Video Synchronization:** PPSSPP provides an adaptive audio-sync subsystem that prevents crackling by micro-adjusting emulation speed to match the TV display's 60 Hz vertical blank interval.
