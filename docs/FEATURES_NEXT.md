# Feature priorities after 0.2.0

This is proposed work, not a claim that these features are available. Finish target-device acceptance before widening the demo's promises. The existing app already has discovery/downloads, search, favorites, history, filters, keyboard input, remapping, four save slots, autosave generations, cartridge RAM and display scaling.

## Highest user value

| Priority | Feature | Complete user outcome | Acceptance gate |
|---|---|---|---|
| 1 | Portable saves | Export/import a game’s cartridge progress and supported snapshots, with a clear restore preview | Versioned manifest, content/core compatibility checks, safe archive extraction and rollback; originals preserved on failure |
| 2 | Phone-to-TV transfer | Scan a QR code or type an address, pair temporarily, upload a personal cartridge and play | Both screens show progress; short-lived pairing, size/rate limits, safe paths, cancellation and session expiry |
| 3 | Controller doctor | See connected input devices and live buttons/sticks, diagnose drift, save per-controller/per-console profiles | Real key/axis readings, disconnect/reconnect and no held input; physical device coverage |
| 4 | Better game discovery | Browse a larger authorized homebrew catalog with useful genres, descriptions and artwork | Explicit redistribution rights, attribution/source/license records, verified download/import/play |
| 5 | Family profiles | Separate saves, favorites, history and bindings without online signup | Schema migration, reliable identity, strict save isolation and profile switching |
| 6 | Storage and downloads | See usage, remove content while preserving progress, resume supported downloads and recover queued work after restart | Space staging budget, atomic replacements, retries/backoff, useful offline/error states |

## Strong follow-on features

- **Per-game preferences:** remember scaling, volume and input profile independently; keep a clear reset-to-default action.
- **Save timeline:** thumbnails, named slots, last-known-good recovery and compatible save/core version metadata.
- **Metadata editor:** correct a title or artwork, review duplicates and create collections without modifying ROM bytes.
- **Accessibility:** adjustable text size, stronger focus cues, non-color status cues and tested screen-reader labels where supported.
- **Core management:** installed version, release notes, source/license links and a tested rollback path; never silently invalidate saves.
- **Couch party:** choose a compatible local multiplayer game, assign controllers and show a concise controls card. This requires real multi-port native input; the current bridge routes player one only.
- **More consoles:** validate NES/GBA/PS1 gameplay first. Add more 8/16-bit systems before expensive hardware-rendered consoles; follow CONSOLES.md’s acceptance gate.
- **Optional cloud save sync:** keep offline play independent, surface conflicts and let people recover either version. A real documented AWS implementation could support the AWS mini challenge; installation of AWS CLI alone does not qualify.

## Presentation extras after the core works

An attract/demo mode with original game clips, a short first-play controller check, contextual recovery instructions, a simple playtime view and an exportable redacted diagnostics report can improve the experience without overwhelming Home. Any new visible action should be demonstrated end to end before appearing in a hackathon video.

More controls on a screen are not automatically a better product. Prioritize complete journeys: get a game, get its console support, play with the available input device, preserve progress, and recover when something fails.
