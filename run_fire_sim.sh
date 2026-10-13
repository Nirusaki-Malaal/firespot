#!/usr/bin/env bash
# ==============================================================================
# FireSpot Simulator Shortcut
# Delegates execution to fire_sim/launch.sh
# ==============================================================================
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$DIR/fire_sim/launch.sh" "$@"
