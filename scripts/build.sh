#!/bin/bash
# Build script for Sfarzo
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
fi

# Configure only on first build
if [ ! -f build/CMakeCache.txt ]; then
    cmake -B build -G Ninja
fi

# Build (cmake handles incremental builds)
cmake --build build
