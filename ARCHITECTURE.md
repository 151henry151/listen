# Listen App - Technical Architecture

## 🎯 Design Goals & Requirements

### Primary Objectives
- **Reliability**: App must persist through reboots, battery deaths, and system updates
- **Battery Efficiency**: Minimal power consumption for 24/7 background operation
- **Storage Management**: Automatic cleanup to prevent device storage overflow
- **Audio Quality**: Clear speech recognition while minimizing file sizes
- **System Resource Optimization**: Minimal CPU, memory, and I/O usage

### Critical Success Factors
- Service must auto-restart after any system interruption
- Audio segments must be created and deleted with precise timing
- App must work without user intervention for weeks/months
- Storage usage must be predictable and bounded

## 🏗️ System Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Listen App Architecture                  │
├─────────────────────────────────────────────────────────────┤
│  UI Layer (Activities & Fragments)                         │
│  ├── MainActivity (Dashboard & Settings)                   │
│  ├── PlaybackActivity (Audio Review)                       │
│  └── TimelineFragment (Visual History)                     │
├─────────────────────────────────────────────────────────────┤
│  Service Layer (Background Processing)                     │
│  ├── ListenForegroundService (Main Service)                │
│  ├── AudioRecorderService (Audio Capture)                  │
│  └── SegmentManagerService (File Management)               │
├─────────────────────────────────────────────────────────────┤
│  Core Layer (Business Logic)                               │
│  ├── AudioEngine (Recording & Playback)                    │
│  ├── SegmentManager (File Operations)                      │
│  ├── StorageManager (File System)                          │
│  └── SettingsManager (Configuration)                       │
├─────────────────────────────────────────────────────────────┤
│  Persistence Layer (Data Storage)                          │
│  ├── SharedPreferences (Settings)                          │
│  ├── SQLite Database (Metadata)                            │
│  └── File System (Audio Files)                             │
└─────────────────────────────────────────────────────────────┘
```

## 🔧 Core Components Design

### 1. ListenForegroundService (Main Service)
**Purpose**: Orchestrates all background operations and ensures service persistence

**Key Responsibilities**:
- Manages service lifecycle and auto-restart mechanisms
- Coordinates between AudioRecorderService and SegmentManagerService
- Handles system events (reboots, battery optimization, etc.)
- Provides foreground notification for user awareness

**Implementation Strategy**:
```kotlin
class ListenForegroundService : Service() {
    private lateinit var audioRecorder: AudioRecorderService
    private lateinit var segmentManager: SegmentManagerService
    private lateinit var workManager: WorkManager
    
    override fun onCreate() {
        super.onCreate()
        setupForegroundNotification()
        initializeServices()
        schedulePeriodicWork()
    }
    
    private fun schedulePeriodicWork() {
        // Use WorkManager for reliable background execution
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()
            
        val segmentWork = PeriodicWorkRequestBuilder<SegmentWork>(
            segmentDuration.toLong(), TimeUnit.SECONDS
        ).setConstraints(constraints)
         .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
         .build()
         
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "segment_rotation",
            ExistingPeriodicWorkPolicy.REPLACE,
            segmentWork
        )
    }
}
```

### 2. AudioRecorderService (Audio Capture)
**Purpose**: Handles continuous audio recording with optimal quality and efficiency

**Audio Configuration**:
- **Format**: AAC (Advanced Audio Coding) - best compression/quality ratio
- **Sample Rate**: 16kHz - sufficient for speech, reduces file size by 50%
- **Bit Depth**: 16-bit - standard for speech recording

**Auto Music Mode Features**:
- **Silence Detection**: Monitors audio levels to detect natural breaks in content
- **Adaptive Segmentation**: Targets ~5-minute segments with intelligent splitting at silence points
- **Content-Aware**: Optimized for podcasts, meetings, music sessions, and long-form content
- **Fallback Protection**: Ensures segments don't exceed maximum duration limits
- **Channels**: Mono - reduces file size by 50%, sufficient for ambient audio
- **Bitrate**: 32kbps - excellent speech quality, minimal storage

**Implementation Strategy**:
```kotlin
class AudioRecorderService {
    private var mediaRecorder: MediaRecorder? = null
    private var currentSegmentFile: File? = null
    private var segmentStartTime: Long = 0
    
    fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(16000)
            setAudioChannels(1) // Mono
            setAudioEncodingBitRate(32000) // 32kbps
            
            val segmentFile = createSegmentFile()
            setOutputFile(segmentFile.absolutePath)
            
            prepare()
            start()
        }
        
        segmentStartTime = System.currentTimeMillis()
        currentSegmentFile = segmentFile
    }
    
    fun rotateSegment(): File? {
        val completedFile = currentSegmentFile
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        
        // Start new segment immediately
        startRecording()
        
        return completedFile
    }
}
```

### 3. SegmentManagerService (File Management)
**Purpose**: Manages audio segment lifecycle, storage, and cleanup

**Storage Strategy**:
- **Directory Structure**: `/data/data/com.listen.app/files/segments/`
- **File Naming**: `segment_YYYYMMDD_HHMMSS.aac`
- **Metadata Storage**: SQLite database for quick segment lookup
- **Cleanup Policy**: Delete oldest segments when retention period exceeded

**Implementation Strategy**:
```kotlin
class SegmentManagerService {
    private val database: SegmentDatabase
    private val storageManager: StorageManager
    
    fun rotateSegments() {
        val currentTime = System.currentTimeMillis()
        val retentionPeriodMs = settings.retentionPeriodMinutes * 60 * 1000L
        
        // Get segments older than retention period
        val oldSegments = database.segmentDao()
            .getSegmentsOlderThan(currentTime - retentionPeriodMs)
        
        // Delete old segments
        oldSegments.forEach { segment ->
            val file = File(segment.filePath)
            if (file.exists()) {
                file.delete()
                database.segmentDao().deleteSegment(segment)
            }
        }
        
        // Ensure storage limits
        enforceStorageLimits()
    }
    
    private fun enforceStorageLimits() {
        val maxStorageBytes = settings.maxStorageMB * 1024 * 1024L
        val currentUsage = storageManager.getCurrentStorageUsage()
        
        if (currentUsage > maxStorageBytes) {
            val segmentsToDelete = database.segmentDao()
                .getOldestSegments((currentUsage - maxStorageBytes) / 1024)
            
            segmentsToDelete.forEach { segment ->
                File(segment.filePath).delete()
                database.segmentDao().deleteSegment(segment)
            }
        }
    }
}
```

### 4. StorageManager (File System Operations)
**Purpose**: Handles file system operations and storage monitoring

**Implementation Strategy**:
```kotlin
class StorageManager {
    private val segmentsDir: File
    
    fun getCurrentStorageUsage(): Long {
        return segmentsDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
    
    fun createSegmentFile(timestamp: Long): File {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val fileName = "segment_${dateFormat.format(Date(timestamp))}.aac"
        return File(segmentsDir, fileName)
    }
    
    fun cleanupOrphanedFiles() {
        val databaseFiles = database.segmentDao().getAllFilePaths().toSet()
        val actualFiles = segmentsDir.listFiles()?.map { it.absolutePath }?.toSet() ?: emptySet()
        
        val orphanedFiles = actualFiles - databaseFiles
        orphanedFiles.forEach { filePath ->
            File(filePath).delete()
        }
    }
}

### 5. Auto Music Mode (Intelligent Segmentation)
**Purpose**: Provides content-aware audio segmentation for optimal playback experience

**Key Features**:
- **Silence Detection**: Real-time audio level monitoring to identify natural content breaks
- **Adaptive Timing**: Targets ~5-minute segments while respecting content boundaries
- **Smart Heuristics**: Uses multiple factors to determine optimal split points

**Implementation Strategy**:
```kotlin
class AutoMusicModeManager {
    private const val TARGET_SEGMENT_SECONDS = 300L // 5 minutes
    private const val SILENCE_THRESHOLD_MS = 1200L // 1.2 seconds
    private const val MAX_EXTRA_WAIT_MS = 180_000L // 3 minutes after target
    
    fun shouldSplitSegment(currentDuration: Long, audioLevel: Int): Boolean {
        val isNearTarget = currentDuration >= TARGET_SEGMENT_SECONDS * 1000
        val isSilent = audioLevel < SILENCE_THRESHOLD
        val hasExceededMax = currentDuration >= (TARGET_SEGMENT_SECONDS + 180) * 1000
        
        return (isNearTarget && isSilent) || hasExceededMax
    }
    
    fun monitorAudioLevels() {
        // Continuous monitoring of audio levels
        // Triggers segment rotation when conditions are met
    }
}
```

**Benefits**:
- **Better Content Organization**: Segments align with natural content boundaries
- **Improved Playback Experience**: Users can easily navigate to specific content sections
- **Optimized for Long-Form Content**: Perfect for podcasts, meetings, and music sessions
- **Fallback Protection**: Ensures segments don't grow indefinitely

## 🔄 Service Persistence Strategy

### 1. Auto-Restart Mechanisms
**Multiple Layers of Reliability**:

1. **Foreground Service**: Prevents system from killing the service
2. **WorkManager**: Handles periodic tasks and service restart
3. **AlarmManager**: Backup restart mechanism
4. **Boot Receiver**: Restarts service after device reboot
5. **Battery Optimization Exemption**: Prevents Doze mode interference

**Implementation**:
```kotlin
class ListenBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val settings = SettingsManager(context)
            if (settings.isServiceEnabled()) {
                // Delay restart to allow system to stabilize
                Handler(Looper.getMainLooper()).postDelayed({
                    ListenForegroundService.start(context)
                }, 30000) // 30 seconds delay
            }
        }
    }
}

class ListenForegroundService : Service() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ListenForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Return START_STICKY to restart service if killed
        return START_STICKY
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        // Restart service if app is removed from recent tasks
        val restartServiceIntent = Intent(this, ListenForegroundService::class.java)
        startService(restartServiceIntent)
    }
}
```

### 2. Battery Optimization
**Strategies for Minimal Power Consumption**:

1. **Efficient Audio Recording**:
   - Use hardware-accelerated AAC encoding
   - Optimize buffer sizes for minimal CPU usage
   - Batch file operations

2. **Smart Scheduling**:
   - Use WorkManager with appropriate constraints
   - Avoid unnecessary wake-ups
   - Batch segment operations

3. **Memory Management**:
   - Reuse objects where possible
   - Minimize object allocations
   - Use efficient data structures

## 📊 Audio Quality & Storage Optimization

### Audio Configuration Analysis
**Recommended Settings for Speech Clarity**:

| Parameter | Value | Rationale |
|-----------|-------|-----------|
| Format | AAC | Best compression/quality ratio |
| Sample Rate | 16kHz | Sufficient for speech (human speech: 85Hz-8kHz) |
| Bit Depth | 16-bit | Standard, sufficient dynamic range |
| Channels | Mono | Reduces file size 50%, sufficient for ambient audio |
| Bitrate | 32kbps | Excellent speech quality, minimal storage |

**Storage Calculation Examples**:

**Fixed Duration Mode**:
- 1 minute segment: 32kbps × 60s = 240KB
- 10 minutes retention: 10 × 240KB = 2.4MB
- 1 hour retention: 60 × 240KB = 14.4MB
- 24 hours retention: 1440 × 240KB = 345.6MB

**Auto Music Mode**:
- ~5 minute segment: 32kbps × 300s = 1.2MB
- 30 minutes retention: ~6 × 1.2MB = 7.2MB
- 1 hour retention: ~12 × 1.2MB = 14.4MB
- 24 hours retention: ~288 × 1.2MB = 345.6MB

### Quality vs. Storage Trade-offs
- **16kHz vs 44.1kHz**: 73% storage reduction, minimal quality loss for speech
- **Mono vs Stereo**: 50% storage reduction, sufficient for ambient recording
- **32kbps vs 128kbps**: 75% storage reduction, still excellent for speech
- **AAC vs MP3**: 10-15% better compression at same quality

## 🛡️ Error Handling & Recovery

### 1. Service Failure Recovery
```kotlin
class ServiceHealthMonitor {
    private val healthCheckInterval = 5 * 60 * 1000L // 5 minutes
    
    fun startHealthMonitoring() {
        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                checkServiceHealth()
                Handler(Looper.getMainLooper()).postDelayed(this, healthCheckInterval)
            }
        }, healthCheckInterval)
    }
    
    private fun checkServiceHealth() {
        if (!isServiceRunning()) {
            Log.w("ListenService", "Service not running, restarting...")
            ListenForegroundService.start(context)
        }
        
        if (!isRecordingActive()) {
            Log.w("ListenService", "Recording not active, restarting...")
            restartRecording()
        }
    }
}
```

### 2. Storage Failure Recovery
```kotlin
class StorageHealthMonitor {
    fun checkStorageHealth() {
        try {
            val freeSpace = getAvailableStorage()
            val requiredSpace = calculateRequiredSpace()
            
            if (freeSpace < requiredSpace) {
                // Emergency cleanup
                emergencyCleanup(requiredSpace - freeSpace)
            }
        } catch (e: Exception) {
            Log.e("StorageHealth", "Storage check failed", e)
            // Attempt recovery
            recoverStorage()
        }
    }
    
    private fun emergencyCleanup(requiredBytes: Long) {
        val segmentsToDelete = database.segmentDao()
            .getOldestSegments(requiredBytes / 1024)
        
        segmentsToDelete.forEach { segment ->
            File(segment.filePath).delete()
            database.segmentDao().deleteSegment(segment)
        }
    }
}
```

## 📱 User Interface Design

### 1. Main Dashboard
- **Service Status**: Running/Stopped indicator
- **Current Recording**: Live audio level meter
- **Storage Usage**: Visual progress bar
- **Quick Actions**: Start/Stop, Settings, Playback

### 2. Timeline View
- **Visual Timeline**: Horizontal scrollable timeline
- **Segment Markers**: Time-based segment indicators
- **Playback Controls**: Tap to play any segment
- **Search/Filter**: Find specific time periods

### 3. Settings Panel
- **Recording Settings**: Segment length, auto music mode, retention period
- **Audio Quality**: Bitrate, sample rate options
- **Storage Settings**: Max storage, cleanup policies
- **Battery Settings**: Optimization preferences

## 🔒 Privacy & Security Considerations

### 1. Data Protection
- **Local Storage Only**: No network transmission
- **File Encryption**: Optional AES-256 encryption
- **Access Control**: App-specific storage directory
- **Secure Deletion**: Overwrite files before deletion

### 2. Permission Management
- **Explicit Permissions**: Clear user consent for microphone
- **Runtime Permissions**: Android 6.0+ permission handling
- **Permission Explanation**: Clear rationale for each permission

## 📈 Performance Monitoring

### 1. Key Metrics
- **Battery Usage**: mAh per hour consumption
- **Storage Usage**: MB per day growth
- **CPU Usage**: Average CPU percentage
- **Memory Usage**: Heap memory consumption
- **Service Uptime**: Percentage of time running

### 2. Monitoring Implementation
```kotlin
class PerformanceMonitor {
    fun trackMetrics() {
        val batteryUsage = getBatteryUsage()
        val storageUsage = getStorageUsage()
        val cpuUsage = getCpuUsage()
        val memoryUsage = getMemoryUsage()
        
        // Log metrics for analysis
        Log.d("Performance", "Battery: ${batteryUsage}mAh/h, " +
            "Storage: ${storageUsage}MB, CPU: ${cpuUsage}%, " +
            "Memory: ${memoryUsage}MB")
    }
}
```

## 🚀 Implementation Roadmap

### Phase 1: Core Infrastructure (Weeks 1-2)
- [ ] Project setup and basic architecture
- [ ] ListenForegroundService implementation
- [ ] Basic audio recording functionality
- [ ] Service persistence mechanisms

### Phase 2: Audio Management (Weeks 3-4)
- [ ] AudioRecorderService with optimal settings
- [ ] SegmentManagerService implementation
- [ ] Storage management and cleanup
- [ ] Error handling and recovery

### Phase 3: User Interface (Weeks 5-6)
- [ ] Main dashboard and settings
- [ ] Timeline view and playback
- [ ] Audio quality controls
- [ ] Storage monitoring UI

### Phase 4: Optimization & Testing (Weeks 7-8)
- [ ] Battery optimization
- [ ] Performance monitoring
- [ ] Stress testing and reliability
- [ ] User acceptance testing

## 🎯 Success Criteria

### Technical Metrics
- **Service Uptime**: >99.5% (less than 3.6 hours downtime per month)
- **Battery Impact**: <2% additional battery drain per day
- **Storage Efficiency**: <50MB per day for 24-hour retention
- **Audio Quality**: Clear speech recognition in normal environments

### User Experience
- **Reliability**: App works for weeks without user intervention
- **Simplicity**: One-tap access to recent audio
- **Performance**: Instant playback response
- **Transparency**: Clear indication of recording status

This architecture provides a robust foundation for a reliable, efficient, and user-friendly background audio recording application that meets all the specified requirements while maintaining excellent performance and battery life. 