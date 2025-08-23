#!/bin/bash

echo "=== Listen App Auto-Build System Status ==="
echo ""

echo "🔧 Webhook Server Status:"
if pgrep -f webhook-server.py > /dev/null; then
    echo "✅ Webhook server is running (PID: $(pgrep -f webhook-server.py))"
    echo "   URL: http://192.64.87.248:8081/"
else
    echo "❌ Webhook server is not running"
fi

echo ""
echo "🔗 GitHub Webhook Status:"
WEBHOOK_ID=$(gh api repos/151henry151/listen/hooks --jq '.[0].id' 2>/dev/null)
if [ -n "$WEBHOOK_ID" ]; then
    echo "✅ GitHub webhook configured (ID: $WEBHOOK_ID)"
    echo "   Events: push"
    echo "   Branch: master"
else
    echo "❌ GitHub webhook not found"
fi

echo ""
echo "📱 APK Deployment:"
if [ -f "/home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk" ]; then
    APK_SIZE=$(du -h /home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk | cut -f1)
    APK_DATE=$(stat -c %y /home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk | cut -d' ' -f1,2)
    echo "✅ APK deployed: $APK_SIZE (updated: $APK_DATE)"
else
    echo "❌ APK not found in deployment directory"
fi

echo ""
echo "🛠️ Build Environment:"
if [ -n "$ANDROID_HOME" ]; then
    echo "✅ ANDROID_HOME: $ANDROID_HOME"
else
    echo "❌ ANDROID_HOME not set"
fi

if command -v java >/dev/null 2>&1; then
    echo "✅ Java: $(java -version 2>&1 | head -1)"
else
    echo "❌ Java not found"
fi

echo ""
echo "📋 Available Commands:"
echo "   ./auto-build-deploy.sh    - Manual build and deploy"
echo "   ./status.sh               - Show this status"
echo "   curl http://localhost:8081/ - Test webhook server"
