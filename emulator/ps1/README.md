# PlayStation 1 Core for Fire OS (PCSX ReARMed)

## Overview
ARMv7/NEON optimized PlayStation 1 core engineered for Amazon Fire TV Stick Lite, Stick (3rd Gen), Stick 4K, Stick 4K Max, and Fire TV Cube.

## Supported Formats
- Single/Multi-track: `.cue`, `.bin`, `.iso`
- Compressed Single-File: `.chd` (CHD v5 recommended for saving storage space)
- Multi-disc: `.pbp` (PSP Eboot format)

## BIOS Setup
- Recommended BIOS: `scph5501.bin` (US), `scph5500.bin` (JP), `scph5502.bin` (EU).
- Place BIOS files in `/sdcard/ROMs/bios/`. If no BIOS is provided, the core falls back to the open-source HLE BIOS.

## Hardware & OS
- **Target OS:** Fire OS 7 / Fire OS 8 (Android API 28–30), minSdk 25.
- **Architectures:** `armeabi-v7a` (Mandatory, Ari64 dynarec), `arm64-v8a`.
- **RAM Footprint:** ~60 MB resident set size.

## Directory Layout
- `upstream/`: Cloned PCSX ReARMed repository pinned at commit `c8816799b50388e61cfe237fe2cdbb7d8175f20a`.
- `patches/`: Ari64 dynarec tuning, SPU 48 kHz sync, and CHD async streaming patches.
- `build/`: NDK build configurations.
- `UPSTREAM.md`: Upstream architecture and DuckStation comparison.
- `FIRE_OS_CHANGES.md`: Detailed engineering log.
- `LICENSE-NOTICE.md`: GNU GPL-2.0 notice.
- `benchmark.md`: FPS and compatibility results.

## Build Command
```bash
cd agent/emulator/ps1/build
./build.sh
```

## ROM Location
Place PS1 games in `/sdcard/ROMs/ps1/`.
