# FireSpot 0.3.0

- Console-first native discovery, catalog keyboard search, installed-only view, console filters and alphabetical/size/year sorting.
- Actual catalog/installed counts, title metadata, and an import action for empty console selections.
- SNES via bsnes 2014 Performance and Master System/Game Gear/SG-1000 via Gearsystem, with actual-title Android runtime evidence in the validation records.
- Six cores across ARMv7/ARM64/x86_64: 18 public binaries, matching source/licenses and signed revision-2 catalog.
- New cartridge formats (.sfc/.smc/.sms/.gg/.sg), X/Y/L/R remapping and keyboard-back handling without a focused control.
- ROMsGames supplied a browsing reference; its ROM files, artwork, ratings and counts are not mirrored. In-app game acquisition uses the original CC0 catalog.

Both new cores pass native Android initialization and have local actual-title test evidence. Broader per-game compatibility and official Fire TV testing remain pending. Existing phase1-v1 downloads and the 0.2.0 release remain available.

# FireSpot 0.2.0

A monochrome retro gaming hub with on-demand console support and original homebrew discovery.

- Standard APK downloads a signed, CPU-specific Game Boy core before play; the bundled Game Boy build provides an offline option.
- Public catalog release supplies Gambatte, FCEUmm, mGBA and PCSX ReARMed across ARMv7, ARM64 and x86_64, with matching source/licenses. SNES and Sega 8-bit cores are also available on demand.
- Discover original games without transferring files; Signal Runner demonstrates battery-backed progress.
- Keyboard gameplay, typed search, Escape navigation, persistent controller remapping, stick-to-D-pad input and held-input cleanup.
- Library filters/sorting, favorites, history and GB/GBC/NES/GBA cartridge import where a file provider exists.
- Manual save/load, resume, rotating autosaves and cartridge RAM persistence.
- Signed release workflow, build/lint/security/host checks, source archives and checksums.
- GPL-3.0-or-later for FireSpot changes, preserving original and third-party notices.

Local Android evidence is not official Fire TV certification. Physical Bluetooth behavior, target-device performance, additional title compatibility and PS1 firmware/disc workflows still need validation. No commercial ROMs or proprietary BIOS are included. The standard APK is the core-free distribution; the bundled Game Boy build provides an offline starter experience.

### Additional setup features

- Settings includes a live controller/keyboard tester with hotplug device inventory, held buttons, key transitions and raw joystick readings.
- Storage and playtime dashboard reports managed folder usage, free space, favorites and most-played titles, with safe image-cache cleanup.
- Live capture stops on Back/Escape, app pause and screen disposal.

### Library integration and performance

- ROMsGames opens in an on-demand browser with native navigation, cancellable HTTPS downloads and import confirmation.
- Unified document/browser import accepts single-game ZIPs and single-file PS1 ISO/PBP/EXE alongside supported cartridges, with bounded staging and deduplication.
- Navigation-owned ViewModels and reusable rail destinations prevent accumulating screen collectors.
- Artwork memory cache capped at 16 MiB/8% heap; fixed decode bounds and hidden-UI trimming.
- Software video reuses scaling maps and repeated output rows; all primary native sections share the restrained monochrome finish.
