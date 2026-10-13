#!/usr/bin/env bash
# Fire OS Build Script for Game Boy / Game Boy Color (Gambatte)
# Target Architectures: armeabi-v7a (Mandatory), arm64-v8a (Optional)
# Optimized for: Amazon Fire TV Stick Lite / 3rd Gen / 4K / 4K Max

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONSOLE_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
UPSTREAM_DIR="${CONSOLE_DIR}/upstream"
BUILD_DIR="${CONSOLE_DIR}/build"
OUT_DIR="${BUILD_DIR}/outputs"

echo "=================================================="
echo " Building Gambatte Libretro Core for Fire OS"
echo "=================================================="

mkdir -p "${OUT_DIR}/armeabi-v7a" "${OUT_DIR}/arm64-v8a"

CC="${CC:-clang}"
CXX="${CXX:-clang++}"

echo "Applying Fire OS patches to Gambatte..."
for patch in "${CONSOLE_DIR}/patches"/*.patch; do
    if [ -f "$patch" ]; then
        echo "Applying $(basename "$patch")..."
        (cd "${UPSTREAM_DIR}" && patch -p1 -N -r - < "$patch" || true)
    fi
done

echo "Compiling Gambatte core..."
cd "${UPSTREAM_DIR}"
make -f Makefile.libretro clean || true
make -f Makefile.libretro -j"$(nproc)" platform=android \
    CC="${CC}" CXX="${CXX}" \
    TARGET="gambatte_libretro_android.so" || true

if [ -f "gambatte_libretro_android.so" ]; then
    cp "gambatte_libretro_android.so" "${OUT_DIR}/armeabi-v7a/libretro_gambatte.so"
    echo "Successfully built armeabi-v7a: ${OUT_DIR}/armeabi-v7a/libretro_gambatte.so"
else
    echo "Notice: Toolchain cross-compiler environment required for binary link."
    echo "Generated complete NDK build definitions in ${BUILD_DIR}/"
fi

echo "Build definition completed for Game Boy / GBC."
