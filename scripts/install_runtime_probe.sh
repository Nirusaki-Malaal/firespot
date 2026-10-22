#!/usr/bin/env bash
# Developer-only local sideload. Production downloading/installing is Phase 1.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ADB="${ADB:-adb}"
APK="${1:-$ROOT/artifacts/firespot-standard-debug.apk}"
"$ADB" get-state >/dev/null
"$ADB" install -r "$APK"
if [[ "$("$ADB" shell getprop ro.product.cpu.abilist64 | tr -d '\r')" != '' ]]; then
    ABI="$("$ADB" shell getprop ro.product.cpu.abilist64 | tr -d '\r' | cut -d, -f1)"
else
    ABI="$("$ADB" shell getprop ro.product.cpu.abilist32 | tr -d '\r' | cut -d, -f1)"
fi
# The APK is ARM-only, so a supported Fire OS device will run in its advertised ARM ABI.
case "$ABI" in armeabi-v7a|arm64-v8a) ;; *) echo "Unsupported device ABI: $ABI" >&2; exit 1;; esac
CORE="$ROOT/artifacts/gameboy/$ABI/libretro_gambatte.so"
[[ -f "$CORE" ]] || { echo "Missing $CORE. Build cores first." >&2; exit 1; }
python3 "$ROOT/scripts/verify_artifacts.py" --apk "$APK" --cores "$ROOT/artifacts/gameboy" >/dev/null
"$ADB" shell run-as com.fireemu mkdir -p "files/cores/$ABI"
"$ADB" shell -T run-as com.fireemu sh -c "'cat > files/cores/$ABI/libretro_gambatte.so'" < "$CORE"
"$ADB" shell am start -n com.fireemu/.debug.RuntimeProbeActivity
