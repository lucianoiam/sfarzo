#!/bin/bash
# Run the Sfarzo standalone app
set -e
cd "$(dirname "$0")/.."

# Build if needed
if [ ! -d "build" ]; then
    echo "Building Sfarzo..."
    ./scripts/build.sh
fi

# Find and run the standalone app
if [ "$(uname)" == "Darwin" ]; then
    # Try Debug build first, then Release/default
    if [ -f "build/Sfarzo_artefacts/Debug/Standalone/Sfarzo.app/Contents/MacOS/Sfarzo" ]; then
        APP="build/Sfarzo_artefacts/Debug/Standalone/Sfarzo.app/Contents/MacOS/Sfarzo"
    elif [ -f "build/Sfarzo_artefacts/Standalone/Sfarzo.app/Contents/MacOS/Sfarzo" ]; then
        APP="build/Sfarzo_artefacts/Standalone/Sfarzo.app/Contents/MacOS/Sfarzo"
    else
        echo "Error: Sfarzo standalone not found"
        echo "Run ./scripts/build.sh first"
        exit 1
    fi
    exec "$APP"
else
    echo "Sfarzo not yet supported on this platform"
    exit 1
fi
