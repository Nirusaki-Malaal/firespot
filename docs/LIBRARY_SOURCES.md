# Game libraries and import

FireSpot offers two library sources from Discover and Settings:

- **FireSpot catalog:** a native, signed offline catalog with three original CC0 games and CPU-specific emulator downloads. Its counts describe that catalog and the installed library.
- **ROMsGames library:** an on-demand in-app browser opening [the requested console index](https://www.romsgames.net/roms/). The provider supplies its own pages, search, artwork, counts and CAPTCHA. The browser has Back, Consoles, Reload, Cancel download and My library controls. Unsupported systems listed on the website do not become supported FireSpot consoles.

Selecting a compatible downloadable file opens an import confirmation. HTTPS downloads run off the UI thread with progress, timeouts, bounded redirects and cancellation. Browser cookies are forwarded only to their original host. Browser downloads are external content, not signed FireSpot catalog entries. The browser does not bypass CAPTCHA or promise the availability of a third-party download server. Server-provided links must resolve to a supported file; executable Android packages and HTML pages are not imported as games.

The importer handles GB/GBC, NES, GBA, SNES, Master System, Game Gear and SG-1000 cartridges; single-file PS1 ISO/PBP/PS-X EXE content; and ZIP archives containing exactly one supported game. Multi-game ZIPs and multi-file CUE/BIN disc sets require extraction/preparation outside this flow. ZIP names are never used as filesystem paths. Limits apply to file bytes, archive entries and unrelated archive data. Content is validated before commit, hashed for deduplication and stored under an app-generated filename. Selecting the same game again preserves its existing library record and progress.

The app does not mirror the provider's library into its APK or GitHub releases. A locally imported game is private app content. Importing does not guarantee that its mapper, firmware requirements, rendering backend or input layout are supported by the selected core.

## Adding a game to the signed FireSpot catalog

Record the title, author, console, genre, release year, player count, source URL and redistribution license. Include required attribution and corresponding source. Stage exact ROM bytes, record size/SHA-256, test download/import/play, increment the revision and sign the catalog. Publish new immutable release assets and intentionally update the app endpoint.

## Performance

The native library works without initializing a WebView. The provider browser exists only while its activity is open; exit cancels transfers, disconnects requests, detaches and destroys the WebView. Local artwork decoding is bounded to card dimensions and the image memory cache is capped. The provider's own page and WebView implementation still determine browsing memory and compatibility.
