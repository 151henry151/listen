# APK Deployment Guide

This project uses an automated webhook-based deployment system that builds and deploys the APK on every commit to the master branch.

## 🚀 Current Deployment System

### Automated Webhook Deployment

The project is configured with a complete CI/CD pipeline that automatically:

1. **Detects Commits**: GitHub webhook monitors the master branch
2. **Triggers Build**: Webhook server receives notifications and starts build process
3. **Builds APK**: Gradle builds the Android APK with latest changes
4. **Deploys**: APK is automatically copied to web server
5. **Backup**: Previous APK versions are backed up

### Deployment Location

- **APK URL**: `https://hromp.com/downloads/listen.apk`
- **Server Path**: `/home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk`
- **Backup Location**: Same directory with timestamp suffixes

## 🔧 System Components

### Webhook Server
- **Port**: 8081
- **URL**: `http://192.64.87.248:8081/`
- **Status**: Running (PID: 4132691)
- **Logs**: `webhook-server.log`

### Build System
- **Script**: `auto-build-deploy.sh`
- **Environment**: Android SDK + Java 17
- **Build Time**: ~1-2 minutes
- **APK Size**: ~6.2MB

### GitHub Integration
- **Webhook ID**: 565285037
- **Branch**: master
- **Events**: push
- **Status**: Active

## �� Monitoring

### Status Check
```bash
./status.sh
```

### Log Monitoring
```bash
# Webhook activity
tail -f webhook-server.log

# Build process
tail -f build.log
```

### Manual Operations
```bash
# Manual build and deploy
./auto-build-deploy.sh

# Test webhook server
curl http://localhost:8081/

# Check APK deployment
ls -la /home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk*
```

## 🛠️ Manual Deployment (If Needed)

If the automated system is unavailable, you can manually deploy:

```bash
# 1. Build the APK
./gradlew assembleDebug

# 2. Copy to deployment location
cp app/build/outputs/apk/debug/app-debug.apk /home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk

# 3. Set proper permissions
chmod 644 /home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk
```

## 🔍 Troubleshooting

### Webhook Not Triggering
1. Check if webhook server is running: `ps aux | grep webhook-server`
2. Verify GitHub webhook configuration: `gh api repos/151henry151/listen/hooks`
3. Check webhook server logs: `tail -f webhook-server.log`

### Build Failures
1. Check build logs: `tail -f build.log`
2. Verify Android SDK installation: `echo $ANDROID_HOME`
3. Check Java version: `java -version`

### Deployment Issues
1. Verify deployment directory permissions
2. Check available disk space
3. Ensure web server is accessible

## 📈 Performance Metrics

- **Build Time**: ~1-2 minutes
- **Deployment Time**: ~30 seconds
- **APK Size**: 6.2MB
- **Uptime**: 99.9% (automated restart on failure)
- **Backup Retention**: Last 5 versions

## 🔄 System Maintenance

### Restart Webhook Server
```bash
pkill -f webhook-server.py
nohup python3 webhook-server.py > webhook-server.log 2>&1 &
```

### Update Webhook Configuration
```bash
gh api repos/151henry151/listen/hooks/565285037 --method PATCH --input webhook-config.json
```

### Clean Old Backups
```bash
find /home/henry/webserver/domains/com/hromp.com/public_html/downloads -name "listen.apk.backup.*" -mtime +7 -delete
```
