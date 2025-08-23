# Listen App Auto-Build System

This directory contains the automated build and deployment system for the Listen Android app.

## Components

### Core Scripts
- `auto-build-deploy.sh` - Main build and deployment script
- `webhook-server.py` - GitHub webhook server (runs on port 8081)
- `status.sh` - System status checker

### Configuration
- `webhook-config.json` - GitHub webhook configuration
- `listen-webhook.service` - Systemd service file for auto-start

## How It Works

1. **GitHub Webhook**: When a commit is pushed to the `master` branch, GitHub sends a webhook to our server
2. **Webhook Server**: Receives the webhook and triggers the build process
3. **Build Process**: 
   - Pulls latest changes from git
   - Builds the Android APK using Gradle
   - Deploys the APK to `/home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk`
4. **Backup**: Creates backups of previous APK versions

## Setup

### Prerequisites
- Android SDK installed at `/home/henry/android-sdk`
- Java JDK 17+
- Python 3
- GitHub CLI (`gh`)

### Installation
1. Ensure the webhook server is running: `nohup python3 webhook-server.py > webhook-server.log 2>&1 &`
2. GitHub webhook is configured to call `http://192.64.87.248:8081/`
3. Webhook listens for push events on the `master` branch

### Manual Commands
- `./status.sh` - Check system status
- `./auto-build-deploy.sh` - Manual build and deploy
- `curl http://localhost:8081/` - Test webhook server

## Monitoring

Check the logs:
- Webhook server: `tail -f webhook-server.log`
- Build process: `tail -f build.log`

## Current Status

✅ **Fully Operational**
- Webhook server running (PID: 4132691)
- GitHub webhook configured (ID: 565285037)
- Build environment ready (Android SDK + Java 17)
- APK successfully deployed (6.2MB)
- Automated builds triggered on every commit

## Troubleshooting

1. **Webhook not triggering**: Check if webhook server is running with `./status.sh`
2. **Build failures**: Check `build.log` for detailed error messages
3. **APK not updating**: Verify deployment directory permissions

## Recent Activity

The system was successfully tested and is fully operational:
- ✅ Webhook detected new commit (b6b00cc1)
- ✅ Build process started automatically
- ✅ APK built successfully (6.2MB)
- ✅ APK deployed to downloads directory
- ✅ Backup system working correctly
