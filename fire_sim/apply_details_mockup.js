const fs = require("fs");
const art = JSON.parse(fs.readFileSync("web/art_data.json", "utf8"));
let html = fs.readFileSync("web/index.html", "utf8");

// Update ART_MAP in html to include the new hero art
html = html.replace(/const ART_MAP = \{[\s\S]*?\};\s*const CONSOLES =/m, `const ART_MAP = ${JSON.stringify(art)};\n    const CONSOLES =`);

// Build Pixel-Faithful Screen Details Markup
const newScreenDetailsMarkup = `
        <!-- 4. GAME DETAILS SCREEN (Pixel-Faithful Mockup) -->
        <div id="screen-details" class="hidden w-full h-full flex relative overflow-hidden bg-gradient-to-r from-[#0A1226] via-[#0E1A33] to-[#162A52]">
          
          <!-- Left Slim Navigation Rail (72px, matches mockup) -->
          <div id="details-left-rail" class="w-[64px] h-full flex flex-col justify-between items-center py-6 border-r border-white/5 z-30 flex-shrink-0" style="background: rgba(10, 18, 38, 0.75);">
            <!-- Logo top -->
            <div class="flex flex-col items-center gap-1 cursor-pointer" onclick="navigateTo('home')">
              <span class="text-amber-400 text-xl font-black">⚡</span>
              <span class="text-[11px] font-bold text-amber-400 tracking-tight">FireSpot</span>
            </div>

            <!-- Vertical icon group -->
            <div class="flex flex-col items-center gap-7">
              <button class="tv-focusable w-10 h-10 rounded-xl flex items-center justify-center text-[#8FA0C0] hover:text-white transition" onclick="navigateTo('home')">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/></svg>
              </button>
              <button class="tv-focusable w-10 h-10 rounded-xl flex items-center justify-center text-[#8FA0C0] hover:text-white transition" onclick="navigateTo('library')">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M7 4v16M17 4v16M3 8h4m10 0h4M3 12h18M3 16h4m10 0h4M4 20h16a1 1 0 001-1V5a1 1 0 00-1-1H4a1 1 0 00-1 1v14a1 1 0 001 1z"/></svg>
              </button>
              <button class="tv-focusable w-10 h-10 rounded-xl flex items-center justify-center text-[#8FA0C0] hover:text-white transition" onclick="navigateTo('library')">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/></svg>
              </button>
              <!-- Active Gamepad Rail Icon -->
              <button class="tv-focusable w-10 h-10 rounded-xl flex items-center justify-center text-[#E4EFFF] relative transition" style="background: rgba(30, 60, 122, 0.85); box-shadow: 0 0 14px rgba(61,139,255,0.4);">
                <div class="absolute left-0 top-1.5 bottom-1.5 w-[3px] bg-[#3D8BFF] rounded-r"></div>
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M11 12H9m2 0h2m-2 0V9m0 3v3m9.5-2.5a3.5 3.5 0 01-3.5 3.5h-1a3 3 0 00-2.83 2H8.33a3 3 0 00-2.83-2H4.5A3.5 3.5 0 011 12.5v-1A3.5 3.5 0 014.5 8h15a3.5 3.5 0 013.5 3.5v1z"/></svg>
              </button>
              <button class="tv-focusable w-10 h-10 rounded-xl flex items-center justify-center text-[#8FA0C0] hover:text-white transition" onclick="navigateTo('settings')">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
              </button>
            </div>
            <div></div>
          </div>

          <!-- Main Content Area with Backdrop Illustration -->
          <div class="flex-1 h-full px-10 py-7 flex flex-col justify-between relative z-10">
            
            <!-- Breadcrumbs Top Bar -->
            <div class="flex items-center gap-2 text-[12px] text-[#A9B4CC] font-semibold tracking-wide uppercase">
              <span class="hover:text-white cursor-pointer" onclick="navigateTo('home')">CONSOLES</span>
              <span class="text-[#8FA0C0]">></span>
              <span id="details-crumb-console" class="hover:text-white cursor-pointer" onclick="navigateTo('library')">NES</span>
              <span class="text-[#8FA0C0]">></span>
              <span id="details-crumb-title" class="text-white font-bold flex items-center gap-1.5">
                SUPER MARIO BROS.
                <span class="inline-block px-1.5 py-0.5 rounded text-[10px] bg-black/40 border border-white/20 font-mono">🎮</span>
              </span>
            </div>

            <!-- Title & Metadata & Description Area -->
            <div class="max-w-[480px] my-auto">
              <!-- Big Bold Title with Period -->
              <h1 id="details-big-title" class="text-[44px] font-black text-white tracking-tight uppercase leading-none mb-3">
                SUPER MARIO BROS.
              </h1>

              <!-- Metadata Line -->
              <div id="details-meta-line" class="text-[14px] text-[#CBD5EA] font-semibold flex items-center gap-2 mb-4">
                <span>NES</span>
                <span>•</span>
                <span>1985</span>
                <span>•</span>
                <span>Platformer</span>
                <span>•</span>
                <span>1 Player</span>
              </div>

              <!-- Synopsis Paragraph -->
              <p id="details-synopsis" class="text-[13px] text-[#A9B4CC] leading-[20px] font-normal mb-8">
                Rescue Princess Peach from the clutches of Bowser in the ultimate 8-bit platforming adventure. Leap over obstacles, stomp on enemies, and navigate through the Mushroom Kingdom's challenging stages.
              </p>

              <!-- 2-Row Action Buttons Grid (Matches Mockup Exactly) -->
              <div class="flex flex-col gap-3">
                <!-- Top Row: [▶ PLAY NOW] [▶ RESUME] [💾 SAVE STATES] -->
                <div class="flex items-center gap-3">
                  <!-- Focused Gold Play Button -->
                  <button id="btn-details-playnow" class="tv-btn tv-focusable h-[44px] px-6 rounded-[12px] flex items-center gap-2.5 font-black text-[14px] transition cursor-pointer relative" style="background: linear-gradient(180deg, #FAD961 0%, #F76B1C 100%); color: #12141A; border: 2px solid #3D8BFF; box-shadow: 0 0 20px rgba(61,139,255,0.65);" onclick="launchCurrentGame(false)">
                    <span class="text-black text-base">▶</span>
                    <span>PLAY NOW</span>
                  </button>

                  <button id="btn-details-resume" class="tv-btn tv-focusable h-[44px] px-5 rounded-[12px] flex items-center gap-2 text-white font-bold text-[13px] transition cursor-pointer" style="background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.12);" onclick="launchCurrentGame(true)">
                    <span>▶</span>
                    <span>RESUME</span>
                  </button>

                  <button id="btn-details-savestates" class="tv-btn tv-focusable h-[44px] px-5 rounded-[12px] flex items-center gap-2 text-white font-bold text-[13px] transition cursor-pointer" style="background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.12);" onclick="saveGameState()">
                    <span>💾</span>
                    <span>SAVE STATES</span>
                  </button>
                </div>

                <!-- Bottom Row: [🎮 CONTROLLER] [⚙ SETTINGS] [❤ ADD TO FAVORITES] -->
                <div class="flex items-center gap-3">
                  <button id="btn-details-controller" class="tv-btn tv-focusable h-[42px] px-5 rounded-[12px] flex items-center gap-2 text-white font-bold text-[12px] transition cursor-pointer" style="background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.12);" onclick="navigateTo('mapping')">
                    <span>🎮</span>
                    <span>CONTROLLER</span>
                  </button>

                  <button id="btn-details-settings" class="tv-btn tv-focusable h-[42px] px-5 rounded-[12px] flex items-center gap-2 text-white font-bold text-[12px] transition cursor-pointer" style="background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.12);" onclick="navigateTo('settings')">
                    <span>⚙</span>
                    <span>SETTINGS</span>
                  </button>

                  <button id="btn-details-fav" class="tv-btn tv-focusable h-[42px] px-5 rounded-[12px] flex items-center gap-2 text-white font-bold text-[12px] transition cursor-pointer" style="background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.12);" onclick="toggleCurrentFavorite()">
                    <span id="fav-icon">❤</span>
                    <span id="fav-text">ADD TO FAVORITES</span>
                  </button>
                </div>
              </div>

            </div>

            
          </div>

          <!-- Right Hero Artwork Illustration (Full Bleed Mario & Bowser from Mockup) -->
          <div class="w-[450px] h-full absolute right-0 top-0 bottom-0 pointer-events-none flex items-center justify-end overflow-hidden z-0">
            <img id="details-hero-art" src="${art['mario_hero_backdrop.png'] || ''}" class="h-[105%] max-w-none object-contain translate-x-4 drop-shadow-2xl" alt="Hero Art" />
            <div class="absolute inset-0 bg-gradient-to-r from-[#0A1226] via-transparent to-transparent"></div>
          </div>
        </div>
`;

// Replace the old screen-details in HTML
html = html.replace(/<!-- 4. GAME DETAILS SHEET -->[\s\S]*?<!-- 5. SEARCH SCREEN -->/m, newScreenDetailsMarkup + "\n\n        <!-- 5. SEARCH SCREEN -->");

// Update renderDetails function to populate the new mockup layout
const newRenderDetailsFunction = `
    function renderDetails(game) {
      const cInfo = CONSOLES.find(c => c.id === game.console) || CONSOLES[0];
      
      // Breadcrumbs
      const crumbConsole = document.getElementById("details-crumb-console");
      if (crumbConsole) crumbConsole.innerText = cInfo.short;
      const crumbTitle = document.getElementById("details-crumb-title");
      if (crumbTitle) crumbTitle.innerHTML = game.title.toUpperCase() + ' <span class="inline-block px-1.5 py-0.5 rounded text-[10px] bg-black/40 border border-white/20 font-mono">🎮</span>';

      // Big Title
      const bigTitle = document.getElementById("details-big-title");
      if (bigTitle) bigTitle.innerText = game.title.toUpperCase() + '.';

      // Metadata line
      const metaLine = document.getElementById("details-meta-line");
      const year = game.console === "nes" ? 1985 : (game.console === "gameboy" ? 1989 : (game.console === "gba" ? 2002 : 1997));
      const genre = game.console === "nes" ? "Platformer" : (game.console === "gameboy" ? "Adventure" : "Action");
      if (metaLine) {
        metaLine.innerHTML = '<span>' + cInfo.short + '</span><span>•</span><span>' + year + '</span><span>•</span><span>' + genre + '</span><span>•</span><span>1-2 Players</span>';
      }

      // Synopsis
      const syn = document.getElementById("details-synopsis");
      if (syn) {
        if (game.id === 1) {
          syn.innerText = "Rescue Princess Peach from the clutches of Bowser in the ultimate 8-bit platforming adventure. Leap over obstacles, stomp on enemies, and navigate through the Mushroom Kingdom's challenging stages.";
        } else {
          syn.innerText = "Experience the legendary classic " + game.title + " on FireSpot. Featuring ultra-low latency direct Libretro emulation, integer scaling, and full controller remapping.";
        }
      }

      // Favorite toggle text
      const favText = document.getElementById("fav-text");
      if (favText) {
        favText.innerText = game.fav ? "IN FAVORITES" : "ADD TO FAVORITES";
      }

      // Hero artwork image
      const heroArt = document.getElementById("details-hero-art");
      if (heroArt) {
        if (game.id === 1) {
          heroArt.src = ART_MAP["mario_hero_backdrop.png"];
        } else if (game.id === 2) {
          heroArt.src = ART_MAP["zelda_crop.png"];
        } else if (game.id === 3) {
          heroArt.src = ART_MAP["metroid_crop.png"];
        } else if (game.id === 4) {
          heroArt.src = ART_MAP["megaman2_crop.png"];
        } else if (game.id === 5) {
          heroArt.src = ART_MAP["pokemon_red_crop.png"];
        } else {
          heroArt.src = ART_MAP["mario_hero_backdrop.png"];
        }
      }

      updateFocusHighlight();
    }
`;

html = html.replace(/function renderDetails\(game\)[\s\S]*?updateFocusHighlight\(\);\s*\}/m, newRenderDetailsFunction);

fs.writeFileSync("web/index.html", html, "utf8");
console.log("Successfully updated web/index.html with the exact Game Details mockup!");
