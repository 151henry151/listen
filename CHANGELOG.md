# Changelog

All notable changes to the Listen app will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.4] - 2025-01-14

### Fixed
- **Android 15+ Compliance with Boot Launch**: Restored BOOT_COMPLETED receiver to launch app, but service only starts after user consent
- **Proper Boot Behavior**: App now launches automatically on boot, then prompts user before starting recording service

### Changed
- **Boot Receiver Restored**: Re-enabled ListenBootReceiver to launch MainActivity on device boot
- **User Consent Required**: Service startup now requires explicit user interaction via dialog prompt
- **Best of Both Worlds**: App launches on boot (user convenience) but service only starts with user consent (Android 15+ compliance)

### Technical Changes
- **Restored Boot Receiver**: Re-enabled BOOT_COMPLETED receiver in AndroidManifest.xml
- **Launch App Only**: Boot receiver launches MainActivity with AUTO_START_AFTER_BOOT flag
- **User Prompt on Boot**: MainActivity shows consent dialog when launched from boot receiver
- **Service User-Initiated**: Service startup is triggered only when user clicks "Yes, Resume" in dialog
- **Dual Recovery Methods**: Both automatic boot launch and manual app launch recovery

---

## [1.1.3] - 2025-01-14

### Fixed
- **Complete Android 15+ Compliance**: Completely eliminated BOOT_COMPLETED receiver to resolve Google Play Console restriction
- **No More Boot Receiver**: Disabled ListenBootReceiver entirely to prevent any connection between BOOT_COMPLETED and service startup

### Changed
- **Boot Recovery Method**: Changed from automatic boot receiver to manual app launch detection
- **User Experience**: Boot recovery prompt now appears when user manually launches the app after device restart
- **Service Startup**: Service only starts when user manually launches app and chooses to resume recording

### Technical Changes
- **Disabled Boot Receiver**: Commented out BOOT_COMPLETED receiver in AndroidManifest.xml
- **Manual Recovery Check**: Added checkBootRecovery() function that runs on normal app startup
- **User-Initiated Only**: Service startup is now completely user-initiated, never triggered by system events
- **Flag Management**: Proper cleanup of wasRecordingOnShutdown flag to prevent repeated prompts

---

## [1.1.2] - 2025-01-14

### Fixed
- **Android 15+ Compliance**: Completely resolved BOOT_COMPLETED + restricted foreground service issue by requiring user interaction
- **User Prompt on Boot**: Added user prompt dialog asking if they want to resume recording after device boot instead of automatic startup

### Changed
- **Boot Startup Behavior**: Changed from automatic service startup to user-confirmed startup via dialog prompt
- **Android 15+ Compatibility**: App now fully complies with Android 15+ restrictions on foreground service startup from broadcast receivers

### Technical Changes
- **User Interaction Required**: Boot receiver now launches MainActivity which shows a dialog asking user permission to resume recording
- **Dialog Implementation**: Added `showBootStartupPrompt()` function with clear user choices
- **Service Start Only on User Consent**: Service only starts when user explicitly chooses "Yes, Resume" in the dialog
- **Non-Cancelable Dialog**: User must make a choice - no automatic dismissal

---

## [1.1.1] - 2025-01-14

### Fixed
- **Android 15+ Boot Receiver Issue**: Fixed critical crash issue where BOOT_COMPLETED receiver was directly starting restricted foreground service types, which is not allowed on Android 15+
- **Boot Startup Compatibility**: Modified boot receiver to launch MainActivity instead of directly starting the foreground service, ensuring compatibility with Android 15+ restrictions

### Technical Changes
- **Boot Receiver Refactor**: Changed `ListenBootReceiver` to launch `MainActivity` with `AUTO_START_AFTER_BOOT` flag instead of directly calling `ListenForegroundService.start()`
- **MainActivity Enhancement**: Added boot auto-start detection in `MainActivity.onCreate()` to start the service when launched from boot receiver
- **Android 15+ Compliance**: Ensures the app won't crash on Android 15+ devices due to restricted foreground service startup from broadcast receivers

---

## [1.1.0] - 2025-01-14

### Fixed
- **Intermittent Saved Recordings Display**: Fixed issue where saved recordings tab sometimes wouldn't show recordings on first access
- **Playback Failures**: Resolved playback failures on some recorded tracks with improved error handling and file validation
- **Recording During Playback**: Fixed coordination between recording and playback to prevent audio feedback loops
- **Phone Call Recording**: Improved phone call handling to continue microphone recording during speakerphone calls
- **Boot Startup**: Fixed automatic recording startup after device reboot for continuous operation

### Improved
- **Playback Reliability**: Enhanced MediaPlayer setup with robust file validation and resource cleanup
- **Service Communication**: Improved coordination between PlaybackActivity and ListenForegroundService
- **Boot Recovery**: Enhanced boot receiver logic with better permission and consent checking
- **Error Handling**: Added comprehensive error handling throughout the playback system
- **Audio Focus Management**: Improved audio focus handling during playback sessions

### Technical Improvements
- **Target SDK Update**: Updated to Android 15 (API 35) for Google Play Store compliance
- **Boot Receiver Enhancement**: Added user consent verification and reduced boot delay
- **Service Lifecycle**: Improved flag management for reliable boot recovery
- **File Validation**: Added comprehensive file existence and readability checks
- **Resource Management**: Enhanced MediaPlayer resource cleanup to prevent leaks

### System Requirements
- **Target Android Version**: Android 15 (API 35) - Updated for Google Play compliance
- **Minimum Android Version**: Android 8.0 (API 26) - Unchanged

---

## [1.0.0] - 2025-08-23

### Added
- **Core Audio Recording**: Continuous background audio recording with automatic segment rotation
- **Smart Segment Management**: Automatic creation and rotation of audio segments to prevent memory issues
- **Audio Playback**: Full-featured audio player with playback controls for saved segments
- **Background Service**: Persistent foreground service that continues recording even when app is minimized
- **Boot Auto-Start**: Automatic service restart after device reboot for continuous recording
- **Audio Quality Settings**: Configurable audio quality presets (Low, Medium, High)
- **Storage Management**: Automatic cleanup of old segments based on configurable retention settings
- **Real-time Audio Monitoring**: Live audio level visualization and monitoring
- **Phone Call Detection**: Automatic pause/resume during phone calls
- **Battery Optimization**: Smart battery management with configurable optimization settings

### Features
- **Main Dashboard**: Clean, intuitive interface showing recording status and controls
- **Segment Browser**: Easy navigation through recorded audio segments
- **Settings Panel**: Comprehensive configuration options for all app features
- **Audio Visualizer**: Real-time audio waveform display during recording
- **File Sharing**: Export and share saved audio segments
- **Dark/Light Theme**: Automatic theme adaptation based on system settings
- **Accessibility Support**: Full accessibility features for screen readers and navigation

### Technical Features
- **Room Database**: Local SQLite database for efficient segment storage and management
- **WorkManager Integration**: Reliable background task scheduling and execution
- **Coroutines**: Asynchronous programming for smooth UI performance
- **ViewBinding**: Type-safe view binding for improved development experience
- **ProGuard Optimization**: Code obfuscation and optimization for release builds
- **Material Design 3**: Modern Material Design components and theming

### Permissions
- **Microphone Access**: Required for audio recording functionality
- **Foreground Service**: Enables continuous background recording
- **Storage Access**: For saving and managing audio files
- **Phone State**: For call detection and automatic pause/resume
- **Boot Completion**: For automatic service restart after device reboot
- **Battery Optimization**: For managing power consumption

### System Requirements
- **Minimum Android Version**: Android 8.0 (API 26)
- **Target Android Version**: Android 14 (API 34)
- **Storage**: Requires external storage access for audio file management
- **Memory**: Optimized for devices with 2GB+ RAM

### Performance
- **Memory Efficient**: Automatic segment rotation prevents memory overflow
- **Battery Optimized**: Smart power management with configurable settings
- **Storage Optimized**: Automatic cleanup of old segments based on retention settings
- **Background Optimized**: Efficient foreground service with minimal resource usage

### Security & Privacy
- **Local Storage Only**: All audio data stored locally on device
- **No Cloud Upload**: No data transmitted to external servers
- **Permission Transparency**: Clear explanation of all required permissions
- **Data Control**: User has full control over recorded audio segments

---

## Version History

### Version 1.0.0 (Initial Release)
- **Release Date**: August 23, 2025
- **Package Name**: com.romp.listen.app
- **Build Type**: Release (Signed AAB)
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 26 (Android 8.0)

### Key Features Summary
1. **Continuous Audio Recording**: 24/7 background audio recording capability
2. **Smart Segment Management**: Automatic audio segment creation and rotation
3. **Comprehensive Playback**: Full-featured audio player with all standard controls
4. **Intelligent Pause/Resume**: Automatic handling of phone calls and system events
5. **User-Friendly Interface**: Clean, modern UI following Material Design principles
6. **Robust Background Service**: Reliable foreground service for continuous operation
7. **Flexible Configuration**: Extensive settings for audio quality, storage, and behavior
8. **Data Privacy**: Complete local storage with no external data transmission

### Installation
- Available as Android App Bundle (AAB) for Google Play Store distribution
- Requires Android 8.0 or higher
- Approximate download size: 2.6 MB
- Installation size: ~15 MB (varies by device)

### Support
- Designed for continuous audio recording and monitoring
- Ideal for security, monitoring, and audio documentation purposes
- Optimized for long-term background operation
- Suitable for both personal and professional use cases

---

*This changelog will be updated with each new release to document all changes, improvements, and new features.* 