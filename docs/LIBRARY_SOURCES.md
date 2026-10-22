# Library sources and browsing design

The user-requested [ROMsGames console index](https://www.romsgames.net/roms/) was reviewed as a browsing reference. Its console-first organization and alphabetical navigation informed FireSpot's native console catalog. FireSpot's game/system counts are computed from its own signed catalog and installed library.

FireSpot does not scrape or redistribute ROMsGames ROMs, artwork, ratings or download counts. An accessible download page is not a redistribution record. The in-app Get buttons use the curated, signed FireSpot catalog, currently containing three original CC0 games. The site is not the app's download feed, and no browser/WebView has been embedded into the TV flow.

## Implemented library experience

- Games, Consoles, Installed and Console support views.
- Catalog search using a physical or on-screen keyboard, matching titles/descriptions/authors/genres.
- Console filters and A–Z, size and release-year sorting.
- Accurate catalog and installed-game counts per console.
- Title, author, license, genre, year and download size on game cards.
- A clear empty state offering cartridge import rather than a fake game list.
- Signed on-demand core installation selected for the running process CPU.
- GB/GBC, NES, GBA, SNES, Master System, Game Gear and SG-1000 cartridge imports.

SNES and Sega packages remain experimental. Android native initialization is checked, but that is not evidence that every game runs or that Fire TV performance is adequate. BIOS-dependent/disc systems still need dedicated import/setup flows.

## Adding an authorized game

Record the title, author, console, genre, release year, supported player count, source URL and exact redistribution license. Keep required attribution/license text and corresponding source alongside the release where the license requires it. Verify that the assets as well as code are covered: a source-code license alone may not cover music or artwork.

Stage the exact permitted ROM bytes as a release asset, record its size/SHA-256, test download/import/play with the intended core, increment the catalog revision, and sign the catalog with the publisher key. Publish a new immutable core/catalog release and update the application endpoint intentionally. Do not silently replace existing release bytes or publish unverified commercial game dumps.

Future catalog improvements include more developer-approved homebrew, richer original/licensed artwork, tags, relevance-ranked search and a rights/attribution detail sheet. Personal ROM import remains separate from redistribution through the public catalog.
