# FireSpot product and implementation plan

The standard APK uses a native Libretro bridge and downloads signed console cores when needed. The optional bundled Game Boy build remains available for offline evaluation. Builds, lint, artifact integrity, and local Android runtime checks are recorded in [validation](docs/VALIDATION.md); roadmap entries below describe future work.

## Current delivery milestones

- Phase 1: signed on-demand core packages, original homebrew discovery and keyboard support are implemented and exercised on local Android. Public binaries/source: https://github.com/Nirusaki-Malaal/firespot-cores.
- Phase 2 initiated: console/genre filters, cartridge RAM persistence, rotating autosaves and input cleanup. Family profiles, multiplayer, cloud saves, a storage manager and broad console gameplay validation remain future work.
- Phase 3: public source, CI, signed release automation, screenshots, technical docs and a submission guide are in place. The gameplay walkthrough is recorded; target-device acceptance still depends on the selected Fire TV test environment.

Detailed implemented behavior: [documentation](docs/DOCUMENTATION.md). Measured/tested boundaries: [validation](docs/VALIDATION.md). These milestones distinguish shipped functionality from the larger P1/P2 proposals below.

## Product vision

Turn a Fire OS TV into an easy-to-use retro gaming hub: bring a game, let FireSpot prepare its console, and start playing. A person should not need to understand ROM extensions, CPU architectures, emulator cores, or filesystem paths to use the main flow.

The experience should be complete without an account. After games and required console software are installed, local play, saves, controller settings, and library browsing should work offline. Online services enhance the experience but must not sit between a person and an installed game.

This is a proposed roadmap, not a list of implemented features. The release baseline covers core acquisition, game discovery and play; P1 completes the everyday experience; P2 adds differentiators. Dependency gates override priority: compatibility claims must match observed test results.

## Product principles

- **Play is the primary action.** Make Resume or Play prominent; keep advanced emulator settings behind an optional panel.
- **The remote completes every menu task.** A phone and gamepad can improve the experience, but onboarding, settings, recovery, and downloads must remain navigable with the remote.
- **One preparation flow.** Selecting a game checks its file, console software, controller needs, and BIOS requirements in one place.
- **Show the next useful action.** Explain a missing requirement with a button that fixes it, rather than a generic failure message.
- **Keep ownership clear.** Separate games, console software, artwork, saves, and temporary files in storage management.
- **Preserve progress.** Protect saves during crashes, interrupted writes, software updates, imports, and storage cleanup.

## Navigation and screen map

Keep the primary rail small: Home, Library, Discover, Add Games, and Settings. Put Consoles, Favorites, collections, and filters inside Library; show Downloads as a globally reachable status shortcut. P1 profiles appear in the header.

| Screen | Main content | Primary action |
|---|---|---|
| First launch | Welcome, optional controller check, choose a game source | Try a free game / Add my games |
| Home | Continue Playing, recent additions, favorites, one useful setup suggestion | Resume |
| Library | All games with console, genre, player count, and readiness filters | Play selected game |
| Discover | Authorized homebrew catalog, curated collections, download sizes | Get game |
| Add Games | File/USB import, later phone transfer, source management | Choose source |
| Game details | Cover, synopsis, controls, readiness, local save status | Resume / Play / Prepare to play |
| Prepare to play | Game verification, required console software, BIOS and controller checks | Download and prepare / Fix requirement |
| Downloads | Queued, active, failed, and completed jobs with available space | Pause / Cancel / Retry |
| Pause menu | Resume, save/load, controls, display options, exit | Resume |
| Settings | Controllers, storage, display/audio, accessibility, privacy, diagnostics | Contextual action |

Start with three useful Home rows rather than many empty categories. Preserve the selected game and scroll position when returning from gameplay. Show title and console even when artwork is unavailable. Use original FireSpot branding and authorized assets.

## Complete user journeys

### A. First launch to first game

1. Open FireSpot and choose **Try a free game** or **Add my games**. Offer a short optional controller test and a skip action.
2. Show compatible starter games from the authorized catalog. Label remote suitability and show download sizes before starting.
3. On selection, explain any required console software in plain language: “This game needs Game Boy support — download required.” Show the combined space needed and whether system installation approval will be required.
4. Download, verify, install through the validated delivery method, and show preparation progress. Preserve the screen and job state after app restart.
5. Show a short controller card, then **Play**. After the first session, the game appears in Continue Playing.

Success target: a new user reaches playable content without opening advanced settings, creating an account, or typing a filesystem path. Measure completion time; do not advertise a time promise before testing.

### B. Add a personal game collection

1. Choose an available import method. Discover usable storage and file-provider capabilities on the actual runtime; hide unavailable choices with a brief explanation.
2. Preview detected games, unknown files, duplicates, and missing disc companions before confirming.
3. Import or retain granted source access as appropriate. Show artwork fallbacks immediately while optional metadata loads.
4. Group multi-disc games into one title. Keep region/version variants distinguishable; never silently delete a suspected duplicate.
5. Report a concise result: games added, skipped files, and items needing attention. Provide rescan and change-source actions.

For unavailable USB media, mark the games as disconnected instead of deleting entries or saves. Reconnect and rescan should restore playability.

### C. Select a game and prepare it

1. Check file access, supported format, running-process ABI, installed core version, required BIOS, and controller suitability.
2. If ready, launch directly. If preparation is needed, show a single screen with requirements, size, and actionable fixes.
3. Never assume an existing library file is healthy solely because it exists. Report verification/load failures clearly.
4. If no compatible core is available, explain that limitation and keep the game in the library; do not enter a retry loop.

### D. Pause, leave, and return

1. Menu or the supported controller shortcut opens the pause menu, stops game input, and pauses emulation/audio consistently.
2. Offer Resume, Save, Load, Controls, and Exit. Keep destructive actions away from the default focus.
3. On normal exit, atomically save supported progress and update Continue Playing. If the core cannot save state, explain that before offering resume behavior.
4. Resume uses the recorded game/core version and compatible save. If it cannot load, offer the last known good save or Start Game while preserving the failed save for recovery.

### E. Something goes wrong

Use a short description, a specific cause when known, and a next action. Example: “Your USB drive is disconnected. Reconnect it, then choose Check again.” Keep detailed paths and logs in Diagnostics.

## Feature roadmap

### Release baseline

| Feature | User benefit | Completion evidence |
|---|---|---|
| Reproducible APK and one real console | A demonstrably working product | Clean build; permitted game runs in an official Fire TV environment |
| Remote-first navigation | No touchscreen or mouse needed | Entire onboarding-to-play-to-exit flow completed with D-pad/select/back/menu |
| Focused Home and Library | Fast access to games | Empty library, populated library, missing art, and returning focus all work |
| One authorized downloadable game | Immediate first-play experience | Real download, verification, import, and launch |
| On-demand console software | Small base app; add only needed systems | Base APK contains bridge but no emulator cores; verified separate delivery loads on target |
| Unified preparation screen | Fewer confusing setup steps | Missing-core, ready, unsupported, and failed-download states behave correctly |
| Reliable basic controller handling | Predictable play and pause | No stuck buttons after overlays, focus loss, or disconnect; repeat-safe shortcuts |
| Save/load and Continue Playing | Easy return to a session | Save survives app restart and loads with the same core version |
| Download/recovery states | A failure does not end the demo | Network loss, corrupt artifact, cancel, and low-storage paths show actionable recovery |
| Honest diagnostics and submission evidence | Judges can reproduce the result | Install instructions, actual logs, demo video, and product feedback |

Start with NES or GB/GBC, choosing the first core that genuinely builds, loads, and runs. A single proven console is the minimum; additional systems enter the demo only after passing the same checks. If separate core delivery is blocked, document the blocker and label it unfinished rather than displaying a simulated success.

### P1 — complete everyday experience

| Area | Planned features |
|---|---|
| Controllers | Analog sticks/triggers, per-console remaps, dead-zone calibration, live button tester, reconnect support, player assignment, and local multiplayer where the core/game supports it |
| Phone transfer | QR plus typed address/code; temporary paired LAN upload session; progress on both screens; explicit close-session control |
| Library | Favorites, search, recent games, genre/console/player filters, custom collections, metadata correction, multi-disc grouping, and duplicate review |
| Saves | Named manual slots, thumbnails, rotating autosaves, native save RAM persistence, export/import, and last-known-good recovery |
| Per-game preferences | Controller profile, aspect ratio, integer scaling, volume, and compatible core selection remembered for each game |
| Downloads | Persistent queue, bounded concurrency, retry/backoff, pause/resume when supported by server and delivery method, and clear total space estimates |
| Console management | Installed systems, available versions, supported file formats, BIOS help, release notes, rollback, and source/license links |
| Profiles | Local family profiles with separate favorites, history, bindings, and saves; no online signup required |
| Accessibility | High contrast, reduced motion, readable text sizing, non-color status cues, accessible labels, and screen-reader testing where supported |
| Storage | Usage by category, cache cleanup, explicit game/core removal, USB availability indicators, and save-preserving defaults |
| Offline use | Cached library, installed games/cores, local saves and remaps; clear explanations when an online-only action is unavailable |
| Help | Short contextual hints, setup checklist, controller guide, and redacted diagnostic export initiated by the user |

Phone transfer is an optional convenience, never the only route through onboarding. Protect uploads with short-lived pairing credentials, size/rate limits, safe filenames, traversal checks, and expiry. Do not expose an unauthenticated permanent upload server.

### P2 — differentiators after the foundation works

- **Couch Party:** filter installed games by available controllers and player count, assign players, show controls, and launch. Multiplayer support must be declared per game/core.
- **Family mode:** a parent PIN for downloads/settings, curated collections, optional play reminders, and profile limits. Separate verified age-rating data from user-defined labels; do not infer age suitability from cover art.
- **Phone gamepad:** a paired local web controller for suitable low-demand games, with latency/connection indicators and disconnect recovery. Physical gamepads remain the preferred option for precise input.
- **Rewind and quick save:** enable only on compatible cores with measured memory/CPU budgets and opt-in settings on lower-memory devices.
- **Optional cloud save backup:** explicit opt-in, encrypted transport/storage, version history, conflict resolution, deletion controls, and offline-first writes. Never overwrite both conflicting saves automatically.
- **Natural-language library discovery:** optionally search the user's library by phrases such as “two-player games.” Provide text input and only supported voice entry paths; do not promise control of the remote's system microphone button.
- **Achievements and play stats:** start with private local milestones and session history; add third-party services only after verifying their APIs, licenses, and privacy requirements.
- **Advanced display options:** tested shaders, palettes, and latency settings exposed only for compatible cores/devices, with restore-defaults available.
- **More consoles:** add GBA, PS1, PSP, and other systems one at a time with format, BIOS, graphics, audio, controller, licensing, and performance verification. Do not promise universal support.

Avoid netplay, game streaming, public social feeds, and mandatory AI/chat features in the initial release. They add network and service dependencies before improving the core local-play experience. Optional cloud backup could support an AWS mini-challenge later, but is not required for the Fire TV product.

## Interaction details that make the product feel finished

- Every screen has intentional initial focus, visible focus, predictable Back behavior, and restoration after dialogs.
- Do not make long-press gestures the only route to important actions. Offer a visible More/Options action.
- Keep downloads in the background and let the user browse while waiting. Announce completion without stealing focus.
- Display the real actionable state on cards: Ready, Download required, Needs controller, Missing files, or Storage disconnected.
- Do not label software as installed until verification and registration finish. Keep a separate load-validation result.
- Distinguish Resume from Start Game and keep playtime separate from save-state progress; most emulators cannot report a meaningful game-completion percentage.
- Show format/BIOS guidance only when relevant. Help should explain what is needed without sending users to unauthorized downloads.
- Confirm deletion/overwrite with the title and affected data. Default core/game removal to preserving saves, then explain retained storage.
- Offer a controller overlay on first play and later from Pause > Controls. Let users dismiss and reopen it.
- Prefer a static featured game and quick access to recent titles over autoplay video, animated backgrounds, or ranking rows without real supporting data.

## Architecture additions

Extend the existing Kotlin/Compose, Room, DataStore, coroutine, and JNI architecture rather than starting a new frontend. The module names below describe responsibilities; they need not all become separate Gradle modules immediately.

| Responsibility | Main interfaces and stored data |
|---|---|
| Library/import | Game, GameFile, DiscSet, LibrarySource, MetadataRecord; source grants, hashes, user edits, availability |
| Catalog | AuthorizedGameRelease and CoreRelease; signed manifests, compatibility, provenance, licenses |
| Downloads | DownloadJob and ArtifactVerifier; durable state, byte progress, cancellation, bounded extraction |
| Core installation | CoreInstaller, InstalledCore, CoreResolver; delivery strategy, verified version, process ABI, rollback |
| Launch preparation | PrepareGameUseCase returning Ready, NeedsDownload, NeedsBios, NeedsController, MissingFiles, or Unsupported |
| Runtime | EmulationSession and bridge adapter; actual core timing, video/audio lifecycle, load failures, crash reporting |
| Input | DeviceProfile, PlayerAssignment, InputState; independent event sources and analog/player-port JNI support |
| Saves | SaveRecord, SaveRamStore, SaveStateStore; profile/game/core identity, atomic commits, compatibility, recovery |
| Profiles/settings | Profile, per-game overrides, accessibility preferences, optional family controls |

The launcher should request a prepared session instead of constructing a core path. All callers use one CoreResolver. The game process reports session results explicitly; ordinary in-memory singletons do not share state across processes. If both processes access Room, configure and verify the chosen cross-process synchronization approach.

Use content hashes for game identity while preserving separate variants and disc relationships. Namespace saves by profile and game identity; attach exact core version and state format information to state files. Keep native save RAM and emulator snapshots separate because their compatibility properties differ.

Prefer a minimal hosted static catalog and versioned artifacts initially. A dynamic backend is unnecessary for local gameplay. Signing/build credentials stay outside the APK and repository. Do not add accounts, analytics, or cloud infrastructure merely to populate a technology list.

## Failure and recovery contract

| Situation | Expected experience |
|---|---|
| No network | Installed library remains playable; new downloads explain connectivity and offer retry |
| Insufficient storage | Show required versus available space before download; link to cleanup without deleting saves |
| Interrupted/corrupt download | Keep incomplete data outside the library; validate resumed bytes and retry safely |
| Wrong ABI or unsupported core | Prevent launch; explain compatibility; never report successful installation |
| Missing BIOS | Explain the exact required file and supported import route; allow return to Library |
| Controller disconnect | Pause, clear held inputs, and offer reconnect/reassignment; remote can still exit |
| Core crash | Return to a recoverable launcher state with diagnostic details and the last committed save |
| Core update breaks a state | Preserve state/version metadata; offer rollback or a new session without destroying progress |
| Storage removed | Mark affected titles unavailable; preserve library metadata and saves |
| Artwork/catalog service fails | Keep title placeholders and cached data; never block a local launch |
| App process killed | Recover durable download/install records; discard unverified staged files; preserve committed saves |

## Quality targets and release gates

These are acceptance targets, not measured results. Test on the selected official Fire TV virtual device and report its exact OS, process ABI, memory constraints, and test limitations. Virtual testing does not establish physical Bluetooth behavior or real-stick performance.

- **Usability:** every release-baseline path can be completed without touch, a mouse, or typed paths; errors and dialogs have a clear exit.
- **Integrity:** tampered manifest/artifact, wrong architecture, unsupported version, and unsafe archive cases are rejected before loading.
- **Input:** test simultaneous buttons, key/hat overlap, key repeat, pause/resume, focus loss, and disconnect; test analog/multiplayer once implemented.
- **Progress:** native save RAM and supported state files survive restart; interrupted writes retain a known good version; separate profiles never overwrite each other.
- **Lifecycle:** test background/resume, repeated launches, surface recreation, game-process failure, and external-storage removal.
- **Performance:** record launcher frame times, launch time, peak memory, core frame pacing, and audio underruns. Establish measured budgets before adding effects.
- **Storage:** verify cleanup preserves saves by default and insufficient-space handling includes staging/extraction/rollback overhead.
- **Reproducibility:** a fresh checkout can build the app and obtain pinned, correctly licensed core artifacts using documented steps.

Use unit tests for state merging, catalog verification, compatibility checks, and save/download transitions; integration tests for installer-to-resolver-to-runtime behavior; manual remote-only walkthroughs for focus and recovery. A UI screenshot or a file-exists assertion is insufficient evidence of emulation.

## Milestones and scope gates

| Milestone | Deliverable | Gate to continue |
|---|---|---|
| M0: Runtime proof | Buildable APK, official test session, one genuine core and permitted game | Video/audio/input work; architecture is correct |
| M1: Separate core delivery | Signed release catalog, verified install path, shared resolver | Base APK excludes cores; separately acquired core genuinely loads |
| M2: First-play experience | Onboarding, catalog/import, preparation, Home/Library | Remote-only first-play walkthrough passes |
| M3: Reliable sessions | Pause, controls, save/load, history, error recovery | Restart/resume and failure scenarios pass |
| M4: Submission | Reproducible instructions, short video, feedback/friction log | Claims match demonstrated functionality |
| M5: Everyday release | P1 controller, library, profile, storage, and transfer features | Usability and data-preservation gates pass |
| M6: Differentiators | Selected P2 features and more validated consoles | Added features meet device budgets and do not break basic local play |

For the imminent deadline, stop feature expansion once M0–M3 are demonstrable and reserve time for M4. If a milestone is blocked, communicate the limitation and reduce scope explicitly. Do not replace native runtime evidence with mock UI behavior.

## Suggested demo narrative, under three minutes

1. **0:00–0:20:** State the problem and show FireSpot inside the official Fire TV environment.
2. **0:20–0:55:** Navigate with the remote, choose a permitted game, and show its preparation requirements.
3. **0:55–1:25:** Show genuine game/core acquisition and readiness. If a wait is edited, label the cut.
4. **1:25–2:05:** Play, open controls/pause, and save.
5. **2:05–2:35:** Exit and resume from Continue Playing; demonstrate one useful recovery path if time permits.
6. **2:35–2:55:** Explain what was built during the hackathon, current supported scope, and product feedback.

Success means a judge can reproduce the central promise: add a game, prepare its console, play with TV controls, and return to preserved progress.

Reviewed upstream https://github.com/shubhfront/firespot at e3858c819b4c2b4f3cb810acf5cc6179530efdfe on October 22, 2026.
The current source checkout and validation record document the implementation and test evidence.

## Demo target and deadline

Target the existing Kotlin/Android Fire OS app. Vega OS requires a separate package and runtime integration; do not assume an APK or Android native library works there.

The Amazon Developer Hackathon accepts a demo on an actual Fire TV or Fire TV/Vega simulator. Its listed deadline is October 23, 2026, noon PDT, which is October 24, 2026, 00:30 IST. Produce an English demo under three minutes, source and run instructions, and product feedback. Explain what changed during the hackathon because this project already exists.

Without hardware, use Amazon Appstore Quality Central > Live Device Interaction > Virtual Devices to upload and run the APK, use the virtual remote, record video, and inspect logs. Check account access and available sessions first. An ordinary Android TV AVD is useful for local development, but do not assume it satisfies the competition's Fire TV requirement. The repository's custom Python/browser simulator is a UI prototype, not proof of APK execution on Fire TV.

## Findings from source and artifact inspection

- There are two launchers: the Kotlin Compose application in app/ and a Java launcher in emulator/_shared/. Choose the Kotlin app as the product and connect its build to the native bridge explicitly.
- CoreRepositoryImpl.getCorePath and EmulatorActivity.surfaceCreated independently resolve cores from applicationInfo.nativeLibraryDir. Both must use one installed-core resolver.
- InputHandler supports keys and joystick hats, but not analog sticks or analog triggers. One bitmask combines all devices; hat events clear directional bits set by keys. Menu key repeats can toggle the overlay repeatedly. Opening the overlay can swallow release events and leave buttons held.
- The checked-in armeabi-v7a libraries for FCEUmm, mGBA, and PCSX ReARMed identify as x86-64 ELF files. They cannot load in an ARM process. Rebuild with the Android NDK and reject architecture mismatches in CI.
- RomScanner scans local paths; there is no implemented download flow. Its folder-based fallback can classify unsupported files as games. ZIP recognition does not establish that the selected core can load the archive.
- The app manifest lacks INTERNET permission. A download implementation needs it.
- The README's performance figures are not established by this review. Use measured results from the actual runtime, not prototype verification messages.

## 1. Controller and remote integration

Separate UI navigation from gameplay input. Keep D-pad/select/back usable throughout onboarding, downloads, library, settings, and pause menus. Restore focus to the originating card when returning from a game or details screen.

Represent gameplay input per device and player port, merging independent key, hat, stick, and trigger state. Use device motion ranges for dead zones. Extend the JNI interface for analog values and multiple player ports, rather than converting every input into one joypad mask.

Persist remaps by device descriptor and console, with reconnection fallback. Clear held input on pause, focus loss, overlay entry, and disconnect. Handle menu shortcuts once per press. Test controller reconnection and Start+Select without leaking held input into the game.

Remote-only gameplay should be offered for compatible games with explicit mappings; show a gamepad recommendation for games requiring more buttons or analog control. The remote cannot provide every console control. Home and microphone are system-controlled buttons, not arbitrary game actions.

## 2. TV interface and performance

Keep Kotlin and Compose for TV. Prioritize Continue Playing, Library, Consoles, Add Games, and Settings. Show a persistent, obvious focus state and readable text at TV distance. Avoid a moving hero while the user is navigating.

Use lazy rows/grids, bounded thumbnail caching, background scanning and downloads, and stable item keys. Show truthful empty, downloading, missing-core, unsupported-device, and retry states. Persist library metadata with Room; explicitly account for separate processes when updating play history.

Do not claim frame rate or latency targets as results. Measure frame pacing, audio underruns, memory, and input behavior. Read the core's timing information instead of assuming every game runs at exactly 60 Hz.

## 3. Easy game acquisition

Offer two routes: a curated catalog of homebrew/public-domain/otherwise authorized games with documented redistribution permission, and user imports through a supported file picker/USB flow. A temporary, token-protected LAN phone upload can follow after the core demo works.

Catalog entries should carry title, system, HTTPS URL, size limit, SHA-256, attribution, and license/permission evidence. Download to temporary storage, verify, then atomically add the completed file to the library. Handle cancellation, low storage, and retry. Treat ROMs as data, separately from executable emulator cores.

Bound archive expansion, reject path traversal, and preserve related disc files such as CUE/BIN sets. Do not treat every file in a console folder as a valid ROM. Never add commercial games or proprietary BIOS files to the downloadable catalog without permission; inspect the repository's sample-file provenance before using them in the demo.

## 4. Downloadable emulator cores

Keep only the native frontend bridge in the base APK. Add a catalog and installer abstraction for separately delivered cores.

Each release needs an ID, version, supported console, Android ABI, minimum API, bridge compatibility, artifact size, HTTPS URL, SHA-256, signature, and license/source information. Trust a signed manifest rooted in a public key shipped with the app; a checksum supplied by an untrusted manifest is insufficient.

Use installation states: absent -> downloading -> verifying -> installing -> ready, plus failed and update available. Resolve using the running process ABI, not only the CPU or the first advertised device ABI. Never advertise an unsupported core as ready.

Prototype native loading on the chosen Fire OS virtual device before committing to raw downloaded .so delivery. Android execution/linker constraints and Amazon distribution rules require validation; do not assume chmod or a target-SDK downgrade is an acceptable fix. A separately installed, signed core-provider APK is an alternative to evaluate when raw library loading is unsuitable; confirm package visibility, library access, installation UX, signing, and store policy. Installation requires system/user approval and must not be presented as silent.

Download to staging, verify signature/hash/ELF architecture, then atomically install. Retain the previous version for rollback. Do not overwrite a loaded core. Record the core version with save states and warn about incompatible upgrades. Downloading a GPL core separately does not remove its redistribution or source obligations; review each core's license.

## Delivery order

1. Establish a reproducible Kotlin APK build and an official Fire TV virtual-device session.
2. Build one genuine NES or GB core for the actual process ABI and demonstrate a permitted game running with audio and remote input.
3. Prove separate core delivery and loading on that same runtime before expanding the catalog.
4. Add controller-state fixes, a minimal focused library, and one verified homebrew download.
5. Record install -> obtain game -> obtain core -> play -> pause/save -> resume. Capture real logs and friction notes. Treat additional consoles, analog controls, and multiplayer as follow-up scope unless verified.

## References

- Hackathon: https://amazonappdev2026.devpost.com/
- Amazon virtual devices: https://community.amazondeveloper.com/t/live-device-interaction-with-virtual-devices-is-now-available/28664
- Fire TV device/OS identification: https://developer.amazon.com/docs/device-specs/identify-fire-tv-devices.html
- Remote input: https://developer.amazon.com/docs/fire-tv/remote-input.html
- Android dynamic code loading: https://developer.android.com/privacy-and-security/risks/dynamic-code-loading
- Android 10 execution changes: https://developer.android.com/about/versions/10/behavior-changes-10
- Amazon content policy: https://developer.amazon.com/docs/policy-center/understanding-content-policy.html
