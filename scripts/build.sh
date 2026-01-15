#!/bin/bash
# Build script for Sfarzo
# CMake orchestrates: Native renderer → UI (Compose) → App
set -e
cd "$(dirname "$0")/.."

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
