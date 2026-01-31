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

# Force UI rebuild (Gradle handles incremental compilation)
rm -f build/sfarzo_ui.stamp

# Build
cmake --build build

# Copy UI to standalone bundle (post-build only runs when JUCE targets rebuild)
CMP_UI="ui/composeApp/build/compose/binaries/main/app/Sfarzo.app/Contents"
STANDALONE="build/Sfarzo_artefacts/Debug/Standalone/Sfarzo.app/Contents"
cp "$CMP_UI/MacOS/Sfarzo" "$STANDALONE/MacOS/Sfarzo_UI"
cp -r "$CMP_UI/app" "$STANDALONE/"
mv "$STANDALONE/app/Sfarzo.cfg" "$STANDALONE/app/Sfarzo_UI.cfg" 2>/dev/null || true
