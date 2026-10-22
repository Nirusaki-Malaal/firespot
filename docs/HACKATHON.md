# Hackathon preparation

## Project description

FireSpot turns a Fire OS television into a unified retro gaming launcher. People can open an original starter game immediately, discover another original homebrew title without transferring a file, and let the app obtain the console support needed to play. Its monochrome interface is navigable with a remote and also accepts gamepad or keyboard input. Saves, resume, favorites, history and display controls live in one interface.

The app is Kotlin/Compose with a C/JNI Libretro frontend. A public, versioned GitHub release supplies CPU-specific native packages and corresponding source/license files. FireSpot verifies a signed catalog, artifact hash, size and native CPU before installation. The standard APK excludes emulator cores; an optional demo APK packages Game Boy support for a self-contained presentation.

The practical first experience uses small original CC0 games. It avoids presenting a commercial ROM downloader or pretending that a long list of compiled systems has already passed gameplay tests.

## Track and mini-challenge status

Primary intended track: Fire TV. The supplied rules require the demo to show a real Fire TV device or the official Fire TV/Vega simulator. Our generic Android emulator captures do not satisfy that requirement on their own. The Android APK targets Fire OS; a Vega presentation needs a separate compatible implementation/runtime.

Open Source: the standalone public application repository and public core-release repository provide contribution URLs, source, licenses and implementation history. Verify the event window and include the GitHub username, repository and contribution description in the submission. Merely making an old repository public is not a description of what changed during the event.

AWS Builder: installing AWS CLI/Session Manager for remote-device access does not establish an AWS application integration. Do not claim Bedrock, AgentCore, cloud saves or another AWS service unless it is actually called and demonstrated. No Alexa+, Ring or Bee integration is currently claimed.

## Three-minute demo script

| Time | Show | Explain |
|---|---|---|
| 0:00–0:20 | FireSpot on the official target; use only the remote | One library, familiar controls, no account |
| 0:20–0:55 | Standard build → Library → Play; console support prepares | Core-free APK; signed on-demand package |
| 0:55–1:25 | Discover → Signal Runner → Get → Play | An original game without file upload or manual ROM transfer |
| 1:25–1:55 | Move, pause, save/exit, resume | Real emulation and preserved progress |
| 1:55–2:20 | Search with keyboard; show remapping and scaling | Multiple input methods in one interface |
| 2:20–2:45 | Restart/resume; briefly show trust/source release | Installed play works locally; downloads are verified |
| 2:45–3:00 | Clear project/contribution links and concise feedback | What was built, what was learned, and the next device-validated milestone |

Keep the video under three minutes, in English and publicly accessible as required by the supplied rules. Use original art and audio. Do not use emulator footage to imply another system has passed compatibility testing. Rehearse download timing on the actual presentation network and keep the self-contained demo available as a clearly labeled fallback.

## Product feedback worksheet

Fill this from actual experience, not assumed behavior:

| Tool | Actual use | What to record |
|---|---|---|
| Fire TV virtual-device interface | Target testing once connected | Onboarding, APK upload, input, connection setup and session expiry |
| AWS CLI + Session Manager plugin | Installed prerequisites for remote-device access | Exact connection steps attempted and any generated-script problem |
| Android SDK/ADB/NDK | Real APK/native builds and local install/debug | Setup, architecture selection, logs and build reproducibility |
| GitHub Releases/Actions | Core hosting, public source, checks and signed APK release | Download paths, artifact handling, setup and workflow failures/fixes |
| Kotlin/Compose | TV launcher implementation | Focus behavior, keyboard handling and rendering choices |

The recorded onboarding friction includes difficulty obtaining Amazon's temporary remote-connect script and transferring ROMs into a virtual device. The self-contained demo and in-app original-game catalog directly address the file-transfer problem. Target-session feedback still needs a real successful session to assess end-to-end behavior.

For each friction log entry include task, steps, expected result, observed result, severity, workaround and an actionable suggestion. Include the actual timestamp/device/session details privately when troubleshooting, and redact credentials before submission.

## Final checklist

- [ ] Official target install/play/audio/input/save/resume video captured.
- [ ] Selected release workflow green; signed APK installed and tested.
- [ ] Public source, notices, build instructions, core source/licenses and contribution URL included.
- [ ] Description distinguishes verified Game Boy gameplay from experimental additional cores.
- [ ] English demo video under three minutes published.
- [ ] Tool feedback and friction log based on actual use.
- [ ] Changes made during the hackathon window described.
- [ ] Correct primary track and only genuinely implemented mini challenges selected.
