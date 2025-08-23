#!/bin/bash

# Generate Android app icons from Play Store icon
# This script creates all required icon sizes and places them in the Android project

echo "🎨 Generating Android app icons from Play Store icon..."

# Check if ImageMagick is installed
if ! command -v convert &> /dev/null; then
    echo "Error: ImageMagick is required. Install with: sudo apt-get install imagemagick"
    exit 1
fi

# Check if source icon exists
SOURCE_ICON="icons/google-play-store/play-store-icon.png"
if [ ! -f "$SOURCE_ICON" ]; then
    echo "Error: Source icon not found at $SOURCE_ICON"
    exit 1
fi

# Get icon dimensions
ICON_INFO=$(identify "$SOURCE_ICON" 2>/dev/null)
if [ $? -ne 0 ]; then
    echo "Error: Could not read icon file. Make sure it's a valid PNG image."
    exit 1
fi

echo "📏 Source icon: $ICON_INFO"

# Create Android project icon directories if they don't exist
ANDROID_ICON_DIR="app/src/main/res"
mkdir -p "$ANDROID_ICON_DIR/mipmap-mdpi"
mkdir -p "$ANDROID_ICON_DIR/mipmap-hdpi"
mkdir -p "$ANDROID_ICON_DIR/mipmap-xhdpi"
mkdir -p "$ANDROID_ICON_DIR/mipmap-xxhdpi"
mkdir -p "$ANDROID_ICON_DIR/mipmap-xxxhdpi"

echo "📁 Creating Android app icons..."

# Generate regular app icons (square)
echo "  Creating regular app icons..."
convert "$SOURCE_ICON" -resize 48x48 "$ANDROID_ICON_DIR/mipmap-mdpi/ic_launcher.png"
convert "$SOURCE_ICON" -resize 72x72 "$ANDROID_ICON_DIR/mipmap-hdpi/ic_launcher.png"
convert "$SOURCE_ICON" -resize 96x96 "$ANDROID_ICON_DIR/mipmap-xhdpi/ic_launcher.png"
convert "$SOURCE_ICON" -resize 144x144 "$ANDROID_ICON_DIR/mipmap-xxhdpi/ic_launcher.png"
convert "$SOURCE_ICON" -resize 192x192 "$ANDROID_ICON_DIR/mipmap-xxxhdpi/ic_launcher.png"

# Generate round app icons
echo "  Creating round app icons..."
convert "$SOURCE_ICON" -resize 48x48 -background transparent -gravity center -extent 48x48 -alpha set -channel A -evaluate set 100% "$ANDROID_ICON_DIR/mipmap-mdpi/ic_launcher_round.png"
convert "$SOURCE_ICON" -resize 72x72 -background transparent -gravity center -extent 72x72 -alpha set -channel A -evaluate set 100% "$ANDROID_ICON_DIR/mipmap-hdpi/ic_launcher_round.png"
convert "$SOURCE_ICON" -resize 96x96 -background transparent -gravity center -extent 96x96 -alpha set -channel A -evaluate set 100% "$ANDROID_ICON_DIR/mipmap-xhdpi/ic_launcher_round.png"
convert "$SOURCE_ICON" -resize 144x144 -background transparent -gravity center -extent 144x144 -alpha set -channel A -evaluate set 100% "$ANDROID_ICON_DIR/mipmap-xxhdpi/ic_launcher_round.png"
convert "$SOURCE_ICON" -resize 192x192 -background transparent -gravity center -extent 192x192 -alpha set -channel A -evaluate set 100% "$ANDROID_ICON_DIR/mipmap-xxxhdpi/ic_launcher_round.png"

# Generate adaptive icon foreground layers
echo "  Creating adaptive icon foreground layers..."
convert "$SOURCE_ICON" -resize 108x108 -background transparent -gravity center -extent 108x108 "$ANDROID_ICON_DIR/mipmap-mdpi/ic_launcher_foreground.png"
convert "$SOURCE_ICON" -resize 162x162 -background transparent -gravity center -extent 162x162 "$ANDROID_ICON_DIR/mipmap-hdpi/ic_launcher_foreground.png"
convert "$SOURCE_ICON" -resize 216x216 -background transparent -gravity center -extent 216x216 "$ANDROID_ICON_DIR/mipmap-xhdpi/ic_launcher_foreground.png"
convert "$SOURCE_ICON" -resize 324x324 -background transparent -gravity center -extent 324x324 "$ANDROID_ICON_DIR/mipmap-xxhdpi/ic_launcher_foreground.png"
convert "$SOURCE_ICON" -resize 432x432 -background transparent -gravity center -extent 432x432 "$ANDROID_ICON_DIR/mipmap-xxxhdpi/ic_launcher_foreground.png"

echo "✅ Android app icons generated successfully!"
echo ""
echo "📁 Icons created in:"
echo "  $ANDROID_ICON_DIR/mipmap-*/"
echo ""
echo "📊 Icon counts:"
echo "  - 5 regular app icons (ic_launcher.png)"
echo "  - 5 round app icons (ic_launcher_round.png)"
echo "  - 5 adaptive icon foregrounds (ic_launcher_foreground.png)"
echo ""
echo "🎯 Next steps:"
echo "  1. Build your app to test the new icons"
echo "  2. Check how they look on different device densities"
echo "  3. Adjust the source icon if needed and re-run this script"
echo ""
echo "🔧 To rebuild with new icons:"
echo "  ./gradlew clean assembleDebug" 