#!/usr/bin/env bash
# ==============================================================================
# FireSpot - Fire OS TV Environment Simulator Launcher
# ==============================================================================
# Dedicated Linux desktop simulation of an Amazon Fire TV Stick 4K (Fire OS 7/8)
# connected via HDMI to a 1080p Smart TV.
#
# Usage:
#   ./launch.sh          (Run from fire_sim directory)
#   ../run_fire_sim.sh   (Run from agent root)
# ==============================================================================

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

# 1. Environment Display Detection
if [ -z "$WAYLAND_DISPLAY" ] && [ -n "$DISPLAY" ]; then
    export WAYLAND_DISPLAY="wayland-1"
elif [ -z "$WAYLAND_DISPLAY" ] && [ -z "$DISPLAY" ]; then
    export WAYLAND_DISPLAY="wayland-1"
    export DISPLAY=":1"
fi

# 2. Ensure Background Local Server is Running
SERVER_PID=""
if ! curl -s -m 1 http://127.0.0.1:8080/health > /dev/null 2>&1; then
    echo "[*] Starting Fire OS secure backend server on 127.0.0.1:8080..."
    python3 "$DIR/server.py" > /dev/null 2>&1 &
    SERVER_PID=$!
    # Wait briefly for server to bind
    for i in {1..15}; do
        if curl -s -m 0.5 http://127.0.0.1:8080/health > /dev/null 2>&1; then
            break
        fi
        sleep 0.2
    done
fi

cleanup() {
    if [ -n "$SERVER_PID" ] && kill -0 "$SERVER_PID" 2>/dev/null; then
        kill "$SERVER_PID" 2>/dev/null || true
    fi
}
trap cleanup EXIT INT TERM

echo "[*] Launching Fire OS TV Environment Simulator (1080p 16:9)..."
python3 "$DIR/simulator.py" "$@"
