# Listen - Background Audio Recording App

A sophisticated Android application designed to continuously record audio in the background, providing users with a rolling window of audio history for review and analysis.

## 🎯 Purpose

Listen is a background-listening app that captures ambient audio from your surroundings and maintains a configurable rolling buffer of recordings. Perfect for situations where you need to review recent conversations, sounds, or audio events that occurred in the past few minutes.

## ✨ Key Features

- **Continuous Background Recording**: Runs silently in the background, capturing audio 24/7
- **Configurable Segment Length**: Adjustable recording segment duration (e.g., 1 minute, 30 seconds, 5 minutes)
- **Auto Music Mode**: Intelligent segment splitting that detects natural silence breaks and targets ~5-minute segments for optimal content organization
- **Customizable Retention Period**: Set how long to keep audio history (e.g., 10 minutes, 1 hour, 24 hours)
- **Rotating Buffer System**: Automatically manages storage by deleting old segments and adding new ones
- **Instant Playback**: Access any moment from your audio history with just a few taps
- **Privacy-First Design**: All recordings stored locally on device, no cloud uploads
- **Battery Optimized**: Efficient background processing with minimal battery impact
- **Audio Quality Presets**: Choose from Low (16 kbps), Medium (32 kbps), or High (128 kbps) quality settings
- **Automatic Build & Deployment**: Automated CI/CD pipeline that builds and deploys APK on every commit
- **Export Functionality**: Export audio segments in various formats
- **WorkManager Integration**: Optimized segment rotation with WorkManager scheduling
- **Enhanced Error Handling**: Comprehensive error handling and user feedback systems

## 🔧 How It Works

1. **Background Service**: The app runs a persistent background service that continuously captures audio from the device microphone
2. **Segmented Recording**: Audio is divided into user-configurable time segments (e.g., 1-minute chunks)
3. **Rotating Buffer**: When the retention period is reached, the oldest segment is automatically deleted and replaced with the newest recording
4. **Always Available**: At any moment, you have access to the complete audio history within your retention window

### Example Configurations

**Fixed Duration Mode**:
- **Segment Length**: 1 minute
- **Retention Period**: 10 minutes
- **Result**: Always have the last 10 minutes of audio available, divided into 10 one-minute segments

**Auto Music Mode**:
- **Target Duration**: ~5 minutes (adaptive)
- **Retention Period**: 30 minutes
- **Result**: Audio automatically split at natural silence breaks, typically around 5 minutes, perfect for podcasts, meetings, and music sessions

## 🚀 Use Cases

- **Meeting Review**: Check what was said in a meeting you just left
- **Conversation Recall**: Review conversations that happened while you were distracted
- **Sound Investigation**: Identify what caused a noise you heard earlier
- **Learning Aid**: Review lectures or presentations you attended
- **Podcast/Content Review**: Perfect for reviewing long-form content with auto music mode
- **Security Monitoring**: Keep an audio log of your surroundings
- **Memory Assistance**: Help with recall of recent events or conversations

## 🛠️ Technical Architecture

### Core Components
- **ListenForegroundService**: Main background service for continuous audio capture and orchestration
- **AudioRecorderService**: Handles audio recording with MediaRecorder
- **SegmentManagerService**: Manages segment creation, rotation, and deletion
- **StorageManager**: Manages local file storage and cleanup
- **SettingsManager**: User preferences and configuration management
- **ListenDatabase**: Room database for segment metadata storage
- **ServiceHealthMonitor**: Monitors service health and handles restarts
- **ServiceStateManager**: Manages service state transitions

### Audio Processing
- **Format**: High-quality audio recording with configurable bitrate (16-128 kbps)
- **Compression**: Efficient storage with minimal quality loss
- **Segmentation**: Precise time-based audio splitting
- **Metadata**: Timestamp and duration tracking for each segment

### Quality Presets
- **Low Quality**: 16 kbps, 8 kHz (telephone quality) - Best for battery life and storage
- **Medium Quality**: 32 kbps, 16 kHz (standard quality) - Good balance of quality and efficiency  
- **High Quality**: 128 kbps, 44.1 kHz (CD quality) - Best audio fidelity, uses more storage

## 📱 User Interface

- **Main Dashboard**: Overview of current recording status, storage usage, and service controls
- **Playback Activity**: Audio player with segment navigation and timeline view
- **Settings Panel**: Easy configuration of recording parameters and audio quality
- **Real-time Status**: Live updates of recording status and elapsed time
- **Audio Visualizer**: Visual representation of audio levels during recording

## 🔒 Privacy & Security

- **Local Storage Only**: All recordings remain on your device
- **No Network Access**: App doesn't require internet connection
- **Permission Control**: User grants explicit permission for microphone access
- **Data Encryption**: Optional encryption for sensitive recordings
- **Easy Deletion**: One-tap removal of all recorded data

## ⚙️ Configuration Options

### Recording Settings
- Segment duration (15 seconds to 30 minutes)
- Auto music mode (adaptive ~5-minute segments with silence detection)
- Retention period (1 minute to 24 hours)
- Audio quality presets (Low, Medium, High)
- Microphone sensitivity

### Storage Settings
- Maximum storage allocation
- Auto-cleanup thresholds
- Export options (MP3, WAV, etc.)

### Background Settings
- Battery optimization preferences
- Notification controls
- Auto-start options

## 📋 Requirements

- **Android Version**: Android 8.0 (API 26) or higher
- **Permissions**: Microphone access, background processing, notification access
- **Storage**: Minimum 100MB free space (varies by retention settings)
- **Hardware**: Microphone support

## 🧑‍💻 Development

### Build Environment
- **Android Studio**: Giraffe or newer
- **SDK**: API 26-34
- **Java**: JDK 17
- **Kotlin**: Latest stable version

### Build locally
- Install Android Studio with SDK 26–34
- Set `sdk.dir` in `local.properties`
- Build and run:
  - From IDE: Run the `app` configuration
  - CLI: `./gradlew assembleDebug`

### Automated Build & Deployment
The project includes a complete CI/CD pipeline:
- **Webhook Server**: Automatically triggers builds on GitHub commits
- **Build Script**: `./auto-build-deploy.sh` - Automated APK building and deployment
- **Deployment**: APK automatically deployed to web server on every commit
- **Status Monitoring**: `./status.sh` - System status and health checks

### Logging
- Debug builds use Timber; logs routed via `AppLog` wrapper
- Release builds silence logs through `AppLog` no-op behavior
- Debug logs written to `debug_log.txt` for troubleshooting

### StrictMode (debug)
- StrictMode is enabled in debug builds via `ListenApplication` to catch disk/network on main thread and leaks early

### Linting & Static Analysis
- Android Lint: `./gradlew lintDebug`
- Detekt: `./gradlew detekt`
- Ktlint: `./gradlew ktlintCheck`

### CI
- GitHub Actions builds on push/PR to `master`:
  - Assemble debug, run unit tests, Android Lint
  - Optionally runs Detekt and Ktlint if configured

## 🚧 Development Status

This project is in active development with comprehensive functionality implemented and operational.

### ✅ Implemented Features
- [x] Project setup and basic architecture
- [x] Core audio recording service with background operation
- [x] Segment management system with database storage
- [x] Main user interface with dashboard and controls
- [x] Playback functionality with segment navigation
- [x] Settings and configuration management
- [x] Audio quality presets (Low, Medium, High)
- [x] Storage management and cleanup
- [x] Permission handling for Android 8.0+
- [x] Automated build and deployment system
- [x] Service health monitoring and restart capabilities
- [x] WorkManager-based segment rotation optimization
- [x] Enhanced error handling and user feedback systems
- [x] Export functionality for audio segments
- [x] Performance optimization and battery efficiency improvements
- [x] Audio visualizer for recording feedback
- [x] Comprehensive logging and debugging capabilities
- [x] Boot receiver for auto-start functionality
- [x] Battery optimization handling

## 📚 Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) - Technical architecture and design details
- [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) - Detailed build and deployment instructions
- [DEPLOYMENT.md](DEPLOYMENT.md) - Production deployment guidelines
- [PRODUCTION_ISSUES.md](PRODUCTION_ISSUES.md) - Known issues and production readiness tasks
- [AUTOMATION.md](AUTOMATION.md) - Automated build and deployment system documentation

## 🤝 Contributing

This is a personal project, but suggestions and feedback are welcome. Please ensure any contributions align with the project's privacy-first philosophy.

## 📄 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

The GPL v3.0 ensures that:
- This software remains free and open source
- Any modifications must also be released under the GPL
- Users have the right to view, modify, and distribute the source code
- The software comes with no warranty

## 📞 Support

For questions, suggestions, or issues, please open an issue in this repository.

---

**Note**: This app is designed for personal use and legitimate recording purposes. Users are responsible for complying with local laws regarding audio recording and privacy. Always respect others' privacy and obtain consent when required.
# Test commit for automated build - Thu Aug 21 08:15:56 PM EDT 2025
