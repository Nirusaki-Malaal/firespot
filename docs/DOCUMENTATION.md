# FireSpot documentation

FireSpot is a Kotlin/Compose TV launcher connected to a native Libretro frontend. The active app is `app/`; `emulator/_shared/src/main/jni` supplies the native bridge. The public core catalog lives in [firespot-cores](https://github.com/Nirusaki-Malaal/firespot-cores). This document describes the implemented app; planned work is in IMPLEMENTATION_PLAN.md.

Current local Android captures of Home, Discover, the input tester, and storage/playtime are in [docs/screenshots/current](screenshots/current/). These are app screenshots from a TV-profile emulator, not official Fire TV acceptance evidence.

## Installation

Get an APK from the project Releases page. Standard (`com.fireemu`) and bundled Game Boy (`com.fireemu.demo`) builds can coexist and have independent libraries/settings/saves. Both release APKs target Android API 28+ and include ARMv7, ARM64 and x86_64 support. An Android APK targets Fire OS; it does not become a Vega OS app automatically.

The standard app contains the native frontend bridge, not emulator cores. On first Play it obtains the matching signed core, validates it, and opens the game. An internet connection is needed for the first download. The bundled Game Boy build packages Gambatte for the two original starter games. Once installed, games and cores are local; play, saves and settings need no account or network.

For a developer-controlled ADB device:

```sh
adb devices
adb -s YOUR_SERIAL install -r firespot-standard.apk
adb -s YOUR_SERIAL shell am start -n com.fireemu/com.fireemu.MainActivity
```

Release APKs have a separate stable distribution signing key. Earlier debug APKs may have a different certificate. Android will reject an in-place update with a mismatched certificate. Preserve any needed data before deliberately uninstalling a debug build; `adb uninstall` removes its private saves. Standard and bundled Game Boy builds also use different application IDs.

An Amazon virtual-device session can be used to install and inspect the Android APK. Amazon remote-session authentication and temporary ADB connectivity are separate from FireSpot's public game downloads. See AWS_TESTING_SETUP.md and CONSOLE_SMOKE_TEST_PLAN.md for setup and test steps. Never put AWS credentials into the APK.

## First play

1. Open FireSpot and finish its welcome screen.
2. Choose Open library. Orbit Collector and Pocket Courier are seeded idempotently, preserving existing progress and favorites.
3. Open a title and choose Play from start. Standard obtains console support if needed; the bundled Game Boy build uses its included core.
4. Use the D-pad or a keyboard to move. Menu/Back/Escape opens pause.
5. Save & Exit returns to game details. Resume appears when a nonempty autosave is available.

The included titles are intentionally small original checkpoint games. Move the player onto diamonds to score; A/B produce a tone. Signal Runner is a downloadable variant with battery-backed score persistence. These are not commercial games or a claim of a large licensed catalog.

## Discover games and console support

Open Discover games from Home or Discover games & console support from Settings. The Games tab shows title, description, console, author, license and size. An installed starter title opens its existing library record rather than creating another copy. Get downloads an uninstalled title, verifies its bytes, and opens its details.

Discover has Games, Consoles, Installed and Console support views. Search by title, description, author or genre; filter by console and sort alphabetically, by download size or release year. Console cards show actual catalog and installed-game counts. An empty system offers an import action.

Console support lists the published cores. Each package is selected for the running process ABI, rather than assuming a 64-bit-capable CPU means a 64-bit app process. Unsupported ABIs have no install action. A downloadable binary confirms package availability only; use the [console matrix](CONSOLES.md) and [validation record](VALIDATION.md) for title and device test results.

After a core is installed, its card provides **Import a game** to open the file picker and **Browse game library** to open the in-app ROMsGames browser. Imported titles are validated and copied into the app library; supported catalog downloads follow the same import path.

Refresh catalog fetches the signed remote catalog. A verified embedded catalog remains available offline. A valid newer cached revision takes precedence over the embedded one; rollback to an older revision is rejected. Installed local files remain usable when refresh fails.

Downloads show status, percentage, cancellation and a specific failure message. Downloads are serialized and scoped to their foreground screen/preparation job. Leaving that screen can cancel the operation. Retry starts again; this version does not advertise a background download queue, HTTP range resume or automatic retry policy.

## Import your cartridges

Settings → Import a game, or **Import a game** on an installed console card, uses the system's Android document provider. Supported imports are `.gb`, `.gbc`, `.nes`, `.gba`, `.sfc`, `.smc`, `.sms`, `.gg` and `.sg` cartridges. GB/GBC imports are limited to 8 MiB; other cartridge imports to 32 MiB. The importer validates basic file shape (including the iNES header/declared payload), bounds reads, hashes content, writes to a temporary file and then renames it. Files are copied into private app storage; losing the original file or picker permission does not remove that copy.

Importing the same bytes again reuses their content-derived destination and library record. Import does not download arbitrary web URLs or treat a filename as proof of compatibility. A cartridge may still fail core loading or use unsupported peripherals. Disc sets, M3U playlists and PS1 CUE/BIN sidecar sets need a separate transfer/import flow and are not exposed as a supported one-file cartridge import.

DocumentsUI navigation differs by device. On the local TV-profile AOSP image, use the file focus target and Enter; the preview button is separate. A Fire TV with no document provider reports that limitation. The downloadable/seeded original games do not depend on a picker.

## Library and search

Library supports A–Z, recent-played and date-added sorting. System and genre chips cycle through values in the installed library. Clear filters restores the full collection. Favorites are explicit user choices; Continue playing reflects real history rather than placeholder cards.

Search accepts both its on-screen keyboard and printable keys from a physical keyboard. Backspace edits. Escape returns to the previous screen. Lists use stable game IDs and cached sorting. Artwork has a monochrome fallback when absent; missing art does not block play.

## Controls

| Action | Fire TV remote | Keyboard | Gamepad default |
|---|---|---|---|
| Move | D-pad | Arrows or WASD | D-pad or left stick |
| A | Select | Z or J | A |
| B | Play/Pause | X or K | B |
| Start | Fast Forward | Enter / Numpad Enter | Start |
| Select | Rewind | Right Shift / Tab | Select |
| X / Y | — | C / V | X / Y |
| L / R | — | Q / E | L1 / R1 |
| Pause | Menu / Back | Escape / F1 | Select + Start |

In launcher menus, arrows/Tab navigate and Enter activates. Printable keys type on the Search screen. Escape goes back. Gameplay keyboard shortcuts do not convert typed search text into menu navigation.

Bluetooth and USB keyboards use Android input events; pairing is performed in the operating system, not inside FireSpot. No Bluetooth scanning permission is required to receive normal keyboard events. Physical Bluetooth latency/reconnection behavior still needs device testing. Input is cleared on focus loss, pause and input-device changes/removal to avoid held directions. Multiple keys mapped to the same game action are tracked separately, so releasing one does not release the other.

Settings → Remap controls captures a key for each exposed game control, including X/Y/L/R. Reset Defaults restores the stored controller defaults; fixed keyboard/remote fallbacks remain available. Menu, Back, Home, Escape and F1 remain navigation/pause keys. Analog stick-to-D-pad conversion uses a fixed dead zone; per-controller calibration and multiple-player routing are future work.

Settings also includes separate ambient-audio and interface-feedback toggles. Ambient audio runs at low volume while browsing and pauses when gameplay starts. Focus/click feedback uses short sound and supported device haptics. Actual speaker/headset output should be checked on the intended Fire TV device.

## Saves and recovery

Four manual state slots are available through the pause menu. Save writes an atomic replacement; Load restores that core snapshot. Play from start does not load an emulator snapshot. Resume tries the current autosave, then two older backup generations. A successful fallback produces a recovery message. If all snapshots fail, the app starts using any cartridge progress it can restore and explains the failure.

Save & Exit and lifecycle pause save snapshots and battery RAM where the core exposes it. Cartridge `.srm` data is distinct from emulator states: a new game boot can still retain the game's own saved progress. Signal Runner demonstrates this with its battery-backed score. Games without battery RAM do not create an artificial cartridge save. Binary state compatibility across future core versions is not guaranteed; the released catalog pins the current core version.

Storage locations under the app's private files directory:

| Directory | Purpose |
|---|---|
| `games/starter/` | Two seeded original cartridges |
| `games/downloads/HASH/` | Verified catalog downloads |
| `games/imports/` | Content-named user cartridge copies |
| `cores/ABI/CORE/HASH/` | Versioned, verified native packages |
| `states/CONSOLE/` | Manual slots and autosave generations |
| `battery/CONSOLE/` | Cartridge RAM, keyed by cartridge path |
| `bios/` | User-provided console firmware, when required |
| `catalog-cache.json` | Previously verified catalog envelope |

A full user-facing save export/import and storage cleanup manager is not implemented yet. Do not use Clear storage or uninstall as a routine troubleshooting step when progress matters. Native snapshot names still use the cartridge basename, so user-created collections with identical names require care; a future migration will use collision-resistant game identities.

## Display and performance

Original aspect ratio, integer scaling and fill-screen scaling are implemented and persisted. The optional frame counter can be enabled from Settings or pause. The renderer currently uses software frames into an Android Surface. No OpenGL/Vulkan hardware-rendering context is exposed to cores, which limits immediate PSP/N64/Dreamcast-style integration.

The UI uses static charcoal gradients, etched grid lines and thin highlights with small corner radii. It avoids backdrop blur, animated noise, focus zoom and artwork crossfades. These choices reduce UI work, but they are not measured Fire TV performance guarantees. Record actual launcher frame times, core frame pacing, memory and audio underruns on the intended device before adding heavier effects.

## Build from source

Use JDK 17, Android SDK/build-tools 34, NDK 28.2.13676358 and the pinned Gradle wrapper. Set ANDROID_HOME/ANDROID_SDK_ROOT, or create an untracked local.properties containing sdk.dir. Do not commit machine paths.

```sh
./scripts/build_gameboy.sh
./gradlew :app:assembleStandardDebug :app:assembleDemoDebug \
  :app:lintStandardDebug :app:lintDemoDebug :app:testStandardDebugUnitTest
```

Default ABIs are ARMv7 and ARM64. Add `-PincludeX86=true` for a local x86_64 emulator. Standard can build without bundled core assets; demo requires the pinned core build to populate src/demo/jniLibs. Regenerate the two starter games with scripts/generate_starter_games.py. The generator also exposes the battery-backed Signal Runner variant used by the signed catalog release.

To rebuild the extra downloadable cores:

```sh
export ANDROID_NDK_HOME="$ANDROID_HOME/ndk/28.2.13676358"
python3 scripts/build_online_cores.py
```

Build scripts fetch fixed upstream commits, preserve core licenses and package corresponding source. They do not fetch commercial games or BIOS. See LIBRARY_SOURCES.md for browsing/source boundaries, CI_CD.md for signed release automation, VALIDATION.md for tests, and CONSOLES.md for the honest compatibility boundary.

## Architecture and trust

Compose navigation connects the launcher, library, details, settings, mapping and Discover screens. Room stores game metadata/history/favorites; preferences/DataStore store controls and display settings. FireEmuApp owns repository instances. The native bridge validates required Libretro entry points/API version, provides video/audio/input callbacks and loads cartridge bytes or a path according to core requirements.

The download repository verifies an RSA/SHA-256 catalog with a public key embedded in the APK. It permits HTTPS GitHub release hosts and explicitly checks redirects. Artifact size, SHA-256 and native ELF architecture are checked before same-directory atomic installation. Content-addressed destinations prevent partially downloaded data from becoming an installed core. Existing installs survive a failed/cancelled replacement.

The catalog signing key and APK signing key are different credentials. Neither belongs in Git. The catalog publisher can add signed revisions, but cannot make an old APK support an unimplemented rendering interface or an unsupported console importer. App changes still require a new APK.

## Troubleshooting

| Symptom | Action |
|---|---|
| First Play cannot prepare console support | Check connectivity, free space and catalog status; retry from details |
| Invalid signature/hash/CPU | Retry the published release; do not bypass validation |
| Picker absent | Use seeded/downloadable titles for initial testing; do not rely on ROM upload |
| Core loads but a game fails | Check console, format, required BIOS and the compatibility notes in the console matrix |
| No sound in a local headless test | The recorded emulator session uses `-no-audio`; host callback tests prove generated samples, not speaker output |
| Held direction after device loss | Pause/resume; report device/input details if it persists |
| Update rejected by Android | Check application ID and signer before removing an installed build |
| Autosave damaged | Resume automatically tries retained backups; preserve files before manual recovery |

Do not publish AWS credentials, release keystores, signing passwords, user ROMs or private save files in bug reports.

## Input doctor

Open Settings → Controller & keyboard tester. The device list updates when Android reports a connection, disconnection or configuration change. Start live test to capture key-down/key-up events, held buttons and raw joystick axes, including Android's reported flat threshold. Back or Escape stops capture before leaving the page. Leaving the screen or pausing the app removes capture, so normal navigation and gameplay keep their existing bindings. Pair a Bluetooth device in the TV's system settings. This tool does not pair devices, write calibration or create per-device mapping profiles.

## Storage and playtime

Settings → Storage & playtime shows managed game, core, snapshot/recovery, battery-save and image-cache bytes, plus filesystem free space. Refresh calculates folder sizes off the UI thread. Clear image cache uses Coil's cache APIs and preserves cartridges, console software and progress. The page also shows library/favorite counts, total recorded playtime and the ten most-played games. Session totals update when gameplay ends. Folder totals exclude the APK, database, preferences and external cartridge locations.

## ROMsGames browser and ZIP import

Discover → ROMsGames library or Settings → Browse ROMsGames opens the provider inside FireSpot. Navigate its console and game pages, complete any provider challenge, select a supported download and confirm import. After the status says the game was added, choose My library, return Home and open Library. The first Play downloads the console core if needed. Browsing the website is separate from the signed original-game catalog.

Documents import and website downloads share the same bounded importer. A ZIP must contain exactly one supported cartridge/image. GB/GBC imports are limited to 8 MiB, most other cartridges to 32 MiB, PS-X executables to 4 MiB, and PS1 ISO/PBP files/browser downloads to 768 MiB. Multiple-disc sets and BIOS-dependent titles need further setup. Website listings for NDS, PSP, PS2 and other unavailable consoles do not add emulation support.

## Runtime memory and rendering

Navigation-owned ViewModels cancel work when their entries are cleared. Rail navigation reuses destinations instead of indefinitely stacking screens. Cover images decode at 480×320 or smaller and the memory cache uses at most 8% of the heap, capped at 16 MiB, and clears when the UI is hidden. The external browser is created on demand and destroyed on exit. No backdrop blur or continuously animated textures run in the launcher.

Software video reuses a horizontal source-pixel map, converts each unique scaled source row once and copies repeated rows. Letterbox clearing touches borders rather than clearing the full output before drawing it again. This reduces redundant pixel conversion for handheld-size games; it is not a target-device FPS guarantee. Frame timing and memory measurements must identify device and build type.

Home and the rail's collection views share a lifecycle-owned library ViewModel, avoiding four separate library query/sort pipelines. The external website uses a separate `:browser` process so its Chromium working set is independent of the launcher/game heap. Android may retain that process in its cache after exit and reclaims it under pressure; destroying a WebView does not guarantee immediate process memory reclamation.
