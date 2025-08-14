#!/bin/bash

# Development script for hot-reloading Android app
export ANDROID_HOME=~/android-sdk

echo "🚀 Starting Listen app development mode..."
echo "📱 Make sure the emulator is running or device is connected"
echo "🔄 This script will continuously build and install the app"
echo ""

# Function to build and install
build_and_install() {
    echo "🔨 Building app..."
    ./gradlew assembleDebug
    
    if [ $? -eq 0 ]; then
        echo "✅ Build successful! Installing..."
        adb install -r app/build/outputs/apk/debug/app-debug.apk
        
        if [ $? -eq 0 ]; then
            echo "✅ App installed successfully!"
            echo "📱 Launching app..."
            adb shell am start -n com.listen.app/.ui.MainActivity
        else
            echo "❌ Failed to install app"
        fi
    else
        echo "❌ Build failed!"
    fi
    echo ""
}

# Initial build and install
build_and_install

echo "👀 Watching for changes... (Press Ctrl+C to stop)"
echo "💡 Make changes to your code and save to trigger rebuild"
echo ""

# Watch for file changes and rebuild
while true; do
    # Wait for any .kt or .xml file changes
    inotifywait -r -e modify,create,delete app/src/ 2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo "📝 File changed, rebuilding..."
        build_and_install
    fi
done 