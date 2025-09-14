# Listen App - Current Issues

## Issue 1: Intermittent Saved Recordings Display
**Priority**: High  
**Status**: ✅ RESOLVED  
**Description**: Sometimes the "saved recordings" tab doesn't show any recordings when first accessed, even though there are saved recordings. This is intermittent and seems to occur "when you first go to the tab". The issue can sometimes be resolved by:
- Clicking back to the rotating recordings tab and then back to the saved tab
- Performing other actions and returning to the saved recordings tab

**Expected Behavior**: The saved recordings tab should consistently display all saved recordings when accessed.

**Technical Details**: The issue was related to fragment lifecycle and asynchronous loading of saved segments. The `loadSavedSegments()` function runs asynchronously and the fragment might not be properly updated when the tab is first accessed.

**Resolution**: Added a ViewPager tab change listener that refreshes saved segments when the saved recordings tab is accessed. Also improved the fragment update mechanism with retry logic for cases where the fragment isn't immediately available.

## Issue 2: Playback Failures
**Priority**: High  
**Status**: ✅ RESOLVED  
**Description**: Sometimes playback will fail on some recorded tracks. The failure appears to be intermittent and not consistent across all recordings.

**Expected Behavior**: All saved recordings should play back reliably without failures.

**Technical Details**: The MediaPlayer setup had race conditions and insufficient error handling. The playback implementation in `PlaybackActivity.playSavedSegment()` and `playSegment()` needed improvements.

**Resolution**: Enhanced playback implementation with:
- Comprehensive file validation (existence, size, readability)
- Proper MediaPlayer cleanup and race condition prevention
- Better error handling with user feedback
- Completion listener to handle playback end
- Improved audio focus management

## Issue 3: Recording During Playback
**Priority**: High  
**Status**: ✅ RESOLVED  
**Description**: The app needs to handle the scenario where recording is in progress and the user tries to play back an old recording. Currently, this could result in recordings of other recordings playing back, creating a loop effect.

**Required Behavior**:
- When starting playback of an old recording while recording is in progress, the current recording should be ended
- When stopping playback of an old recording, a new recording segment should begin
- This prevents recordings of recordings and maintains clean audio segments

**Technical Details**: There was no coordination between the `ListenForegroundService` (recording) and `PlaybackActivity` (playback). They operated independently, which could lead to audio feedback loops.

**Resolution**: Implemented coordination between recording and playback:
- Added new service commands: `ACTION_PAUSE_RECORDING_FOR_PLAYBACK` and `ACTION_RESUME_RECORDING_AFTER_PLAYBACK`
- Modified playback functions to pause recording before starting playback
- Modified stopPlayback function to resume recording after playback ends
- Added state tracking to ensure proper coordination

## Issue 4: Phone Call Recording
**Priority**: Medium  
**Status**: ✅ RESOLVED  
**Description**: When a phone call starts, the app should continue recording from the normal microphone to capture speakerphone conversations.

**Expected Behavior**: 
- Recording should continue during phone calls
- Audio should be captured from the normal microphone (not call audio)
- This allows capture of speakerphone conversations

**Technical Details**: The service had phone call detection via `PhoneStateListener` but was incorrectly stopping recording during calls and trying to record call audio directly, which is restricted on modern Android.

**Resolution**: Modified phone call handling to:
- Continue microphone recording during calls instead of stopping it
- Use the existing `MediaRecorder.AudioSource.MIC` which captures ambient audio including speakerphone conversations
- Update notification to indicate recording during calls
- Remove the problematic call recording logic that was causing issues

---

## Summary
✅ **All Issues Resolved**

All four major issues have been successfully addressed:
1. ✅ Intermittent saved recordings display - Fixed with tab change listener and improved fragment handling
2. ✅ Playback failures - Enhanced with comprehensive error handling and validation
3. ✅ Recording/playback coordination - Implemented service communication to prevent audio feedback loops
4. ✅ Phone call recording - Modified to continue microphone recording for speakerphone capture

**Additional Improvements:**
- Updated target SDK to API level 35 (Android 15) for Google Play Store compliance
- Enhanced error handling throughout the playback system
- Improved audio focus management
- Better user feedback for errors

The app should now be much more reliable and handle edge cases properly.

---

## Issue 5: Boot Startup Not Working
**Priority**: High  
**Status**: ✅ RESOLVED  
**Description**: The app doesn't reliably start recording automatically when the device boots up, even when it was recording before shutdown.

**Expected Behavior**: 
- When the app is recording and the device is turned off/restarted
- The app should automatically start recording when the device boots up
- No user intervention should be required

**Technical Details**: The boot startup mechanism was in place but had several issues:
- Boot receiver didn't check user consent before starting
- Service startup logic only set `wasRecordingOnShutdown` flag if recording succeeded
- Boot delay was too long (30 seconds) and could be killed by system
- Insufficient debugging information

**Resolution**: Fixed boot startup mechanism:
- Added user consent check in boot receiver before auto-starting
- Modified service startup to set `wasRecordingOnShutdown` flag regardless of initial recording success
- Reduced boot delay from 30 seconds to 10 seconds
- Added comprehensive debugging logs for troubleshooting
- Ensured proper flag management in service lifecycle

**Files Modified**:
- `ListenBootReceiver.kt` - Added consent check, reduced delay, improved logging
- `ListenForegroundService.kt` - Fixed flag setting logic in service startup
