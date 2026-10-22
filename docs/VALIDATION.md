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
- Physical Bluetooth/USB keyboard and gamepad hot-plug/reconnect, simultaneous keys, device-specific keycodes and latency measurements.
- Per-game native NES, GBA and PlayStation runtime/BIOS/disc tests. Those packages remain experimental.
- Network interruption, slow transfer/cancel, low storage and corrupted catalog/cache exercise on target. Security failure checks are useful, but they do not replace all device-level recovery tests.
- Autosave corruption recovery on a device, beyond retaining backup files and exercising successful resumes.
- Target-device launcher frame-time/launch-time/memory measurements, core frame pacing and audible audio behavior.
- Track the signed release workflow status for the specific tag; a local signed runtime walkthrough has passed.

## Phase status

Phase 1 delivers signed on-demand core acquisition, the original homebrew catalog and keyboard support. Phase 2 has started with filters, cartridge RAM, autosave generations and input cleanup. It is not a claim that profiles, multiplayer, cloud saves, storage management or every roadmap item is finished. Phase 3 has started with reproducible CI/release automation, validation documentation and submission preparation. Official target-device evidence is still a release/hackathon acceptance gate.
