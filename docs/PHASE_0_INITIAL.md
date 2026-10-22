# Phase 0: build and runtime proof

## Status

Phase 0 is **partially verified**. The APK and genuine core build locally, and the core executes an original cartridge on Linux. Official Fire TV execution is pending an Amazon virtual-device session or hardware. No device was connected through ADB during this work.

This milestone fixes the build/runtime foundation. It does not implement the production online core installer, a downloadable game catalog, native battery-save persistence, analog controls, multiplayer, or all planned consoles.

### Verification recorded on October 22, 2026

| Check | Result |
|---|---|
| Full rerun of APK assembly and Android lint | Passed; all 48 tasks executed |
| Android lint | 0 errors, 145 warnings; inherited warnings remain to triage |
| APK signature | Verified APK Signature Scheme v2 |
| APK native payload | ARMv7 and ARM64 frontend bridge only; no emulator cores |
| Separate core build script | Executed successfully for ARMv7, ARM64, and x86_64 |
| Genuine Linux core execution | 216 video frames at 160×144; input changes rendered output; nonzero audio |
| State recovery | Restored snapshot reproduces the same five video frames under the same input |
| Negative artifact tests | Wrong CPU label and modified core bytes both rejected |
| Fire TV / Android installation and playback | Not tested; no connected device/session |
| GitHub Actions | Workflow added; no remote workflow run or publication performed |

The approximately 26 MiB APK is debug-signed and includes developer diagnostics. No release-size or frame-rate claims are made.

## Changes from the upstream baseline

- Imported the Kotlin application source while excluding prebuilt APKs, mislabeled native binaries, sample ROMs/BIOS, and debug signing keys.
- Added a Gradle 8.6 wrapper with distribution checksum; use JDK 17, SDK/build tools 34, NDK 28.2.13676358.
- Connected the Kotlin app to an actual NDK bridge build. Fixed Gradle/manifest/resources and mismatched UI/repository APIs that prevented compilation.
- Corrected the native JNI class/method bindings to match the Kotlin bridge and removed swallowed load errors.
- Resolve separate emulator cores from private app storage rather than bundling them in the base APK.
- Pass ROM bytes to cores that require data instead of a pathname, retain their lifetime until unload, and reject missing Libretro entry points.
- Use core-reported frame/audio timing; fix red/blue conversion for Android surfaces; handle failed core/game launch with a recoverable error.
- Serialize bridge calls and use a temporary file plus flush/sync/rename for state saves.
- Make Menu repeat-safe, separate hat/key masks, and clear held inputs at pause/overlay/focus loss. Analog/per-device mapping is follow-up work.
- Add an original test cartridge, a debug-only runtime-check screen, separate pinned ARM/x86_64 core artifacts, source/license archives, integrity gates, and a CI workflow.

## 1. Set up Amazon testing without owning a Fire TV

1. Open https://developer.amazon.com/ and sign in/create an Amazon Developer account. Complete the account onboarding yourself.
2. In Developer Console, open **Tools & Services → Appstore Quality Central**.
3. On **Live Device Interaction — Virtual Devices**, choose **Get Started** and select an available **Fire OS** device. Availability and session limits depend on your account/service.
4. Use the session's APK upload/install action for `artifacts/firespot-phase0-debug.apk`.
5. Open **FireSpot Runtime Check** from the device's launcher. This second launcher entry exists only in the debug APK.
6. Install the matching separate Game Boy core using the file-import or ADB route below. The base APK intentionally has no emulator core.
7. Run the acceptance checklist and record the service's actual screen. Download logs/video if supported.

Reference: https://community.amazondeveloper.com/t/live-device-interaction-with-virtual-devices-is-now-available/28664

If account onboarding or session access is blocked, document that exact issue. A generic Android AVD can help debug the APK but is not evidence that it runs on Fire TV. Vega's virtual device requires a Vega app; it cannot run this Android APK.

## 2. Install the separate test core

### File-import route

If the session exposes a way to place a file on the device, put the appropriate `artifacts/gameboy/<abi>/libretro_gambatte.so` there. In **FireSpot Runtime Check**, select **Import Phase 0 Game Boy core** and choose the file. The probe checks exact size and SHA-256 against metadata embedded in this debug APK before installation. No picker/provider may be available on a given Fire TV runtime; use ADB if available.

This flow tests a separately delivered core. It is a developer diagnostic, not an online storefront, signature infrastructure, or app-store approval result. The hash metadata must match the core build; run the core build before the APK build when rebuilding both.

### ADB route

Use the remote session's documented ADB connection details if it provides remote ADB. A web console that only runs individual shell commands is not necessarily a binary-transfer connection; do not assume the local helper works through it. For a Fire OS device available to local ADB:

```sh
adb devices
./scripts/install_runtime_probe.sh artifacts/firespot-phase0-debug.apk
```

The helper installs the APK, copies a matching separate core through debug `run-as`, and opens the test screen. It does not bypass device installation/consent flows or work on a non-debug package. Set `ADB` to your ADB executable if needed, and select a single target using standard ADB configuration.

Native code loaded from private app storage remains a **compatibility gate**. If Fire OS refuses loading, capture `dlerror` in logs and record the device/API/ABI. Do not downgrade the target SDK or disable device protections. Revisit the separate-core delivery strategy in Phase 1 if necessary.

## 3. Runtime acceptance checklist

Record each item as pass/fail/not tested with evidence. Do not mark this list passed from the Linux smoke test.

- [ ] Install APK and open normal launcher; onboarding and library render without crashing.
- [ ] Navigate with the virtual remote alone; verify select, Back and focus visibility.
- [ ] Open Runtime Check and record device model, OS/API, process bitness and core path.
- [ ] Import/side-load the core outside the APK and run **FireSpot Signal**.
- [ ] Confirm a checkerboard appears and changes when pressing a horizontal D-pad direction.
- [ ] Select (A) or Play/Pause (B) produces audible tone; observe actual audio, not just a callback count.
- [ ] Menu opens Pause once per press; returning leaves no direction held.
- [ ] Save a slot, move the pattern, load it, and observe restoration.
- [ ] Exit, relaunch and verify autosave restoration.
- [ ] Background/resume and surface recreation do not start duplicate emulation threads or crash.
- [ ] Missing/wrong/unloadable core produces an actionable error rather than a fake success.
- [ ] Record a short runtime video and export logs.

Useful local ADB commands:

```sh
adb shell am start -n com.fireemu/.debug.RuntimeProbeActivity
adb logcat -s FireEmuBridge FireSpotRuntime AndroidRuntime
```

The diagnostic cartridge is authored for this project and dedicated under CC0-1.0. D-pad scrolls the pattern; A/B trigger tone. It is deliberately small and does not test every emulator feature. It contains no commercial game or proprietary BIOS. Its zeroed cartridge logo assumes built-in emulator startup, so do not use it to claim physical Game Boy compatibility.

## 4. Build and integrity verification

```sh
# Install the pinned SDK/NDK and use JDK 17 first.
./scripts/build_gameboy.sh
./gradlew :app:assembleDebug :app:lintDebug
python3 scripts/verify_artifacts.py --apk app/build/outputs/apk/debug/app-debug.apk --cores artifacts/gameboy
```

The core script pins Gambatte to `d9d6cd06382d1ced30de34d56d3609452323dab1`. Corresponding source and GPL notice accompany output. Core artifacts are not uploaded or published by this task.

Only the Google Play-specific `ExpiredTargetSdkVersion` lint check is excluded because this app targets Fire OS. Other lint checks remain active. Existing warnings must be tracked; passing compilation/lint does not imply the complete product UX has been verified.

`artifacts/evidence/` contains build output, signature inspection, artifact hashes and host-test results from this run. These are local build evidence, not Fire TV screenshots or runtime certification.

## 5. Hackathon delivery gate

After the official runtime checklist passes, capture the app running on Fire TV in an English video under three minutes. Lead with real playback and remote interaction. Describe implemented functionality accurately and separate roadmap ideas. Explain what changed during the hackathon and include actual product feedback/friction, not invented service experiences.

Deadline recorded in the product plan: October 24, 2026 at 00:30 IST. Verify the organizer page again before submitting: https://amazonappdev2026.devpost.com/
