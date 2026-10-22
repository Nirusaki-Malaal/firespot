# Console support and expansion limits

There is no fixed maximum console count in the Libretro architecture. A system needs a compatible core, a supported CPU/API, appropriate rendering/input interfaces, a working content/BIOS flow, and measured performance on the chosen Fire TV. Being present in an enum or compiling a .so is not a gameplay compatibility test.

## Published now

The [phase1-v2 release](https://github.com/Nirusaki-Malaal/firespot-cores/releases/tag/phase1-v2) contains six real cores and 18 Android binaries: ARMv7, ARM64 and x86_64 for each.

| System | Core | FireSpot status |
|---|---|---|
| Game Boy | Gambatte | Original and diagnostic gameplay exercised on local Android; Fire TV acceptance pending |
| Game Boy Color | Gambatte | Diagnostic gameplay exercised on local Android; more color-title coverage pending |
| NES / Famicom | FCEUmm | Actual-title gameplay, input, audio, save/restore and reset exercised on local Android; Fire TV acceptance pending |
| Game Boy Advance | mGBA | User-provided FireRed gameplay, controls, saves and resume exercised on local Android; Fire TV acceptance pending |
| SNES / Super Famicom | bsnes 2014 Performance | Actual-title video/audio/input, save/restore and reset exercised on local Android; target-device checks pending |
| Master System | Gearsystem | Actual-title video/audio, save/restore and reset exercised on local Android; broader controls and target-device checks pending |
| Game Gear | Gearsystem | Video and save/restore exercised on local Android; audible output and target-device checks pending |
| SG-1000 | Gearsystem | Actual-title video/audio and save/restore exercised on local Android; broader controls and target-device checks pending |
| PlayStation | PCSX ReARMed | Actual-title video/audio and save/restore exercised on local Android; input response, BIOS/disc workflow and target-device checks pending |

Gambatte is included in the optional bundled Game Boy build. Standard downloads console support. The public release includes each core's exact source archive, commit and license. No BIOS or commercial ROMs are supplied.

## Expansion candidates

These are integration candidates based on the Libretro ecosystem, not features already available in FireSpot. Expected suitability is engineering judgment, not a Fire TV benchmark.

| Group | Candidate systems | Main remaining work |
|---|---|---|
| Additional 8/16-bit consoles | Mega Drive/Genesis | Select/pin/build/license-review cores, import formats, controls and real game tests |
| Older cartridge consoles | Atari 2600/5200/7800, ColecoVision, Intellivision, Vectrex | Core integration, unusual controller layouts, optional firmware and device tests |
| Handhelds | Atari Lynx, Neo Geo Pocket/Color, WonderSwan/Color, Pokémon Mini | Core integration, orientation and controls |
| CD and add-ons | PC Engine/TurboGrafx-16/CD, Sega CD, Sega 32X | Content sidecars/playlists, firmware where required, storage and save behavior |
| Arcade | Neo Geo, CPS1/CPS2 and other core-supported arcade boards | Correct core-specific ROM sets, BIOS where required, controls and license review |
| More demanding 3D | N64, PSP, Dreamcast, Saturn | Rendering integration, CPU/GPU budgets, per-game validation and gamepad controls |
| Computers and fantasy consoles | DOS, MSX, Commodore platforms, supported open fantasy consoles | Keyboard/mouse/device APIs, disk management and individual core support |

Twenty or more classic system families are plausible future targets, but the current app does not support that list yet. Expanding to many untested binaries would make the catalog larger without making the experience work.

PSP has no published FireSpot binary yet. The active bridge accepts software video frames and does not implement the Libretro hardware-rendering context. PPSSPP, N64 and Dreamcast need more than a download URL. GameCube/Wii, PS2, Switch, PS3, Xbox and newer platforms are outside the current delivery scope; neither the app nor this document promises their viability on a Fire TV Stick. Some upstream cores also lack Android/ARM builds even when desktop versions exist.

## Acceptance gate for a new system

1. Pin a genuine upstream revision and read its license/component notices.
2. Build Android ARMv7/ARM64; add x86_64 where practical for local tests.
3. Verify ELF CPU, required entry points, API level and dependency availability.
4. Exercise video, audio, input, reset, lifecycle and saves using permitted content.
5. Validate firmware and multi-file import requirements without distributing proprietary firmware.
6. Measure the intended Fire TV and publish the observed compatibility evidence in the catalog and docs.
7. Publish matching source/licenses and signed artifact metadata before enabling download.

## Primary references

- [Libretro core/system list](https://docs.libretro.com/guides/core-list/): available emulator/system mappings, not a FireSpot support claim.
- [Libretro hardware rendering](https://docs.libretro.com/development/cores/opengl-cores/): frontend-owned graphics contexts and rendering requirements.
- [Libretro core licenses](https://docs.libretro.com/development/licenses/): licenses differ; some cores have noncommercial conditions.
- [Genesis Plus GX](https://docs.libretro.com/library/genesis_plus_gx/): supported Sega systems; 32X needs another core.
- [PPSSPP](https://docs.libretro.com/library/ppsspp/): backend and assets/options considerations.
