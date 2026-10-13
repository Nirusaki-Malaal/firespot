# PlayStation Portable Core for Fire OS (PPSSPP)

## Overview
High-performance PPSSPP core tailored for Amazon Fire TV Sticks and Cubes.

## Supported Formats
- Standard UMD ISO: `.iso`
- Compressed CSO: `.cso`
- Homebrew / Eboot: `.pbp`

## Hardware & OS Requirements
- **OS:** Fire OS 7 / Fire OS 8 (Android API 28–30), minSdk 25.
- **Architectures:** `armeabi-v7a` (32-bit ARM JIT, mandatory) and `arm64-v8a`.
- **Memory Footprint:** ~140–260 MB.

## Directory Structure
- `upstream/`: Cloned PPSSPP repository pinned at tag `v1.18.1` (`98a8583b1783af766423ec799e82d5829a7f496d`).
- `patches/`: Leanback input, PowerVR GLES fallback, and auto-preset patches.
- `build/`: NDK and CMake build configurations.
- `UPSTREAM.md`: Upstream architecture specification.
- `FIRE_OS_CHANGES.md`: Detailed changelog.
- `LICENSE-NOTICE.md`: GNU GPL-2.0+ notice.
- `benchmark.md`: FPS and gameplay stability benchmarks across Fire TV models.

## Building
```bash
cd agent/emulator/psp/build
./build.sh
```

## ROM Storage
Store PSP ISOs and CSOs in `/sdcard/ROMs/psp/` or on USB storage.
