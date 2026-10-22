# Validation record

## Verified locally

- Both debug flavors build and lint with zero errors. Existing warnings remain and are not a claim of production certification.
- JVM security tests reject catalog tampering, a different publisher key, altered artifact length/hash, the wrong native CPU, and untrusted/cleartext redirect destinations.
- Packaging tests enforce a core-free standard APK and expected demo core/bridge ABIs; negative tests reject corruption and the wrong CPU.
- Genuine host Gambatte tests exercise original games, 160×144 video, nonzero generated audio, input, checkpoint scoring and deterministic state restoration.
- Phase 0 has 16 recorded local Android checks, including native gameplay, save/load, resume/reset, favorite/history/search, settings, remapping capture/reset, Pocket Courier and file-provider import/play.
- The standard app's online Android walkthrough downloads the signed Gambatte core from public GitHub, verifies its exact bytes, launches a game, exercises keyboard movement/pause/typed search, refreshes the signed catalog, obtains Signal Runner, saves 8 KiB battery RAM, retains autosave backups, resumes across process restart and applies/clears library filters.
- An additional native roundtrip seeds a known cartridge RAM score, boots with Play from start, exits and verifies the score persisted independently of snapshot restoration.
- Unauthenticated public release checks download all 12 published native binaries, verify their ELF/size/SHA-256 metadata, fetch corresponding source/licenses, verify the RSA catalog signature and verify all three game downloads.

Evidence is under artifacts/evidence in a local checkout and attached to application releases where provided. README screenshots are actual app captures. The local test image is generic AOSP Android 11/x86_64 with a TV device profile, not an official Fire TV image. Headless testing uses no speaker output; generated audio samples are tested on the host, not heard or measured on a Fire TV.

## Reproduce

```sh
./scripts/build_gameboy.sh
./gradlew :app:assembleStandardDebug :app:assembleDemoDebug \
  :app:lintStandardDebug :app:lintDemoDebug :app:testStandardDebugUnitTest -PincludeX86=true
python3 scripts/verify_artifacts.py --apk app/build/outputs/apk/standard/debug/app-standard-debug.apk
python3 scripts/verify_artifacts.py --apk app/build/outputs/apk/demo/debug/app-demo-debug.apk --demo
python3 -m unittest discover -s tests -p 'test_*.py'
python3 scripts/verify_online_release.py
```

With a developer-owned Android device and the appropriate debug APK already installed:

```sh
python3 tests/android_demo_smoke.py --serial YOUR_SERIAL
python3 tests/android_online_smoke.py --serial YOUR_SERIAL --out artifacts/evidence/phase1
```

The online test expects an initialized/fresh standard installation and public network connectivity. Select the intended device explicitly. Do not clear data on a personal device with saves simply to run a test. The provider-specific extras script targets the local AOSP DocumentsUI layout and may need navigation changes for another provider. Test code does not automatically establish an Amazon session.

## Signed release and remote CI

The signed/minified standard APK also passed four local Android walkthrough checks: real on-demand core/gameplay, keyboard/pause/controls/save/resume, catalog acquisition/play of a new title, and search/library/resume across process restart. The installed release is version 0.2.0, versionCode 2, without the DEBUGGABLE flag. Its certificate and packaging reports are recorded with the evidence.

[Remote Android CI passed](https://github.com/Nirusaki-Malaal/firespot/actions/runs/39773037599), including both debug distributions, lint, security tests, packaging checks and real host core checks. See docs/validation for compact public records; generic Android results still do not certify Fire TV.

## Still required

- Official Fire TV hardware or official virtual-device acceptance from install through play, audio, pause, save/exit, restart and resume.
- In-app ambient playback on a speaker/headset: the local emulator has no active PulseAudio backend, so Android's player state and toggle behavior were checked, but the output was not acoustically verified.
- Physical Bluetooth/USB keyboard and gamepad hot-plug/reconnect, simultaneous keys, device-specific keycodes and latency measurements.
- More per-game NES, GBA and PlayStation runtime, BIOS and disc tests on target hardware.
- Network interruption, slow transfer/cancel, low storage and corrupted catalog/cache exercise on target. Security failure checks are useful, but they do not replace all device-level recovery tests.
- Autosave corruption recovery on a device, beyond retaining backup files and exercising successful resumes.
- Target-device launcher frame-time/launch-time/memory measurements, core frame pacing and audible audio behavior.
- Complete the per-console target-device matrix in [CONSOLE_SMOKE_TEST_PLAN.md](CONSOLE_SMOKE_TEST_PLAN.md) before publishing device compatibility claims.
- Track the signed release workflow status for the specific tag; a local signed runtime walkthrough has passed.

## Phase status

Phase 1 delivers signed on-demand core acquisition, the original homebrew catalog and keyboard support. Phase 2 has started with filters, cartridge RAM, autosave generations and input cleanup. It is not a claim that profiles, multiplayer, cloud saves, storage management or every roadmap item is finished. Phase 3 has started with reproducible CI/release automation, validation documentation and submission preparation. Official target-device evidence is still a release/hackathon acceptance gate.

## Reproduce the Android bridge integration checks

These instrumented tests require a selected, owned Android x86_64 developer device. Stage test-only assets with `ANDROID_NDK_HOME=… python3 scripts/prepare_android_tests.py`, build `:app:assembleStandardDebug :app:assembleStandardDebugAndroidTest -PincludeX86=true`, install both APKs on the selected serial, then run `adb -s SERIAL shell am instrument -w -r -e class com.fireemu.ConsoleIntegrationTest com.fireemu.test/androidx.test.runner.AndroidJUnitRunner`.

The test APK exercises the real importer, JNI bridge, Android ImageReader video surface, audio callback, input, reset and snapshots. A fresh local x86_64 instrumented run passed import, video, generated audio, input response, save/restore and reset on Game Boy, Game Boy Color, NES, GBA, Master System, Game Gear and SG-1000. SNES and PS1 diagnostic fixtures returned zero nonzero samples and no visible input change, so those fixture results alone do not establish gameplay audio/input. A separate headless Android direct-core run exercised a real local title for all nine systems; see [v031-android-actual-game-smoke.json](validation/v031-android-actual-game-smoke.json). That run observed nonzero core audio samples for every actual title except the selected Game Gear title, and visible input-driven frame changes in NES, GBA and SNES. Local UI play/save records from earlier runs are in the attached local artifacts bundle. `importLocalDemoGames` is an optional test fixture helper; it skips when no local game files are staged. Test game files are not part of the repository or production APK. Original generated diagnostics are CC0. Additional fixtures are from gilyon/snes-tests v1.4 and JaCzekanski/ps1-tests build-158; retain their upstream terms when sharing test assets.

Settings offers optional low-volume ambient audio and click/focus feedback toggles. UI toggle persistence, Android player start/pause/release state, and click SoundPool activity passed on the local emulator. That emulator's PulseAudio backend did not initialize, so no claim is made about audible output on it; verify sound on the target device. The ambient MP3 is owner-supplied and is not covered by the FireSpot GPL license; see `NOTICE`.
