# Game Boy & Game Boy Color Core for Fire OS (Gambatte)

## Overview
High-performance, cycle-accurate Gambatte core tuned for Amazon Fire TV Sticks and Cubes.

## Supported File Extensions
- Game Boy: `.gb`
- Game Boy Color: `.gbc`
- Super Game Boy enhancements: supported

## Hardware Requirements
- **OS:** Fire OS 7 (API 28) / Fire OS 8 (API 30). minSdk 25.
- **Architectures:** `armeabi-v7a` (Mandatory), `arm64-v8a` (Optional).
- **RAM Footprint:** ~18 MB resident set size.

## Directory Structure
- `upstream/`: Cloned upstream Gambatte repository pinned at commit `d9d6cd06382d1ced30de34d56d3609452323dab1`.
- `patches/`: Fire OS television color correction and 48 kHz audio patches.
- `build/`: NDK build recipes and compilation scripts.
- `UPSTREAM.md`: Core selection rationale and technical comparison.
- `FIRE_OS_CHANGES.md`: Detailed engineering changes.
- `LICENSE-NOTICE.md`: GNU GPL-2.0 license notice.
- `benchmark.md`: FPS and latency results on Fire TV hardware.

## Building the Core
Run the compilation script:
```bash
cd agent/emulator/gameboy/build
./build.sh
```

## Running ROMs
Place ROM files in `/sdcard/ROMs/gameboy/` or select a USB storage drive via the Fire TV Leanback Launcher.
