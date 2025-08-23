#!/bin/bash

# Automated build and deploy script for Listen app
# This script monitors the git repo and builds/deploys APK on new commits

set -e  # Exit on any error

# Configuration
REPO_DIR="/home/henry/listen"
DEPLOY_DIR="/home/henry/listen-apk-deploy"
APK_NAME="listen.apk"
LOG_FILE="/home/henry/listen/build.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1" | tee -a "$LOG_FILE"
}

success() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS:${NC} $1" | tee -a "$LOG_FILE"
}

warning() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1" | tee -a "$LOG_FILE"
}

# Function to check if there are new commits
check_for_new_commits() {
    cd "$REPO_DIR"
    
    # Fetch latest changes
    git fetch origin
    
    # Get current and remote commit hashes
    LOCAL_COMMIT=$(git rev-parse HEAD)
    REMOTE_COMMIT=$(git rev-parse origin/master)
    
    if [ "$LOCAL_COMMIT" != "$REMOTE_COMMIT" ]; then
        log "New commits detected! Local: ${LOCAL_COMMIT:0:8}, Remote: ${REMOTE_COMMIT:0:8}"
        return 0
    else
        log "No new commits detected"
        return 1
    fi
}

# Function to prepare repository for pull (stash gradle artifacts)
prepare_for_pull() {
    cd "$REPO_DIR"
    
    # Check if there are any uncommitted changes in gradle directory
    if git status --porcelain | grep -q "^ M .gradle/"; then
        log "Stashing gradle build artifacts to prevent merge conflicts..."
        git stash push -m "Auto-stash: Gradle build artifacts $(date '+%Y-%m-%d %H:%M:%S')" .gradle/
        return 0
    fi
    
    return 0
}

# Function to build and deploy APK
build_and_deploy() {
    cd "$REPO_DIR"
    
    log "Starting build and deploy process..."
    
    # Prepare repository for pull (stash gradle artifacts if needed)
    prepare_for_pull
    
    # Pull latest changes
    log "Pulling latest changes..."
    if ! git pull origin master; then
        error "Failed to pull latest changes from remote. Cannot proceed with build."
        return 1
    fi
    
    # Verify we're on the latest commit
    CURRENT_COMMIT=$(git rev-parse HEAD)
    log "Building APK with commit: ${CURRENT_COMMIT:0:8}"
    
    # Set up environment
    export ANDROID_HOME=$HOME/android-sdk
    export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
    
    # Clean and build
    log "Building APK..."
    ./gradlew clean assembleDebug
    
    # Check if build was successful
    if [ $? -eq 0 ] && [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        # Get APK info
        APK_SIZE=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
        log "Build successful! APK size: $APK_SIZE"
        
        # Create backup of current APK
        if [ -f "$DEPLOY_DIR/$APK_NAME" ]; then
            log "Creating backup of current APK..."
            cp "$DEPLOY_DIR/$APK_NAME" "$DEPLOY_DIR/${APK_NAME}.backup.$(date +%Y%m%d_%H%M%S)"
        fi
        
        # Deploy new APK
        log "Deploying APK to $DEPLOY_DIR/$APK_NAME..."
        cp app/build/outputs/apk/debug/app-debug.apk "$DEPLOY_DIR/$APK_NAME"
        
        # Set proper permissions
        chmod 644 "$DEPLOY_DIR/$APK_NAME"
        
        success "APK successfully deployed! New size: $APK_SIZE"
        
        # Clean up old backups (keep last 5)
        find "$DEPLOY_DIR" -name "${APK_NAME}.backup.*" -type f | sort | head -n -5 | xargs -r rm
        
        return 0
    else
        error "Build failed!"
        return 1
    fi
}

# Main execution
main() {
    log "=== Listen Auto Build & Deploy Script Started ==="
    
    # Check if required directories exist
    if [ ! -d "$REPO_DIR" ]; then
        error "Repository directory not found: $REPO_DIR"
        exit 1
    fi
    
    if [ ! -d "$DEPLOY_DIR" ]; then
        error "Deploy directory not found: $DEPLOY_DIR"
        exit 1
    fi
    
    # Check for new commits
    if check_for_new_commits; then
        log "New commits found, starting build process..."
        if build_and_deploy; then
            success "Build and deploy completed successfully!"
        else
            error "Build and deploy failed!"
            exit 1
        fi
    else
        log "No new commits, nothing to do"
    fi
    
    log "=== Script completed ==="
}

# Run main function
main "$@"
