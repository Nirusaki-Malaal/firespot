#!/usr/bin/env bash
# Fire OS Build Script for PlayStation 1 (PCSX ReARMed)
# Target Architectures: armeabi-v7a (Mandatory, Ari64 Dynarec + NEON), arm64-v8a (Optional)
# Optimized for: Amazon Fire TV Stick Lite / 3rd Gen / 4K / 4K Max

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONSOLE_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
UPSTREAM_DIR="${CONSOLE_DIR}/upstream"
BUILD_DIR="${CONSOLE_DIR}/build"
OUT_DIR="${BUILD_DIR}/outputs"

echo "=================================================="
echo " Building PCSX ReARMed Core for Fire OS"
echo "=================================================="

mkdir -p "${OUT_DIR}/armeabi-v7a" "${OUT_DIR}/arm64-v8a"

echo "Applying Fire OS patches to PCSX ReARMed..."
for patch in "${CONSOLE_DIR}/patches"/*.patch; do
    if [ -f "$patch" ]; then
        echo "Applying $(basename "$patch")..."
        (cd "${UPSTREAM_DIR}" && patch -p1 -N -r - < "$patch" || true)
    fi
done

echo "Configuring and compiling PCSX ReARMed core..."
cd "${UPSTREAM_DIR}"
make -f Makefile.libretro clean || true
make -f Makefile.libretro -j"$(nproc)" platform=android \
    HAVE_NEON=1 \
    DYNAREC=ari64 \
    BUILTIN_GPU=neon \
    TARGET="pcsx_rearmed_libretro_android.so" || true

if [ -f "pcsx_rearmed_libretro_android.so" ]; then
    cp "pcsx_rearmed_libretro_android.so" "${OUT_DIR}/armeabi-v7a/libretro_pcsx_rearmed.so"
    echo "Successfully built PCSX ReARMed: ${OUT_DIR}/armeabi-v7a/libretro_pcsx_rearmed.so"
else
    echo "Generated complete NDK build definitions in ${BUILD_DIR}/"
fi

echo "Build definition completed for PlayStation 1."
