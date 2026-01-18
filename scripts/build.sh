#!/bin/bash
# Build script for Sfarzo
# CMake orchestrates: Native renderer → UI (Compose) → App
set -e
cd "$(dirname "$0")/.."

# Download instruments if not present
RHODES_DIR="res/jRhodes3c"
if [ ! -d "$RHODES_DIR" ]; then
    echo "=== Downloading jRhodes3c instrument ==="
    mkdir -p res
    curl -L -o /tmp/jRhodes3c.zip https://github.com/sfzinstruments/jlearman.jRhodes3c/archive/refs/heads/master.zip
    unzip -q /tmp/jRhodes3c.zip -d res
    mv res/jlearman.jRhodes3c-master "$RHODES_DIR"
    rm /tmp/jRhodes3c.zip
    echo "=== jRhodes3c downloaded to $RHODES_DIR ==="
fi

echo "=== Building Sfarzo ==="

# Configure
cmake -B build

# Build all targets
echo "=== Building native renderer ==="
cmake --build build --target sfarzo_native_renderer

echo "=== Building Compose UI ==="
cmake --build build --target sfarzo_ui

echo "=== Building Sfarzo Standalone ==="
cmake --build build --target Sfarzo_Standalone

echo "=== Building Sfarzo AU ==="
cmake --build build --target Sfarzo_AU

echo "=== Build complete ==="
