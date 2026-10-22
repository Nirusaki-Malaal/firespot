# Console smoke-test plan

Use this plan on a clean standard install and again on the Fire TV/virtual device used for acceptance. It is a repeatable smoke test, not a substitute for broad game compatibility testing. Use games and BIOS files that the tester is legally entitled to use. Record the device model, Fire OS/Android version, app build, core version, game title and file hash with each run.

## Per-console run

Run these steps independently for each listed system, including after a fresh core install:

1. **Install and recover:** install/verify the core from Console support; confirm progress and completion; leave and reopen the screen; verify the core remains installed. Try once offline after installation.
2. **Get a game:** use Import a game with a supported local file, then use Browse game library and download a compatible, redistributable catalog title. Verify the detected console, title, artwork and library entry. Cancel one transfer and confirm the UI returns to an idle state.
3. **Launch:** cold-launch each test title three times. Confirm the game renders within 10 seconds, stays responsive, and reports the expected console. Run one title from the online library and one local import where available.
4. **Controls:** verify D-pad in four directions, every face/shoulder button the system uses, Start/Select, controller input, and keyboard input. Hold a direction, press two buttons together, release all controls, pause and resume. Confirm no stuck input and that Back/Escape opens the in-game menu and exits as labeled.
5. **Audio/video:** play for at least five minutes; confirm moving gameplay, stable frame pacing, correct aspect/scaling, and audible game sound on physical speakers/headphones. Toggle ambient audio and interface sounds separately; confirm ambient audio pauses in gameplay and resumes in the menus.
6. **Persistence:** create a native save where supported, create a FireSpot save state, exit, force-stop the app, relaunch, and load/resume. Verify the game state and save data; test reset and return to library.
7. **Soak and failure path:** play continuously for 15 minutes, watch for crash, black/frozen frames, audio dropouts, thermal slowdown and memory growth. Repeat a launch after disconnecting/reconnecting the controller. Check behavior for a corrupt/unsupported file and a low-storage or interrupted-download case when the target test environment permits.
8. **Record evidence:** save a short screen capture, logcat/crash report, and a completed row in the results table below. Mark a check only when observed on that device; a host bridge test or diagnostic fixture alone does not count as gameplay acceptance.

## System-specific coverage

| System | Minimum real-game coverage | Extra check |
| --- | --- | --- |
| Game Boy | One local import and one library title | Four-direction movement, A/B, Start/Select, battery save and state resume |
| Game Boy Color | One local import and one library title | Color rendering, A/B, Start/Select, save and resume |
| NES | Two titles from different genres | D-pad, A/B, Start/Select, sustained audio and save-state resume |
| Game Boy Advance | One local title and one library title | FireRed or another tester-provided title, L/R, Start/Select, battery save and resume |
| SNES | Two titles, including one with active music/effects | D-pad, face buttons, L/R, Start/Select; verify real-game audio and input response |
| Master System | One local import and one library title | D-pad, 1/2, Start, sound and save-state resume |
| Game Gear | One local import and one library title | Handheld aspect/scaling, D-pad, 1/2, sound and save-state resume |
| SG-1000 | One compatible title | Verify the mapped controller buttons and sustained gameplay/audio |
| PlayStation | Two compatible discs/titles with required BIOS/configuration | Verify BIOS and disc selection, analog and digital input where supported, CD audio, save card, reset, state resume and a 15-minute session |

For consoles without a confirmed target-device result, record failures and limitations accurately. Unsupported BIOS, disc formats, protected downloads, or missing game files are setup blockers, not passing tests.

## Result sheet

Duplicate this table for each device/build and fill it during testing. Use **Pass**, **Fail**, or **Blocked** plus a concise evidence path; leave no blank check implied as a pass.

| System | Core install/offline | Import | Library download | Launch x3 | Controls | Game audio/video | Save/restore | 15-min soak | Result/evidence |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Game Boy | | | | | | | | | |
| Game Boy Color | | | | | | | | | |
| NES | | | | | | | | | |
| Game Boy Advance | | | | | | | | | |
| SNES | | | | | | | | | |
| Master System | | | | | | | | | |
| Game Gear | | | | | | | | | |
| SG-1000 | | | | | | | | | |
| PlayStation | | | | | | | | | |

## Exit criteria

A system can be described as verified on the intended Fire TV/virtual target only after all per-console steps pass, two real titles pass its system-specific checks, no repeatable crash or data-loss issue remains, and the evidence is attached to the release validation record. Host tests and Android emulator results are useful earlier gates but cannot meet the Fire TV acceptance criterion by themselves.
