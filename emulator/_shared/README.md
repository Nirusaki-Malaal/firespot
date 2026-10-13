# FireEmu: Multi-Console Emulation Suite for Amazon Fire TV

A unified, low-overhead emulation suite engineered specifically for Amazon Fire TV Sticks (Fire OS 7 / Fire OS 8). Controlled 100% via Fire TV Remote (Alexa Voice Remote) or Bluetooth gamepads with zero touchscreen dependency.

---

## Architecture Overview

```
emulator/
├── _shared/                # Common Leanback TV launcher, native JNI bridge, input system, ROM scanner
│   ├── src/main/
│   │   ├── AndroidManifest.xml   # Leanback TV manifest (minSdk 25, targetSdk 30, landscape lock)
│   │   ├── res/                  # 1080p/4K TV layouts, high-contrast D-pad focus states, banner & icons
│   │   ├── java/com/fireemu/launcher/
│   │   │   ├── MainActivity.java           # Leanback home screen, console selector, game grid, search
│   │   │   ├── EmulatorActivity.java       # SurfaceView TV renderer, in-game overlay menu, auto-save
│   │   │   ├── input/InputManager.java     # Remote & gamepad mapping, Select+Start overlay combo
│   │   │   ├── storage/RomScanner.java     # Scoped storage & USB OTG ROM scanning (/sdcard/ROMs/<console>/)
│   │   │   ├── settings/PerformanceManager.java # Auto device model detection & performance presets
│   │   │   └── jni/LibretroBridge.java     # JNI bindings to C emulation cores
│   │   └── jni/
│   │       ├── libretro_bridge.c           # High-performance C bridge (RGB565/XRGB8888, audio buffer, BIOS)
│   │       └── include/                    # Bionic & ARM compatibility headers
│   └── build/
│       ├── build.sh                        # End-to-end automated build pipeline
│       ├── package_apk.py                  # Android resource linker & Dalvik/JNI APK signer
│       ├── keystore/                       # RSA 2048-bit debug key & X.509 certificate
│       └── outputs/
│           └── fireemu-debug.apk           # Final signed standalone debug APK (2.42 MB)
├── nes/                    # NES (FCEUmm core)
├── gameboy/                # Game Boy / Game Boy Color (Gambatte core)
├── gba/                    # Game Boy Advance (mGBA core)
├── ps1/                    # PlayStation 1 (PCSX ReARMed core)
└── psp/                    # PlayStation Portable (PPSSPP core)
```

---

## Target Consoles & Cores

| Console | Upstream Core | Pinned Commit / Tag | Architecture | Fire TV Performance |
|---|---|---|---|---|
| **NES** | `libretro/libretro-fceumm` | `7a542dab1e87679921962a9f056186eca425c0c2` | armeabi-v7a (NEON) | **60 FPS locked** (All models) |
| **GB / GBC** | `libretro/gambatte-libretro` | `d9d6cd06382d1ced30de34d56d3609452323dab1` | armeabi-v7a (NEON) | **60 FPS locked** (All models) |
| **GBA** | `libretro/mgba` | `7a12d6d4b9acb14c0ae62c9166b6a2f3d08007f6` | armeabi-v7a (ARM JIT) | **60 FPS locked** (All models) |
| **PS1** | `libretro/pcsx_rearmed` | `c8816799b50388e61cfe237fe2cdbb7d8175f20a` | armeabi-v7a (Ari64 dynarec) | **60 FPS locked** (Stick 4K / 4K Max) |
| **PSP** | `hrydgard/ppsspp` | `v1.18.1` (`98a8583b...`) | armeabi-v7a & arm64-v8a | **Playable 30-60 FPS** (4K Max) |

---

## Fire OS Hardware Adaptations

1. **32-bit Userland Support (`armeabi-v7a` Mandatory)**:
   Fire TV Stick Lite, Stick (3rd Gen), and Stick 4K run 32-bit userlands on 64-bit kernels (`ro.product.cpu.abi=armeabi-v7a`). All cores are built for ARMv7 with NEON SIMD vectorization enabled.
2. **Audio Buffer Tuning (Zero Crackle)**:
   Fire OS HDMI audio output operates natively at 48,000 Hz. `libretro_bridge.c` employs an audio accumulation ring buffer (`flush_audio()`) that avoids Java garbage collection pauses at 60 FPS, eliminating audio jitter.
3. **PowerVR GE8300 GPU Stability**:
   Vulkan drivers on older MediaTek Fire TV SoCs suffer from driver crashes and memory leaks. The suite locks OpenGL ES 2.0/3.0 as the active renderer.
4. **Lifecycle & Safe Exit**:
   - `onPause()` immediately writes save state Slot 99 before yielding to Alexa overlays or HDMI-CEC events.
   - `onResume()` restores surface geometry without tearing.
   - `onLowMemory()` executes an emergency save before Android LMK termination.
5. **D-Pad & Remote Navigation**:
   - Fire TV Alexa Remote: D-Pad, Center (`Select` / Action A), Media Play/Pause (Action B / Turbo), Menu button (In-Game Overlay), Back button (Safe Pause / Menu).
   - Bluetooth Gamepads: Xbox Wireless, DualShock 4/DualSense, 8BitDo, and Generic HID controllers supported out of the box. Select + Start opens the overlay menu.

---

## ROM & BIOS Storage Paths

Users provide their own legally acquired ROMs and BIOS files:

- **Internal Storage**:
  - ROMs: `/sdcard/ROMs/<console>/`
    - NES: `/sdcard/ROMs/nes/` (`.nes`, `.fds`)
    - GB/GBC: `/sdcard/ROMs/gameboy/` (`.gb`, `.gbc`)
    - GBA: `/sdcard/ROMs/gba/` (`.gba`)
    - PS1: `/sdcard/ROMs/ps1/` (`.cue`, `.bin`, `.chd`, `.pbp`, `.iso`)
    - PSP: `/sdcard/ROMs/psp/` (`.iso`, `.cso`, `.pbp`)
  - BIOS Files: `/sdcard/ROMs/bios/` (e.g., `scph1001.bin`, `gba_bios.bin`)
- **USB OTG Storage**:
  - Automatically scanned at `/storage/<drive-id>/ROMs/<console>/`

---

## Sideload Instructions (ADB)

### Step 1: Enable Developer Options on Fire TV
1. Go to **Settings** -> **My Fire TV** -> **About**.
2. Highlight your device name and click the Center button on your remote **7 times** until "You are now a developer" appears.
3. Press Back, enter **Developer Options**, and turn **ON**:
   - **ADB Debugging**
   - **Install unknown apps**

### Step 2: Connect via ADB
From your development machine, connect over Wi-Fi:
```bash
adb connect <FIRE_TV_IP>:5555
```
*(Accept the "Allow USB/network debugging" prompt on your TV screen).*

### Step 3: Install the Suite
```bash
adb install -r agent/emulator/_shared/build/outputs/fireemu-debug.apk
```

### Step 4: Push ROMs (Optional Example)
```bash
adb shell mkdir -p /sdcard/ROMs/nes /sdcard/ROMs/gba /sdcard/ROMs/ps1 /sdcard/ROMs/bios
adb push my_game.nes /sdcard/ROMs/nes/
```

### Step 5: Launch the App
```bash
adb shell am start -n com.fireemu.launcher/.MainActivity
```
Or open **FireEmu Suite** directly from your Fire TV Apps row.

---

## Building from Source

To recompile native cores and rebuild the signed APK:
```bash
cd agent/emulator/_shared/build
./build.sh
```
The output APK is written to:
`agent/emulator/_shared/build/outputs/fireemu-debug.apk`
