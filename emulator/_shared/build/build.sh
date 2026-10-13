#!/usr/bin/env bash
# Fire OS Build Script for Shared Leanback Launcher
# Assembles native bridges, collects core libraries into jniLibs, and produces signed debug APK

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SHARED_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
EMULATOR_ROOT="$(cd "${SHARED_DIR}/.." && pwd)"
JNILIBS_DIR="${SHARED_DIR}/src/main/jniLibs"
OUT_DIR="${SHARED_DIR}/build/outputs"

echo "=================================================="
echo " Building FireEmu Shared Launcher Suite"
echo "=================================================="

mkdir -p "${JNILIBS_DIR}/armeabi-v7a" "${JNILIBS_DIR}/arm64-v8a" "${OUT_DIR}"

# 1. Compile Native Bridge
echo "Building libretro_bridge.so..."
if command -v ndk-build >/dev/null 2>&1; then
    ndk-build -C "${SHARED_DIR}/src/main/jni" \
        NDK_OUT="${SHARED_DIR}/build/obj" \
        NDK_LIBS_OUT="${JNILIBS_DIR}"
fi

# 2. Gather Built Core Shared Libraries
echo "Collecting console core libraries..."
for console in nes gameboy gba ps1 psp; do
    core_out="${EMULATOR_ROOT}/${console}/build/outputs"
    if [ -d "${core_out}/armeabi-v7a" ]; then
        cp -f "${core_out}/armeabi-v7a"/*.so "${JNILIBS_DIR}/armeabi-v7a/" 2>/dev/null || true
    fi
    if [ -d "${core_out}/arm64-v8a" ]; then
        cp -f "${core_out}/arm64-v8a"/*.so "${JNILIBS_DIR}/arm64-v8a/" 2>/dev/null || true
    fi
done

echo "Assembling debug APK package..."
# Invoke Gradle or automated FireEmu APK Packager
if [ -f "${SHARED_DIR}/gradlew" ]; then
    (cd "${SHARED_DIR}" && ./gradlew assembleDebug)
    cp "${SHARED_DIR}/build/outputs/apk/debug/app-debug.apk" "${OUT_DIR}/fireemu-debug.apk" 2>/dev/null || true
else
    python3 "${SCRIPT_DIR}/package_apk.py"
fi

echo "=================================================="
echo " Build Process Completed Successfully"
echo "=================================================="
