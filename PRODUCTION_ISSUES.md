## Listen App — Production Readiness Backlog (GitHub Issue Seeds)

This document lists all identified gaps, risks, and enhancements required to bring the app to a production-ready state. Each subsection can be turned into a GitHub issue. Severity scale: P0 (critical) → P3 (nice-to-have).

---

### P0 — Critical Functionality & Stability

- **P0: Replace WorkManager-based segment rotation (invalid period) with in-service scheduler**
  - **Problem**: Segment rotation uses `WorkManager` with a seconds-level interval, which is invalid (min period ~15 minutes). Rotation is effectively non-functional.
  - **Impact**: No rolling buffer; segments won’t rotate as intended.
  - **Tasks**:
    - [ ] Implement an in-service scheduler (e.g., coroutine/Handler timer) inside `ListenForegroundService` to rotate segments every `SettingsManager.segmentDurationSeconds`.
    - [ ] Remove or gate WorkManager usage for rotation.
    - [ ] Ensure rotation is resilient across service restarts.
  - **Acceptance**: Segments rotate at the configured interval (e.g., 60s), verified via logs and DB entries.
  - **Refs**: `app/src/main/java/com/listen/app/service/ListenForegroundService.kt` (scheduleSegmentRotation 171–193), `app/src/main/java/com/listen/app/worker/SegmentRotationWorker.kt`.

- **P0: Fix WorkManager initialization (currently disabled in Manifest)**
  - **Problem**: Manifest removes `WorkManagerInitializer` but no manual initialization is provided.
  - **Impact**: Any WorkManager call throws “WorkManager is not initialized properly”.
  - **Tasks**:
    - [ ] Either re-enable auto-init by removing the manifest override or
    - [ ] Add an `Application` subclass to initialize WorkManager manually with a custom `Configuration`.
  - **Acceptance**: `WorkManager.getInstance(context)` returns without error; scheduled tasks run if used.
  - **Refs**: `app/src/main/AndroidManifest.xml` (provider removing initializer), `ListenForegroundService.kt` (uses WorkManager).

- **P0: Correct service restart on task removal for Android O+**
  - **Problem**: `onTaskRemoved` calls `startService(...)` on O+, which can crash; should use `startForegroundService(...)`.
  - **Impact**: Service may fail to restart reliably.
  - **Tasks**:
    - [ ] Use version-aware restart (O+ → `startForegroundService`).
    - [ ] Add telemetry/logs on restart success/failure.
  - **Acceptance**: No crashes on task removal; service restarts and stays foreground.
  - **Refs**: `app/src/main/java/com/listen/app/service/ListenForegroundService.kt` (onTaskRemoved 99–104).

- **P0: Complete audio segment pipeline (rotation → DB insert → cleanup)**
  - **Problem**: Rotation isn’t triggered; DB insert only happens on stop.
  - **Impact**: Rolling buffer doesn’t exist; segmentation logic isn’t exercised.
  - **Tasks**:
    - [ ] Trigger rotation on the configured cadence.
    - [ ] Verify `onSegmentCompleted` inserts a `Segment` record with correct timestamps and size.
    - [ ] Confirm `SegmentManagerService.performCleanup()` removes old segments beyond retention and enforces storage limits.
  - **Acceptance**: Continuous segment creation and automatic cleanup observed over multiple rotations.
  - **Refs**: `AudioRecorderService.kt`, `SegmentManagerService.kt`.

- **P0: Add missing runtime error handling and user messaging for recorder failures**
  - **Problem**: MediaRecorder failures not surfaced to UI; no retry/backoff.
  - **Impact**: Silent failure; no recording.
  - **Tasks**:
    - [ ] Emit user-visible status when recording fails.
    - [ ] Add bounded retry strategy with jitter and clear fallbacks.
  - **Acceptance**: Failures visible in UI and logs; auto-recovery attempted safely.
  - **Refs**: `AudioRecorderService.kt` (start/stop exceptions).

---

### P1 — Core UX, Settings, and Permissions

- **P1: Implement Settings screen**
  - **Problem**: `openSettings()` is a stub.
  - **Tasks**:
    - [ ] Create `SettingsActivity` with controls for segment duration, retention, bitrate, sample rate, max storage, boot auto-start, notification visibility.
    - [ ] Persist via `SettingsManager` and apply to service (hot-apply when feasible).
  - **Acceptance**: Settings can be changed and take effect without app restart.
  - **Refs**: `app/src/main/java/com/listen/app/ui/MainActivity.kt` (231–235), `SettingsManager.kt`.

- **P1: Complete Playback UI and logic**
  - **Problem**: Playback screen lacks binding and list; controls are non-functional.
  - **Tasks**:
    - [ ] Enable ViewBinding in `PlaybackActivity`.
    - [ ] Implement a `RecyclerView` adapter to display segments from DB.
    - [ ] Create `res/layout/item_segment.xml` for list rows (time, duration, size) and bind click to play.
    - [ ] Wire play/pause/stop/seek; update labels and seek bar.
  - **Acceptance**: User can select and play any segment; UI reflects playback state.
  - **Refs**: `app/src/main/java/com/listen/app/ui/PlaybackActivity.kt`, `res/layout/activity_playback.xml`, `res/layout/item_segment.xml`. 

- **P1: Provide microphone permission rationale dialog (custom UI)**
  - **Problem**: TODO placeholder for rationale.
  - **Tasks**:
    - [ ] Show a modal explaining why `RECORD_AUDIO` is required, with links to privacy policy.
    - [ ] Request permission after user acknowledgment.
  - **Acceptance**: Users see rationale when required; permission flow is smooth.
  - **Refs**: `MainActivity.kt` (139–150).

- **P1: Battery optimization handling**
  - **Problem**: Permission declared, but no flow to request exemption / educate users.
  - **Tasks**:
    - [ ] Detect Doze/battery optimization status and prompt for `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.
    - [ ] Add help UI explaining impact on background recording.
  - **Acceptance**: Clear prompt and navigation to system settings; state reflected in UI.
  - **Refs**: `AndroidManifest.xml` (permissions), `MainActivity.kt`.

- **P1: Add missing UI/runtime dependencies**
  - **Problem**: Layout uses `CardView` and `RecyclerView` without deps; Activities use `lifecycleScope` but `lifecycle-runtime-ktx` is not declared.
  - **Tasks**:
    - [ ] Add `androidx.cardview:cardview`.
    - [ ] Add `androidx.recyclerview:recyclerview`.
    - [ ] Add `androidx.lifecycle:lifecycle-runtime-ktx`.
  - **Acceptance**: ViewBinding and `lifecycleScope` compile; app builds.
  - **Refs**: `app/build.gradle`, `res/layout/*`, `MainActivity.kt`, `PlaybackActivity.kt`. 

- **P1: Fix audio bitrate unit mismatch**
  - **Problem**: `SettingsManager.audioBitrate` stores kbps (e.g., 32), but `AudioRecorderService.updateSettings` expects bps.
  - **Tasks**:
    - [ ] Multiply by 1000 when passing to recorder or change setting units to bps throughout.
    - [ ] Audit and align `calculateStorageUsage()` with final units.
  - **Acceptance**: MediaRecorder receives correct `setAudioEncodingBitRate` (e.g., 32000).
  - **Refs**: `SettingsManager.kt`, `ListenForegroundService.updateAudioSettings()`.

- **P1: Consider using MPEG_4 container instead of AAC_ADTS**
  - **Problem**: `AAC_ADTS` may be less compatible across devices than `MPEG_4`.
  - **Tasks**:
    - [ ] Switch to `OutputFormat.MPEG_4` and `.m4a` extension; validate quality/size.
  - **Acceptance**: Recording succeeds across target devices; playback works.
  - **Refs**: `AudioRecorderService.kt` (41–49).

- **P1: Accurate service status and recording indicator in Main screen**
  - **Problem**: UI shows only a preference-backed state; `tv_recording_status` is never updated; no live timer; no actual service liveness check.
  - **Tasks**:
    - [ ] Bind to service or use a `LocalBroadcastManager`/`SharedFlow` to push recording state and elapsed time.
    - [ ] Update `tv_recording_status` and start/stop button states accordingly.
    - [ ] Handle service restarts and process death gracefully.
  - **Acceptance**: Main screen reflects real recording status and elapsed time; no stale state after process restarts.
  - **Refs**: `app/src/main/java/com/listen/app/ui/MainActivity.kt`, `res/layout/activity_main.xml`, `ListenForegroundService.kt`.

- **P1: Boot auto-start safeguards for permission state**
  - **Problem**: `ListenBootReceiver` starts the service after boot if enabled, even if microphone permission is revoked.
  - **Tasks**:
    - [ ] Check `RECORD_AUDIO` at boot before starting; if missing, delay start and notify user on next launch.
    - [ ] Optionally schedule a reminder/notification prompting to open the app to re-grant permission.
  - **Acceptance**: No failed starts at boot due to missing permission; clear guidance to the user.
  - **Refs**: `app/src/main/java/com/listen/app/receiver/ListenBootReceiver.kt`, `AndroidManifest.xml`.

---

### P2 — Reliability, Storage, and Cleanup

- **P2: Audio focus and playback attributes**
  - **Problem**: `MediaPlayer` playback lacks audio focus handling; may conflict with other apps and notifications; missing `AudioAttributes`.
  - **Tasks**:
    - [ ] Request and manage audio focus during playback; abandon on stop.
    - [ ] Set `AudioAttributes` on `MediaPlayer` for proper routing/ducking.
  - **Acceptance**: Playback respects system audio policies; no conflicts with other audio apps.
  - **Refs**: `PlaybackActivity.kt`.

- **P2: Correct storage limit enforcement algorithm**
  - **Problem**: `SegmentManagerService.enforceStorageLimits()` converts excess bytes to a count with `(excessBytes / 1024)` and fetches that many oldest rows, which is inefficient and incorrect.
  - **Tasks**:
    - [ ] Fetch oldest segments iteratively until cumulative file sizes free at least `excessBytes`.
    - [ ] Do deletions in a transaction and handle partial failures robustly.
  - **Acceptance**: Deletion targets the minimal number of segments and reliably frees required space.
  - **Refs**: `app/src/main/java/com/listen/app/service/SegmentManagerService.kt` (96–115).

- **P2: Tie background coroutine scope to lifecycle**
  - **Problem**: `SegmentManagerService` owns a `CoroutineScope(Dispatchers.IO)` without a cancellable parent; potential leaks after service teardown.
  - **Tasks**:
    - [ ] Provide a `SupervisorJob` and expose `cancel()`; call from `ListenForegroundService.onDestroy`.
    - [ ] Prefer structured concurrency where possible.
  - **Acceptance**: No leaked coroutines; clean shutdown.
  - **Refs**: `SegmentManagerService.kt`, `ListenForegroundService.kt`.

- **P2: Unique segment filenames**
  - **Problem**: Filenames are second-granularity; rapid rotations or retries can collide.
  - **Tasks**:
    - [ ] Include milliseconds (e.g., `yyyyMMdd_HHmmss_SSS`) or add a short random suffix.
  - **Acceptance**: No collisions observed under stress tests.
  - **Refs**: `app/src/main/java/com/listen/app/storage/StorageManager.kt` (24–28).

- **P2: Respect or redefine `showNotification` setting**
  - Resolved: Removed misleading setting; FGS always shows a notification.
  - Refs: `SettingsManager.kt`.

- **P2: Improve storage health and telemetry**
  - **Problem**: No proactive alerts or UI indicators when storage is low; emergency cleanup not surfaced to user.
  - **Tasks**:
    - [ ] Add UI banner/toast when storage near/exceeds cap.
    - [ ] Expose storage stats in Main screen (segments count, usage/available).
  - **Acceptance**: Users are informed before recording is impacted.
  - **Refs**: `SegmentManagerService.kt`, `StorageManager.kt`, `MainActivity.kt`.

- **P2: Room migrations plan**
  - Resolved: Added v2 migration scaffold and enabled exportSchema for future tracking.
  - Refs: `ListenDatabase.kt`. 

- **P2: Remove unnecessary storage permissions**
  - Resolved: Removed legacy `READ/WRITE_EXTERNAL_STORAGE` permissions; using app-private storage.
  - Refs: `AndroidManifest.xml`. 

- **P2: Cancel scheduled work when stopping service**
  - **Problem**: Unique periodic work is enqueued but never canceled on stop.
  - **Tasks**:
    - [ ] Cancel work in `ListenForegroundService.stop()` or `onDestroy()`.
  - **Acceptance**: No stale background tasks remain after stop.
  - **Refs**: `ListenForegroundService.kt`.

- **P2: Wake lock usage audit**
  - **Problem**: `WAKE_LOCK` permission declared but not used.
  - **Tasks**:
    - [ ] Decide whether an explicit partial wake lock is required during rotation/stop-start to avoid gaps.
    - [ ] If used, scope and release responsibly.
  - **Acceptance**: No unnecessary wake locks; battery usage remains minimal.
  - **Refs**: `AndroidManifest.xml`.

---

### P3 — Polish, DX, Tooling, and Compliance

- **P3: Create missing ProGuard/R8 rules file**
  - **Problem**: `proguard-rules.pro` referenced but missing.
  - **Tasks**:
    - [ ] Add an empty `proguard-rules.pro` with a basic template.
  - **Acceptance**: Release build doesn’t error if minification is enabled in the future.
  - **Refs**: `app/build.gradle` (24–25).

- **P3: Unify user-visible strings**
  - **Problem**: Some strings are hard-coded in code.
  - **Tasks**:
    - [ ] Move to `strings.xml` (e.g., toasts in `MainActivity` and `PlaybackActivity`).
  - **Acceptance**: All user-visible text is localized via resources.
  - **Refs**: `MainActivity.kt`, `PlaybackActivity.kt`, `res/values/strings.xml`.

- **P3: Logging strategy for production**
  - **Problem**: Extensive `Log.d` across code; no build-type gating.
  - **Tasks**:
    - [ ] Introduce Timber or wrap logs; silence debug logs in release.
    - [ ] Add `StrictMode` policies in debug to catch disk/network on main thread.
  - **Acceptance**: Clean release logs; debuggable builds retain verbosity. Debug builds surface performance issues early.
  - **Refs**: Multiple files.

- **P3: CI/CD pipeline**
  - **Tasks**:
    - [ ] Add GitHub Actions to build, run unit tests and lint on PRs.
    - [ ] Cache Gradle and setup Android SDK.
  - **Acceptance**: PRs are validated automatically.

- **P3: Code quality & linting**
  - **Tasks**:
    - [ ] Enable Android Lint, Detekt/Ktlint, and a formatting task.
  - **Acceptance**: Failing lint breaks CI; clear reports in PRs.

- **P3: Documentation updates**
  - **Tasks**:
    - [ ] Update `README.md` to reflect implemented features and limitations.
    - [ ] Add privacy guidelines and explicit legal disclaimers.
    - [ ] Add `ARCHITECTURE.md` cross-links to concrete classes and current design deltas.
  - **Acceptance**: Docs reflect current state; clear for users/contributors.

---

### Additional Findings (Nice-to-haves / Future Work)

- **Notification actions**: Start/stop recording, quick jump to playback.
- **Timeline view (visual history)**: As per `ARCHITECTURE.md`.
- **Data export**: If/when implemented, ensure scoped storage and runtime permissions.
- **Internationalization**: Add translations for key locales.

---

### Test Plan Checklist (for Done criteria)

- [ ] Unit tests: `SettingsManager.calculateStorageUsage`, `SegmentManagerService.performCleanup`, `StorageManager` helpers.
- [ ] Instrumented tests: permission flow, service lifecycle start/stop, rotation cadence over time.
- [ ] Manual QA matrix: API 26–34, background behavior with/without battery optimizations, device reboot auto-start.

---

### Quick Reference to Affected Files

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/listen/app/service/ListenForegroundService.kt`
- `app/src/main/java/com/listen/app/worker/SegmentRotationWorker.kt`
- `app/src/main/java/com/listen/app/audio/AudioRecorderService.kt`
- `app/src/main/java/com/listen/app/service/SegmentManagerService.kt`
- `app/src/main/java/com/listen/app/ui/MainActivity.kt`
- `app/src/main/java/com/listen/app/ui/PlaybackActivity.kt`
- `app/src/main/java/com/listen/app/settings/SettingsManager.kt`
- `app/build.gradle`
- `PRODUCTION_ISSUES.md`