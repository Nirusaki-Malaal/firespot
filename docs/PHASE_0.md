# Phase 0 demo: current setup and acceptance

## What to install

**Recommended: `artifacts/firespot-phase0-demo.apk`.** It includes the native Game Boy core and two original games. The user explicitly approved this self-contained demo so a virtual-device ROM/core upload is unnecessary. The standard APK remains core-free.

The included games are Orbit Collector and Pocket Courier, original CC0 Game Boy checkpoint games. D-pad moves to the diamond checkpoint; reaching it increments the score. Select/A or Play/Pause/B produces a tone. No commercial ROM or proprietary BIOS is included.

Application IDs:

- Demo: `com.fireemu.demo`
- Standard: `com.fireemu`

## Amazon virtual-device test

1. Sign in at https://developer.amazon.com/.
2. Open Tools & Services → Appstore Quality Central → Live Device Interaction — Virtual Devices.
3. Start an available Fire OS session and upload **only the demo APK**.
4. Launch FireSpot. Select **Open my library** on the welcome screen.
5. Open Library, select a game, then choose **Play from start**.
6. Use Menu to pause, save/load, change scaling, view controls, or save and exit.

No AWS credentials or ADB tunnel are needed solely for the app's own offline gameplay. The remote service may require its own connection/authentication steps; follow its generated instructions. Local AWS CLI, Session Manager and ADB prerequisites are recorded in [AWS_TESTING_SETUP.md](AWS_TESTING_SETUP.md).

A local ADB-connected device can install/launch the demo directly:

```sh
adb install -r artifacts/firespot-phase0-demo.apk
adb shell am start -n com.fireemu.demo/com.fireemu.MainActivity
```

The older `install_runtime_probe.sh` helper is for the **standard debug** build and its separately delivered core; the self-contained demo does not need it.

## User-facing acceptance

- Welcome completes using the remote and the included games appear once.
- Home and Library open. Game Boy collection and Favorites contain the correct games.
- Search keys, Space, Delete and Clear change actual results.
- Favorite/unfavorite survives navigating away and restarting the app.
- Both games load without an external ROM/core. Move onto a checkpoint and verify points increase.
- Menu opens once per press; Resume returns without a stuck direction.
- Controls guide opens and closes.
- Manual slot save/load restores the emulated position; slots are independent.
- Save & Exit produces a Resume action; Play from start resets instead of loading autosave.
- Reset restarts the game.
- Original aspect, integer scale and fill screen visibly change the viewport and persist.
- Frame counter can be enabled and disabled.
- Remapping captures a button and applies to gameplay; reset restores defaults. Menu, Back and Home stay reserved.
- Import accepts a complete .gb/.gbc through a file provider, reports invalid input, and adds a title without overwriting existing progress. A virtual device without a file provider reports that limitation; the included games remain available.

## Runtime controls

| Input | Gameplay action |
|---|---|
| D-pad | Move |
| Select / gamepad A | A |
| Play/Pause / gamepad B | B |
| Fast Forward / gamepad Start | Start |
| Rewind / gamepad Select | Select |
| Menu or Back | Pause menu |

## Reproducible checks

```sh
./scripts/build_gameboy.sh
./gradlew :app:assembleDemoDebug :app:assembleStandardDebug :app:lintDemoDebug :app:lintStandardDebug
python3 scripts/verify_artifacts.py --apk app/build/outputs/apk/demo/debug/app-demo-debug.apk --demo
python3 scripts/verify_artifacts.py --apk app/build/outputs/apk/standard/debug/app-standard-debug.apk
python3 -m unittest discover -s tests -p 'test_*.py'
```

For Linux host emulation, build the pinned core with `make -f Makefile.libretro platform=unix`. Run `tests/core_smoke.py CORE ROM 1` for Orbit Collector and `CORE ROM 2` for Pocket Courier. These checks test movement, checkpoint scoring, nonzero audio, and deterministic state restoration.

For a developer-controlled Android test device with the demo installed:

```sh
python3 tests/android_demo_smoke.py --serial YOUR_ADB_SERIAL
```

Additional local AOSP provider/remapping checks are in `tests/android_demo_extras.py --serial YOUR_ADB_SERIAL`.

That script exercises only the demo application and writes screenshots/logs to `artifacts/evidence/monochrome/`. It is not an official Fire TV test or a Bluetooth latency benchmark.

## Verification scope

Both APK variants compile and pass Android lint with zero errors; existing warnings remain. Native host checks pass for both games, including checkpoint scoring. Android runtime evidence and the latest scripted results are in `artifacts/evidence/monochrome/`. Sixteen local Android checks passed, including both games, real key capture/reset, and importing then playing a cartridge through DocumentsUI. Provider navigation may differ on Fire TV; the included games need no provider.

**Official Fire TV runtime acceptance and physical Bluetooth behavior remain pending.** No universal console compatibility or guaranteed frame rate is claimed. The normal launcher exposes only the implemented Game Boy feature set, rather than unfinished roadmap controls.

Original foundation-only results are archived in [PHASE_0_INITIAL.md](PHASE_0_INITIAL.md).
