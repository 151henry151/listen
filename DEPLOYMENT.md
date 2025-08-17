# APK Deployment Guide

This project supports automated APK deployment to your web server.

## 🚀 Deployment Options

### Option 1: GitHub Actions (Recommended)

**Automatic deployment on every push to master branch**

#### Setup Steps:

1. **Add GitHub Secrets** (in your GitHub repo settings):
   - Go to Settings > Secrets and variables > Actions
   - Add these secrets:
     - `HOST`: `romptele.com`
     - `USERNAME`: `henry`
     - `SSH_KEY`: Your private SSH key content

2. **Get your SSH key** (if you don't have one):
   ```bash
   ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
   ```

3. **Add public key to server**:
   ```bash
   ssh-copy-id henry@romptele.com
   ```

4. **Test sudo access** (make sure you can run sudo without password):
   ```bash
   ssh henry@romptele.com "sudo echo 'Sudo access works'"
   ```

5. **Push to GitHub** - The workflow will automatically:
   - Build the APK
   - Deploy it to your server
   - Make it available at `https://hromp.com/downloads/listen.apk`

### Option 2: Local Script

**Manual deployment using local script**

#### Usage:
```bash
./deploy.sh
```

This will:
- Build the APK locally
- Upload it to your server
- Set proper permissions
- Make it available at `https://hromp.com/downloads/listen.apk`

## 🔧 Server Setup

### Sudo Access Without Password

To avoid entering sudo passwords, add this line to your server's sudoers file:

```bash
# On your server, run: sudo visudo
# Add this line:
henry ALL=(ALL) NOPASSWD: /bin/mv /tmp/listen.apk /home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk, /bin/chmod 644 /home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk, /bin/chown root:root /home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk
```

### Alternative: Use a Deployment User

Create a dedicated deployment user with limited sudo access:

```bash
# On your server
sudo useradd -m -s /bin/bash deploy
sudo usermod -aG sudo deploy
# Add to sudoers with specific commands only
```

## 📱 APK Access

Once deployed, your APK will be available at:
- **URL**: `https://hromp.com/downloads/listen.apk`
- **Direct download**: Users can download directly from this URL

## 🔄 Workflow

### GitHub Actions Workflow:
1. Push to `master` branch
2. GitHub Actions automatically:
   - Sets up Android build environment
   - Builds the APK
   - Uploads to your server
   - Sets proper permissions

### Local Deployment:
1. Run `./deploy.sh`
2. Script handles everything automatically

## 🛠️ Troubleshooting

### Common Issues:

1. **SSH Key Issues**:
   ```bash
   # Test SSH connection
   ssh henry@romptele.com
   ```

2. **Sudo Permission Issues**:
   ```bash
   # Test sudo access
   ssh henry@romptele.com "sudo echo 'test'"
   ```

3. **Build Failures**:
   - Check GitHub Actions logs
   - Ensure Android SDK is properly configured

4. **Deployment Failures**:
   - Check server disk space
   - Verify file permissions
   - Check network connectivity

## 📋 Manual Commands

If you need to deploy manually:

```bash
# Build APK
export ANDROID_HOME=/home/henry/android-sdk
./gradlew assembleDebug

# Deploy to server
scp app/build/outputs/apk/debug/app-debug.apk henry@romptele.com:/tmp/listen.apk

# Set permissions on server
ssh henry@romptele.com
sudo mv /tmp/listen.apk /home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk
sudo chmod 644 /home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk
sudo chown root:root /home/henry/webserver/domains/com/hromp.com/public_html/downloads/listen.apk
``` 