# FireSpot 0.3.0

- Console-first native discovery, catalog keyboard search, installed-only view, console filters and alphabetical/size/year sorting.
- Actual catalog/installed counts, title metadata, and an import action for empty console selections.
- SNES via bsnes 2014 Performance and Master System/Game Gear/SG-1000 via Gearsystem, all marked experimental.
- Six cores across ARMv7/ARM64/x86_64: 18 public binaries, matching source/licenses and signed revision-2 catalog.
- New cartridge formats (.sfc/.smc/.sms/.gg/.sg), X/Y/L/R remapping and keyboard-back handling without a focused control.
- ROMsGames supplied a browsing reference; its ROM files, artwork, ratings and counts are not mirrored. In-app game acquisition uses the original CC0 catalog.

Both new cores pass native Android initialization. Per-game compatibility and official Fire TV testing remain pending. Existing phase1-v1 downloads and the 0.2.0 release remain available.

# FireSpot 0.2.0

A monochrome retro gaming hub with on-demand console support and original homebrew discovery.

- Standard APK downloads a signed, CPU-specific Game Boy core before play; demo keeps a self-contained offline option.
- Public catalog release supplies Gambatte, FCEUmm, mGBA and PCSX ReARMed across ARMv7, ARM64 and x86_64, with matching source/licenses. Additional cores are experimental.
- Discover original games without transferring files; Signal Runner demonstrates battery-backed progress.
- Keyboard gameplay, typed search, Escape navigation, persistent controller remapping, stick-to-D-pad input and held-input cleanup.
- Library filters/sorting, favorites, history and GB/GBC/NES/GBA cartridge import where a file provider exists.
- Manual save/load, resume, rotating autosaves and cartridge RAM persistence.
- Signed release workflow, build/lint/security/host checks, source archives and checksums.
- GPL-3.0-or-later for FireSpot changes, preserving original and third-party notices.

Local Android evidence is not official Fire TV certification. Physical Bluetooth behavior, target-device performance, NES/GBA/PS1 game compatibility and PS1 disc import still need validation. No commercial ROMs or proprietary BIOS are included. The standard APK is the core-free distribution; download the demo for an immediate offline starter experience.
