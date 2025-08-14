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

- Resolved in code; will validate on device/emulator.

---

### P2 — Reliability, Storage, and Cleanup

- Resolved in code; will validate on device/emulator.

---

### P3 — Polish, DX, Tooling, and Compliance

- **P3: Logging strategy for production**
  - **Problem**: Extensive `Log.d` across code; no build-type gating.
  - **Tasks**:
    - [ ] Introduce Timber or wrap logs; silence debug logs in release.
    - [ ] Add `StrictMode` policies in debug to catch disk/network on main thread.
  - **Acceptance**: Clean release logs; debuggable builds retain verbosity. Debug builds surface performance issues early.
  - **Refs**: Multiple files.

- **P3: CI/CD pipeline**
  - Resolved: Added GitHub Actions workflow to build, test, and lint.
  - Refs: `.github/workflows/android.yml`.

- **P3: Code quality & linting**
  - **Tasks**:
    - [ ] Enable Detekt/Ktlint and a formatting task.
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