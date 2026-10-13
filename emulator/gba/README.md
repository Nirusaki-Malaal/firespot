# Game Boy Advance Core for Fire OS (mGBA)

## Overview
Optimized mGBA core for Amazon Fire TV Sticks and Cubes.

## Supported Extensions
- `.gba`
- Support for RTC (Real-Time Clock), solar sensors, and save states.

## System Requirements
- **Target OS:** Fire OS 7 / Fire OS 8 (Android API 28–30), minSdk 25.
- **Architectures:** `armeabi-v7a` (Mandatory), `arm64-v8a` (Optional).
- **RAM Footprint:** ~24 MB.

## Directory Layout
- `upstream/`: Cloned mGBA repository pinned at commit `7a12d6d4b9acb14c0ae62c9166b6a2f3d08007f6`.
- `patches/`: Fire OS 48 kHz HDMI audio, RTC lifecycle guard, and NEON optimization patches.
- `build/`: Automated build scripts and NDK configs.
- `UPSTREAM.md`: Engineering evaluation and rationale.
- `FIRE_OS_CHANGES.md`: Detailed changelog.
- `LICENSE-NOTICE.md`: MPL-2.0 legal notice.
- `benchmark.md`: Measured performance metrics.

## Compilation
```bash
cd agent/emulator/gba/build
./build.sh
```

## Game Storage
Place GBA ROMs in `/sdcard/ROMs/gba/`. Official GBA BIOS (`gba_bios.bin`) is optional and can be placed in `/sdcard/ROMs/bios/` for authentic boot animation.
