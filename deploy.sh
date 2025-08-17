#!/bin/bash

# Deploy APK to server
# This script builds the APK and deploys it to your web server

set -e  # Exit on any error

echo "🚀 Starting APK deployment..."

# Build the APK
echo "📱 Building APK..."
export ANDROID_HOME=/home/henry/android-sdk
./gradlew assembleDebug

# Check if build was successful
if [ ! -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "❌ APK build failed!"
    exit 1
fi

echo "✅ APK built successfully"

# Deploy to server
echo "🌐 Deploying to server..."

# Copy APK to server
scp app/build/outputs/apk/debug/app-debug.apk henry@romptele.com:/tmp/listen.apk

# Move and set permissions on server
ssh henry@romptele.com << 'EOF'
sudo mv /tmp/listen.apk /home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk
sudo chmod 644 /home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk
sudo chown root:root /home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk
echo "✅ APK deployed successfully!"
EOF

echo "🎉 Deployment complete!"
echo "📥 APK available at: https://hromp.com/downloads/listen.apk" 