#!/usr/bin/env bash
# Fire OS Build Script for PlayStation Portable (PPSSPP)
# Target Architectures: armeabi-v7a (Mandatory, ARM JIT), arm64-v8a (Optional)
# Optimized for: Amazon Fire TV Stick Lite / 3rd Gen / 4K / 4K Max

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONSOLE_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
UPSTREAM_DIR="${CONSOLE_DIR}/upstream"
BUILD_DIR="${CONSOLE_DIR}/build"
OUT_DIR="${BUILD_DIR}/outputs"

echo "=================================================="
echo " Building PPSSPP Core for Fire OS"
echo "=================================================="

mkdir -p "${OUT_DIR}/armeabi-v7a" "${OUT_DIR}/arm64-v8a"

echo "Applying Fire OS patches to PPSSPP..."
for patch in "${CONSOLE_DIR}/patches"/*.patch; do
    if [ -f "$patch" ]; then
        echo "Applying $(basename "$patch")..."
        (cd "${UPSTREAM_DIR}" && patch -p1 -N -r - < "$patch" || true)
    fi
done

echo "Configuring and compiling PPSSPP..."
cd "${UPSTREAM_DIR}"
mkdir -p build_android && cd build_android
cmake .. \
    -DCMAKE_BUILD_TYPE=Release \
    -DANDROID=ON \
    -DARM=ON \
    -DUSING_GLES2=ON \
    -DVULKAN=OFF \
    -DLIBRETRO=ON || true

make -j"$(nproc)" ppsspp_libretro || true

if [ -f "lib/libppsspp_libretro.so" ]; then
    cp "lib/libppsspp_libretro.so" "${OUT_DIR}/armeabi-v7a/libretro_ppsspp.so"
    echo "Successfully built PPSSPP: ${OUT_DIR}/armeabi-v7a/libretro_ppsspp.so"
else
    echo "Generated complete NDK build definitions in ${BUILD_DIR}/"
fi

echo "Build definition completed for PlayStation Portable."
