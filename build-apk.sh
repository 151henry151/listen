#!/bin/bash

# Build script for Listen app APK
echo "🔨 Building Listen APK..."
echo ""

# Set Android SDK location if available
if [ -d "$HOME/android-sdk" ]; then
    export ANDROID_HOME=$HOME/android-sdk
    echo "✓ Android SDK found at: $ANDROID_HOME"
elif [ -d "/opt/android-sdk" ]; then
    export ANDROID_HOME=/opt/android-sdk
    echo "✓ Android SDK found at: $ANDROID_HOME"
else
    echo "⚠️  Android SDK not found in standard locations"
    echo "   Please set ANDROID_HOME environment variable"
    echo ""
fi

# Create local.properties if ANDROID_HOME is set
if [ -n "$ANDROID_HOME" ]; then
    echo "sdk.dir=$ANDROID_HOME" > local.properties
    echo "✓ Created local.properties with SDK location"
    echo ""
fi

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build debug APK
echo ""
echo "📦 Building debug APK..."
./gradlew assembleDebug

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "📱 APK location:"
    echo "   app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    
    # Show APK info
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        APK_SIZE=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
        echo "📊 APK size: $APK_SIZE"
        echo ""
    fi
    
    echo "📥 To install on a connected device/emulator:"
    echo "   adb install -r app/build/outputs/apk/debug/app-debug.apk"
    echo ""
else
    echo ""
    echo "❌ Build failed!"
    echo "   Check the error messages above for details"
    exit 1
fi