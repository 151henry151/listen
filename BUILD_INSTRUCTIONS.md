# Building the Listen APK

## Prerequisites

1. **Android SDK** - You need the Android SDK installed on your system
2. **Java JDK** - Java 11 or higher is required

## Quick Build

To build the APK, you have several options:

### Option 1: Using the Build Script (Recommended)
```bash
./build-apk.sh
```

This script will:
- Automatically detect Android SDK location
- Clean previous builds
- Build a debug APK
- Show the APK location and size

### Option 2: Using Gradle Directly
```bash
# Set Android SDK location
export ANDROID_HOME=/path/to/your/android-sdk

# Build debug APK
./gradlew assembleDebug
```

### Option 3: Build Release APK (Requires signing)
```bash
./gradlew assembleRelease
```

## APK Output Location

After a successful build, the APK will be located at:
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`

## Installing the APK

### On a Connected Device/Emulator
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Manual Installation
1. Copy the APK to your Android device
2. Enable "Install from Unknown Sources" in your device settings
3. Open the APK file on your device to install

## Troubleshooting

### SDK Location Not Found
If you get an error about SDK location:
1. Set the `ANDROID_HOME` environment variable:
   ```bash
   export ANDROID_HOME=/path/to/android-sdk
   ```
2. Or create a `local.properties` file:
   ```bash
   echo "sdk.dir=/path/to/android-sdk" > local.properties
   ```

### Build Failures
- Ensure you have Java 11 or higher installed
- Check that all required Android SDK components are installed
- Run `./gradlew clean` before building again

## Development Mode

For continuous development with hot-reload:
```bash
./dev.sh
```

This will watch for file changes and automatically rebuild and install the app.