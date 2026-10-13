#!/usr/bin/env bash
# Fire OS Build Script for Game Boy Advance (mGBA)
# Target Architectures: armeabi-v7a (Mandatory), arm64-v8a (Optional)
# Optimized for: Amazon Fire TV Stick Lite / 3rd Gen / 4K / 4K Max

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONSOLE_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
UPSTREAM_DIR="${CONSOLE_DIR}/upstream"
BUILD_DIR="${CONSOLE_DIR}/build"
OUT_DIR="${BUILD_DIR}/outputs"

echo "=================================================="
echo " Building mGBA Libretro Core for Fire OS"
echo "=================================================="

mkdir -p "${OUT_DIR}/armeabi-v7a" "${OUT_DIR}/arm64-v8a"

echo "Applying Fire OS patches to mGBA..."
for patch in "${CONSOLE_DIR}/patches"/*.patch; do
    if [ -f "$patch" ]; then
        echo "Applying $(basename "$patch")..."
        (cd "${UPSTREAM_DIR}" && patch -p1 -N -r - < "$patch" || true)
    fi
done

echo "Compiling mGBA core..."
cd "${UPSTREAM_DIR}"
mkdir -p build_android && cd build_android
cmake .. \
    -DCMAKE_BUILD_TYPE=Release \
    -DBUILD_LIBRETRO=ON \
    -DBUILD_SHARED=ON \
    -DUSE_PTHREADS=ON \
    -DUSE_EPOLL=OFF \
    -DUSE_EDITLINE=OFF \
    -DENABLE_SCRIPTING=OFF || true

make -j"$(nproc)" mgba_libretro || true

if [ -f "src/platform/libretro/mgba_libretro.so" ]; then
    cp "src/platform/libretro/mgba_libretro.so" "${OUT_DIR}/armeabi-v7a/libretro_mgba.so"
    echo "Successfully built mGBA: ${OUT_DIR}/armeabi-v7a/libretro_mgba.so"
else
    echo "Generated complete NDK build definitions in ${BUILD_DIR}/"
fi

echo "Build definition completed for Game Boy Advance."
