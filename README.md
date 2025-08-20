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
- **AudioRecorderService**: Background service for continuous audio capture
- **AudioSegmentManager**: Handles segment creation, rotation, and deletion
- **StorageManager**: Manages local file storage and cleanup
- **PlaybackManager**: Handles audio playback and navigation
- **SettingsManager**: User preferences and configuration

### Audio Processing
- **Format**: High-quality audio recording with configurable bitrate
- **Compression**: Efficient storage with minimal quality loss
- **Segmentation**: Precise time-based audio splitting
- **Metadata**: Timestamp and duration tracking for each segment

## 📱 User Interface

- **Main Dashboard**: Overview of current recording status and available segments
- **Timeline View**: Visual representation of audio history with time markers
- **Playback Controls**: Intuitive audio player with segment navigation
- **Settings Panel**: Easy configuration of recording parameters
- **Quick Access**: Widget support for instant audio review

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
- Audio quality (bitrate, sample rate)
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
- **Permissions**: Microphone access, background processing
- **Storage**: Minimum 100MB free space (varies by retention settings)
- **Hardware**: Microphone support

## 🧑‍💻 Development

### Build locally
- Install Android Studio (Giraffe or newer) with SDK 26–34
- Set `sdk.dir` in `local.properties`
- Build and run:
  - From IDE: Run the `app` configuration
  - CLI: `./gradlew assembleDebug`

### Logging
- Debug builds use Timber; logs routed via `AppLog` wrapper
- Release builds silence logs through `AppLog` no-op behavior

### StrictMode (debug)
- StrictMode is enabled in debug builds via `ListenApplication` to catch disk/network on main thread and leaks early

### Linting & Static Analysis
- Android Lint: `./gradlew lintDebug`
- Detekt: `./gradlew detekt`
- Ktlint: `./gradlew ktlintCheck`

### CI
- GitHub Actions builds on push/PR to `main`:
  - Assemble debug, run unit tests, Android Lint
  - Optionally runs Detekt and Ktlint if configured

## 🚧 Development Status

This project is currently in the initial planning and development phase.

### Planned Milestones
- [ ] Project setup and basic architecture
- [ ] Core audio recording service
- [ ] Segment management system
- [ ] Basic user interface
- [ ] Playback functionality
- [ ] Settings and configuration
- [ ] Testing and optimization
- [ ] Release preparation

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

**Note**: This app is designed for personal use and legitimate recording purposes. Users are responsible for complying with local laws regarding audio recording and privacy. Always respect others' privacy and obtain consent when recording in shared spaces. 