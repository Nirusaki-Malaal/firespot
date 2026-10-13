# FireSpot: Streaming-Style Retro Console Hub for Amazon Fire TV

**FireSpot** is a native Kotlin + Jetpack Compose for TV emulation frontend and runtime engineered exclusively for Amazon Fire TV Sticks (Fire OS 7 / Fire OS 8). It features an entertainment UI modeled directly after the **Disney+ Hotstar TV app** layout, paired with zero touchscreen dependency, 100% Alexa Voice Remote and Bluetooth gamepad navigation, isolated process runtime memory separation (`android:process=":game"`), and pre-integrated Libretro emulation cores.

---

## What's New: The FireSpot Streaming Experience

- **Original Brand & Visuals**: FireSpot wordmark with electric blue-violet gradient (`#2D5BFF` to `#6A3DFF`), spark accents, warm badge tones (`#FFB800`), deep navy background (`#0B0F1A` to `#0C111B`), and 320x180 TV banner.
- **Left-Side Slim Navigation Rail**: Collapsed icon-only rail that expands on D-pad focus with labels: *Search, Home, Consoles, All Games, Favorites (My Space), Categories, Settings*, plus a local player profile avatar slot.
- **Hero Carousel**: ~55% screen height full-bleed backdrop art, gradient scrims, badges, metadata chips (Console, Year, Genre, Players, Rating), and direct `[Play]`, `[+ Favorites]`, `[Details]` action buttons with 6s auto-advance.
- **Curated Content Rows**:
  - **Continue Playing**: 16:9 landscape cards with play-progress bars and instant resume.
  - **Top 10 Games Today**: Authentic Hotstar-style large numerals (1-10) beside 2:3 poster cards.
  - **Recently Added** & **Favorites (Watchlist)**.
  - **Browse by Console**: Big hardware logo tiles with active game counts.
  - **Curated Genre Rows**: *Must-Play Platformers*, *Adrenaline & Action*, *Epic RPGs*.
  - **Best of Console**: Dedicated row per system.
- **Title Details Page**: Full-bleed backdrop, metadata bar, synopsis, quick action buttons, and three interactive tabs:
  1. *More Like This*: Poster row of related recommendations.
  2. *Details & Specs*: Core backend, ROM path, file size, checksum, and play time.
  3. *Save States*: Slot previews (1–4) with instant resume.
- **Two-Column Search**: On-screen D-pad TV keyboard on the left, live search results grid & trending titles on the right, plus voice mic remote trigger.
- **Two-Panel TV Settings**: Left categories menu (*Storage, Video, Audio, Controller, About*) paired with right action panels.
- **Restyled In-Game Pause Menu**: Translucent dark overlay with game title, console badge, slot selector, instant save/load states, scaling modes, and exit.

---

## Architecture Overview

```
FireSpot/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml       # Leanback manifest, isolated :game process, Fire OS storage permissions
│   │   ├── java/com/fireemu/
│   │   │   ├── FireEmuApp.kt         # Application container, Coil memory budget (15% heap cap)
│   │   │   ├── MainActivity.kt       # Compose for TV launcher with FireSpotTheme
│   │   │   ├── domain/models/        # Game (with genre, year, rating, players, description), ConsoleType
│   │   │   ├── data/
│   │   │   │   ├── local/room/       # Room DB (GameEntity, GameDao with streaming queries)
│   │   │   │   ├── local/datastore/  # SettingsStore (Preferences DataStore)
│   │   │   │   ├── repository/       # GameRepository, CoreRepository, SettingsRepository, SaveStateRepository
│   │   │   │   └── scanner/          # RomScanner, GameMetadataProvider, TitleCleaner, CoverArtResolver
│   │   │   ├── game/
│   │   │   │   ├── EmulatorActivity.kt # Runtime Viewport running in android:process=":game"
│   │   │   │   ├── AudioEngine.kt    # 48,000 Hz stereo low-latency AudioTrack streaming
│   │   │   │   └── InputHandler.kt   # Joypad bitmask mapper, remote shortcuts
│   │   │   ├── nativebridge/
│   │   │   │   └── LibretroBridge.kt # JNI wrapper binding into C libretro_bridge.so
│   │   │   └── ui/
│   │   │       ├── theme/            # FireSpot Palette (#0B0F1A), Inter/Poppins 10-ft TV Type, FireSpotTheme
│   │   │       ├── navigation/       # NavGraph, Screen routes (Home, Consoles, Games, Favorites, Categories...)
│   │   │       ├── components/       # FireSpotNavigationRail, FireSpotHeroCarousel, FireSpotCards, TvButton
│   │   │       └── screens/          # Splash, Home, Details, Library, FilteredGrid, Search, Settings, Mapping
│   │   ├── jniLibs/
│   │   │   ├── armeabi-v7a/          # 32-bit userland ARMv7 NEON cores + bridge
│   │   │   └── arm64-v8a/            # 64-bit ARMv8 cores + bridge
│   │   └── res/                      # TV banner (320x180), original FireSpot icon, focus states, strings, colors
│   └── build.gradle.kts
├── emulator/
│   ├── _shared/build/outputs/
│   │   └── firespot-debug.apk        # Standalone signed installable APK (2.45 MB)
│   ├── nes/                          # FCEUmm core (armeabi-v7a NEON, 60 FPS locked)
│   ├── gameboy/                      # Gambatte core (armeabi-v7a NEON, 60 FPS locked)
│   ├── gba/                          # mGBA core (armeabi-v7a ARM JIT, 60 FPS locked)
│   ├── ps1/                          # PCSX ReARMed core (armeabi-v7a Ari64 dynarec, 60 FPS locked)
│   └── psp/                          # PPSSPP core (armeabi-v7a & arm64-v8a)
├── test_verification.py              # Automated verification & TV UX audit suite
└── web/index.html                    # Laptop simulation player with Disney+ Hotstar UI & interactive games
```

---

## Supported Consoles & Libretro Cores

| Console | Core Library | Target ABI | Hardware Performance Profile |
|---|---|---|---|
| **Nintendo (NES)** | `libretro_fceumm.so` | `armeabi-v7a` | 60 FPS locked, <5% CPU |
| **Game Boy / GBC** | `libretro_gambatte.so` | `armeabi-v7a` | 60 FPS locked, <8% CPU |
| **Game Boy Advance** | `libretro_mgba.so` | `armeabi-v7a` | 60 FPS locked, <15% CPU |
| **Sony PlayStation 1** | `libretro_pcsx_rearmed.so`| `armeabi-v7a` | 60 FPS locked, <22% CPU |
| **PlayStation Portable** | `libretro_ppsspp.so` | `armeabi-v7a` / `arm64-v8a` | 30–60 FPS, multi-threaded GLES |

---

## Sideloading onto Fire TV Stick (ADB)

1. Enable Developer Options & ADB Debugging on your Fire TV Stick:
   - `Settings` -> `My Fire TV` -> `About` -> Click on your device name **7 times**.
   - `Settings` -> `My Fire TV` -> `Developer Options` -> Turn ON **ADB Debugging** and **Apps from Unknown Sources**.
2. Find the IP address: `Settings` -> `My Fire TV` -> `About` -> `Network`.
3. Connect and install the signed APK via your terminal:

```bash
adb connect <FIRE_TV_IP>:5555
adb install -r emulator/_shared/build/outputs/firespot-debug.apk
```

---

## Adding ROMs and Custom Artwork

- **ROM Directory Structure**:
  ```
  /sdcard/ROMs/
  ├── nes/       (*.nes, *.fds, *.zip)
  ├── gameboy/   (*.gb, *.gbc, *.zip)
  ├── gba/       (*.gba, *.zip)
  ├── ps1/       (*.cue, *.bin, *.chd, *.pbp)
  └── psp/       (*.iso, *.cso, *.pbp)
  ```
- **FireSpot Custom Artwork Convention**:
  ```
  /sdcard/FireSpot/art/<console>/<clean_game_name>/
  ├── poster.jpg    (2:3 vertical poster card)
  └── backdrop.jpg  (16:9 hero background art)
  ```
- If custom images are not provided, FireSpot automatically renders high-quality console-themed gradient fallbacks with verified metadata.

---

## TV Remote Navigation & Controls

| Button | Action in UI | Action in Emulation |
|---|---|---|
| **D-pad (Up/Down/Left/Right)** | Move TV focus / expand rail | D-pad / directional stick |
| **Select (Center Circle)** | Launch / Confirm selection | Action Button A (Jump) |
| **Back Button** | Pop screen / collapse rail | Hold or Menu for pause overlay |
| **Play / Pause** | Toggle play / details | Action Button B (Run / Dash) |
| **Menu Button (≡)** | Open quick options | In-game pause & state menu |
| **Alexa Mic** | Trigger voice search | - |
