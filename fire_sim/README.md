# FireSpot — Fire OS TV Environment Simulator

A dedicated desktop simulation environment for **Amazon Fire TV Sticks (Fire OS 7/8)** running on Linux. It accurately simulates a 1080p Smart TV connected to a Fire TV Stick with an Alexa Voice Remote, allowing complete testing of the FireSpot TV UI and gaming emulators.

---

## Quick Start

### 1. Launch the Simulator
From this directory:
```bash
./launch.sh
```
Or from the project root (`agent`):
```bash
./run_fire_sim.sh
```

The script automatically:
1. Detects your display environment (`WAYLAND_DISPLAY` / `DISPLAY`).
2. Starts the secure local server (`127.0.0.1:8080`) if not already active.
3. Launches the native TV Simulator window maximized with a strict **16:9** aspect ratio.

---

## Folder Structure

```
fire_sim/
├── launch.sh              # One-click launch script
├── simulator.py           # Native PyQt6 TV simulator with 1080p canvas & Alexa Remote
├── server.py              # Secure loopback runtime server (localhost only, CORS & health probe)
├── web/                   # Leanback TV interface & multi-console web emulation runtime
│   ├── index.html         # FireSpot 1080p TV UI & retro engine
│   └── art/               # Game boxart & title illustrations
├── apply_details_mockup.js# Game details layout generator
├── generate_sample_roms.py# Test ROM generator
├── test_verification.py   # Automated audit & verification tests
└── README.md              # This file
```

---

## TV Controls & Alexa Remote

### Remote Buttons:
- **D-pad (Up/Down/Left/Right)**: Navigate tiles and menus
- **Select**: Activate focused item / Play game
- **Back**: Go back / Exit sub-menu
- **Home**: Return to FireSpot home screen
- **Menu (☰)**: In-game options / pause menu
- **Play/Pause**: Toggle game pause

### Keyboard Shortcuts:
| Action | Key |
|---|---|
| **D-pad Navigation** | `Arrow Keys` |
| **Select / OK** | `Enter` |
| **Back** | `Backspace` or `Escape` |
| **Home** | `H` |
| **Menu Overlay** | `M` |
| **Play / Pause** | `Space` |
| **Retro Button A** | `Z` |
| **Retro Button B** | `X` |
| **Toggle Remote Sidebar** | `Tab` or `R` |
| **Full Screen TV Mode** | `F11` (Edge-to-edge 1080p TV) |
| **Overscan Safe Bounds** | `F9` (Title-Safe 80% & Action-Safe 90%) |
| **Fire OS Compatibility Audit** | `F8` (Leanback checklist) |

---

## Security & Architecture
- **Loopback Isolation**: The server strictly binds to `127.0.0.1` and forbids directory traversal.
- **Aspect Ratio Locking**: TV viewport is strictly constrained to `16:9` with dynamic zoom factor calculation (`zoom = width / 1920.0`), guaranteeing that all rows and elements scale smoothly.
- **Process Isolation**: Launcher UI runs in a low-memory profile (<150 MB), simulating Fire OS LMK headroom.
