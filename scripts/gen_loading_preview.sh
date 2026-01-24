#!/bin/bash
# Generate LoadingPreview.h from loading_preview.png
# First run the app with captureFirstFrame enabled in main.kt

PNG_FILE="/tmp/loading_preview.png"
HEADER_FILE="src/LoadingPreview.h"

if [ ! -f "$PNG_FILE" ]; then
    echo "Error: $PNG_FILE not found"
    echo "Run the app first with captureFirstFrame enabled in main.kt"
    exit 1
fi

# Get file size
FILE_SIZE=$(wc -c < "$PNG_FILE" | tr -d ' ')

cat > "$HEADER_FILE" << HEADER_START
// Auto-generated from loading_preview.png
// This image is displayed while the Compose UI child process loads.

#pragma once

HEADER_START

# Generate the array (excluding the length line from xxd)
xxd -i "$PNG_FILE" | grep -v "unsigned int" | sed 's/unsigned char .*\[\]/static const unsigned char loading_preview_png[]/' >> "$HEADER_FILE"

# Add the correct length
echo "static const unsigned int loading_preview_png_len = $FILE_SIZE;" >> "$HEADER_FILE"

echo "Generated $HEADER_FILE from $PNG_FILE ($FILE_SIZE bytes)"
