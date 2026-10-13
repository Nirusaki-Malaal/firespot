# NES Emulation Core for Fire OS (FCEUmm)

## Overview
Optimized FCEUmm core specifically adapted for Amazon Fire TV Stick Lite, Stick (3rd Gen), Stick 4K, Stick 4K Max, and Fire TV Cube.

## Hardware & OS Requirements
- **OS:** Fire OS 7 (Android 9 / API 28) or Fire OS 8 (Android 11 / API 30). minSdk 25.
- **Architectures:** `armeabi-v7a` (32-bit userland, mandatory) and `arm64-v8a` (optional).
- **Supported File Types:** `.nes`, `.fds` (Famicom Disk System with `disksys.rom` in BIOS folder).

## Directory Structure
- `upstream/`: Cloned FCEUmm repository pinned at commit `7a542dab1e87679921962a9f056186eca425c0c2`.
- `patches/`: Modular Fire OS patches.
- `build/`: NDK build definitions and shell scripts.
- `UPSTREAM.md`: Upstream repository and architectural rationale.
- `FIRE_OS_CHANGES.md`: Detailed changelog of Fire OS optimizations.
- `LICENSE-NOTICE.md`: GNU GPL-2.0 legal and attribution notice.
- `benchmark.md`: Real-world FPS, CPU, and audio stability measurements per Fire TV model.

## How to Build
Run the build script:
```bash
cd agent/emulator/nes/build
./build.sh
```
Or build using standard Android NDK:
```bash
ndk-build -C build
```

## How to Launch
ROMs are placed in `/sdcard/ROMs/nes/` or loaded from external USB OTG storage. The core is invoked directly via the Fire TV Leanback Launcher (`_shared`).
