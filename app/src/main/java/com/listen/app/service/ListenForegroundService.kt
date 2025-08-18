package com.listen.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.listen.app.R
import com.listen.app.audio.AudioRecorderService
import com.listen.app.data.ListenDatabase
import com.listen.app.settings.SettingsManager
import com.listen.app.storage.StorageManager
import com.listen.app.ui.MainActivity
import com.listen.app.worker.SegmentRotationWorker
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import android.os.PowerManager
import com.listen.app.util.AppLog
import android.provider.Settings as AndroidSettings
import android.net.Uri
import com.listen.app.perf.PerformanceMonitor
import android.Manifest
import android.content.pm.PackageManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import android.provider.CallLog
import android.database.Cursor

/**
 * Main foreground service that orchestrates background audio recording
 */
class ListenForegroundService : Service() {
    
    private lateinit var settings: SettingsManager
    private lateinit var database: ListenDatabase
    private lateinit var storageManager: StorageManager
    private lateinit var audioRecorder: AudioRecorderService
    private lateinit var segmentManager: SegmentManagerService
    private lateinit var workManager: WorkManager
    private var performanceMonitor: PerformanceMonitor? = null
    
    private var isServiceRunning = false
    
    // Service-scoped coroutine context for timers/broadcasts
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    
    // Jobs
    private var rotationJobActive = false
    private var statusBroadcastJobActive = false
    
    private var wakeLock: PowerManager.WakeLock? = null

    // Telephony
    private var telephonyManager: TelephonyManager? = null
    private var phoneStateListener: PhoneStateListener? = null
    private var isCallActive: Boolean = false
    private var lastState: Int = TelephonyManager.CALL_STATE_IDLE
    private var sawRingingBeforeOffhook: Boolean = false
    private var currentCallDirection: String? = null // INCOMING or OUTGOING
    private var currentCallNumber: String? = null
    private var currentSegmentIsPhoneCall: Boolean = false
    
    companion object {
        private const val TAG = "ListenForegroundService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "listen_recording_channel"
        
        // Broadcasts
        const val ACTION_RECORDING_STATUS = "com.listen.app.ACTION_RECORDING_STATUS"
        const val EXTRA_IS_RECORDING = "extra_is_recording"
        const val EXTRA_ELAPSED_MS = "extra_elapsed_ms"
        
        // Commands
        const val ACTION_UPDATE_SETTINGS = "com.listen.app.ACTION_UPDATE_SETTINGS"
        
        // Auto music mode heuristics
        private const val AUTO_MUSIC_POLL_INTERVAL_MS = 100L
        private const val AUTO_MUSIC_SILENCE_MIN_MS = 1200L
        private const val AUTO_MUSIC_MAX_EXTRA_WAIT_MS = 180_000L // 3 minutes after target
        private const val AUTO_MUSIC_MIN_THRESHOLD = 800
        private const val AUTO_MUSIC_RELATIVE_SILENCE_FACTOR = 0.35
        
        /** Start the service */
        fun start(context: Context) {
            val intent = Intent(context, ListenForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        /** Stop the service */
        fun stop(context: Context) {
            val intent = Intent(context, ListenForegroundService::class.java)
            context.stopService(intent)
        }
        
        /** Apply updated settings while service is running */
        fun applyUpdatedSettings(context: Context) {
            val intent = Intent(context, ListenForegroundService::class.java).apply {
                action = ACTION_UPDATE_SETTINGS
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        AppLog.d(TAG, "Service created")
        
        // Initialize components
        settings = SettingsManager(this)
        database = ListenDatabase.getDatabase(this)
        storageManager = StorageManager(this)
        audioRecorder = AudioRecorderService(this, storageManager)
        segmentManager = SegmentManagerService(this, database, storageManager, settings)
        workManager = WorkManager.getInstance(this)
        performanceMonitor = PerformanceMonitor(this)
        
        // Set up audio recorder callback
        audioRecorder.onSegmentCompleted = { file, startTime, duration ->
            // Pass call metadata if this segment corresponds to a phone call
            val isCall = currentSegmentIsPhoneCall
            val dir = if (isCall) currentCallDirection else null
            val num = if (isCall) currentCallNumber else null
            segmentManager.addSegment(file, startTime, duration, isCall, dir, num)
            // Record rotation performance
            performanceMonitor?.recordSegmentRotation(duration)
            // Update notification content subtly to show recent rotation
            updateNotification("Recording... (rotated)")
        }
        
        // Create notification channel
        createNotificationChannel()

        // Initialize telephony listener to handle call start/end
        initTelephonyMonitoring()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppLog.d(TAG, "Service started")
        
        if (intent?.action == ACTION_UPDATE_SETTINGS) {
            AppLog.d(TAG, "Applying updated settings to recorder")
            updateAudioSettings()
            applyAdaptiveBehavior()
            return START_STICKY
        }
        
        if (!isServiceRunning) {
            startForegroundService()
            ensureWakeLock()
            requestIgnoreBatteryOptimizations()
            applyAdaptiveBehavior()
            val started = startRecording()
            if (started) {
                // Mark that recording is active (for boot recovery)
                settings.wasRecordingOnShutdown = true
                startInServiceRotationScheduler()
                startStatusBroadcasts()
                performanceMonitor?.start(serviceScope)
            } else {
                updateNotification("Recording failed. Tap to retry.")
            }
            // Cancel any legacy scheduled work to avoid duplicates
            cancelScheduledSegmentRotationWork()
            settings.lastServiceStartTime = System.currentTimeMillis()
        }
        
        // Return START_STICKY to restart service if killed
        return START_STICKY
    }
    
    override fun onDestroy() {
        AppLog.d(TAG, "Service destroyed")
        
        // Check if this is a user-initiated stop vs system shutdown
        // If isServiceEnabled is false, the user stopped it manually
        // If true, this is likely a system shutdown/crash
        if (!settings.isServiceEnabled) {
            // User stopped the service manually
            settings.wasRecordingOnShutdown = false
            AppLog.d(TAG, "Service stopped by user - clearing shutdown flag")
        } else {
            // Service is being destroyed but isServiceEnabled is still true
            // This means it's a system shutdown or crash
            // Keep wasRecordingOnShutdown as true so we can resume on boot
            AppLog.d(TAG, "Service destroyed unexpectedly - keeping shutdown flag")
        }
        
        stopStatusBroadcasts()
        stopInServiceRotationScheduler()
        stopRecording()
        cancelScheduledSegmentRotationWork()
        segmentManager.cancel()
        releaseWakeLock()
        performanceMonitor?.stop()
        unregisterTelephonyMonitoring()
        isServiceRunning = false
        serviceScope.cancel()
        super.onDestroy()
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        AppLog.d(TAG, "App removed from recent tasks, restarting service")
        // Restart service if app is removed from recent tasks
        val restartServiceIntent = Intent(this, ListenForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartServiceIntent)
        } else {
            startService(restartServiceIntent)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    /** Start the foreground service with notification */
    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        isServiceRunning = true
        AppLog.d(TAG, "Started foreground service")
    }
    
    /** Create the notification for the foreground service */
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_mic)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
    
    /** Update the existing foreground notification text */
    private fun updateNotification(contentText: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /** Create notification channel for Android O+ */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /** Start audio recording */
    private fun startRecording(): Boolean {
        val success = audioRecorder.startRecording()
        currentSegmentIsPhoneCall = false
        broadcastStatus()
        return success
    }
    
    /** Stop audio recording */
    private fun stopRecording() {
        audioRecorder.stopRecording()
        broadcastStatus()
        AppLog.d(TAG, "Audio recording stopped")
    }
    
    /** In-service rotation scheduler to replace WorkManager short-period scheduling */
    private fun startInServiceRotationScheduler() {
        if (rotationJobActive) return
        rotationJobActive = true
        serviceScope.launch {
            while (isActive && isServiceRunning) {
                try {
                    if (!settings.autoMusicModeEnabled) {
                        val segmentDuration = settings.segmentDurationSeconds.toLong().coerceAtLeast(1L)
                        delay(segmentDuration * 1000L)
                        val before = System.currentTimeMillis()
                        audioRecorder.rotateSegment()
                        val after = System.currentTimeMillis()
                        performanceMonitor?.recordSegmentRotation(after - before)
                        broadcastStatus()
                    } else {
                        val targetMs = SettingsManager.AUTO_MUSIC_TARGET_SECONDS * 1000L
                        // Wait until we reach at least the target duration
                        while (isActive && isServiceRunning && audioRecorder.getCurrentRecordingDuration() < targetMs) {
                            delay(250L)
                            if (!settings.autoMusicModeEnabled) break
                        }
                        if (!settings.autoMusicModeEnabled) {
                            // Mode toggled off; restart loop to use fixed scheduler path
                            continue
                        }
                        // After target, look for a silence window
                        var emaAmplitude = 0.0
                        var consecutiveSilenceMs = 0L
                        val maxSegmentMs = targetMs + AUTO_MUSIC_MAX_EXTRA_WAIT_MS
                        while (isActive && isServiceRunning && audioRecorder.getCurrentRecordingDuration() < maxSegmentMs) {
                            val amp = audioRecorder.getMaxAmplitude().coerceAtLeast(0)
                            // Exponential moving average to adapt threshold
                            emaAmplitude = if (emaAmplitude == 0.0) amp.toDouble() else (0.9 * emaAmplitude + 0.1 * amp)
                            val dynamicThreshold = maxOf(AUTO_MUSIC_MIN_THRESHOLD.toDouble(), emaAmplitude * AUTO_MUSIC_RELATIVE_SILENCE_FACTOR).toInt()
                            if (amp < dynamicThreshold) {
                                consecutiveSilenceMs += AUTO_MUSIC_POLL_INTERVAL_MS
                                if (consecutiveSilenceMs >= AUTO_MUSIC_SILENCE_MIN_MS) {
                                    break
                                }
                            } else {
                                consecutiveSilenceMs = 0L
                            }
                            delay(AUTO_MUSIC_POLL_INTERVAL_MS)
                            if (!settings.autoMusicModeEnabled) break
                        }
                        val before = System.currentTimeMillis()
                        audioRecorder.rotateSegment()
                        val after = System.currentTimeMillis()
                        performanceMonitor?.recordSegmentRotation(after - before)
                        broadcastStatus()
                    }
                } catch (e: Exception) {
                    AppLog.e(TAG, "Error rotating segment", e)
                }
            }
        }
        AppLog.d(TAG, "Started in-service rotation scheduler")
    }
    
    private fun stopInServiceRotationScheduler() {
        if (!rotationJobActive) return
        serviceJob.children.forEach { child -> child.cancel() }
        rotationJobActive = false
        AppLog.d(TAG, "Stopped in-service rotation scheduler")
    }
    
    /** Periodically broadcast recording status to UI */
    private fun startStatusBroadcasts() {
        if (statusBroadcastJobActive) return
        statusBroadcastJobActive = true
        serviceScope.launch {
            while (isActive && isServiceRunning) {
                broadcastStatus()
                delay(1000L)
            }
        }
    }
    
    private fun stopStatusBroadcasts() {
        statusBroadcastJobActive = false
    }
    
    private fun broadcastStatus() {
        val intent = Intent(ACTION_RECORDING_STATUS).apply {
            putExtra(EXTRA_IS_RECORDING, audioRecorder.isRecording())
            putExtra(EXTRA_ELAPSED_MS, audioRecorder.getCurrentRecordingDuration())
        }
        sendBroadcast(intent)
    }
    
    /** Schedule periodic segment rotation using WorkManager (legacy, not used for <15 min) */
    private fun scheduleSegmentRotation() {
        val segmentDuration = settings.segmentDurationSeconds.toLong()
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(settings.powerSavingModeEnabled)
            .build()
        
        val segmentWork = PeriodicWorkRequestBuilder<SegmentRotationWorker>(
            segmentDuration, TimeUnit.SECONDS
        ).setConstraints(constraints)
         .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
         .build()
        
        workManager.enqueueUniquePeriodicWork(
            "segment_rotation",
            ExistingPeriodicWorkPolicy.REPLACE,
            segmentWork
        )
        
        AppLog.d(TAG, "Scheduled segment rotation every $segmentDuration seconds")
    }
    
    private fun cancelScheduledSegmentRotationWork() {
        try {
            workManager.cancelUniqueWork("segment_rotation")
        } catch (e: Exception) {
            AppLog.w(TAG, "Failed to cancel scheduled segment rotation work", e)
        }
    }
    
    /** Get current recording status */
    fun isRecording(): Boolean = audioRecorder.isRecording()
    
    /** Get current recording duration */
    fun getCurrentRecordingDuration(): Long = audioRecorder.getCurrentRecordingDuration()
    
    /** Get current segment file */
    fun getCurrentSegmentFile(): File? = audioRecorder.getCurrentSegmentFile()
    
    /** Update audio settings */
    fun updateAudioSettings() {
        val bitrateKbps = if (settings.powerSavingModeEnabled) {
            (settings.audioBitrate / 2).coerceAtLeast(16)
        } else settings.audioBitrate
        val sampleRateHz = if (settings.powerSavingModeEnabled) {
            when {
                settings.audioSampleRate >= 44100 -> 22050
                settings.audioSampleRate >= 32000 -> 16000
                else -> settings.audioSampleRate
            }
        } else settings.audioSampleRate
        audioRecorder.updateSettings(
            bitrateKbps * 1000, // convert kbps to bps
            sampleRateHz,
            1 // Mono channel
        )
    }
    
    /** Apply adaptive behavior based on device state and settings */
    private fun applyAdaptiveBehavior() {
        if (!settings.adaptivePerformanceEnabled) return
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val isPowerSave = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) pm.isPowerSaveMode else false
            val isInteractive = pm.isInteractive
            // If device is in power save or not interactive, reduce segment churn and bitrate
            if (isPowerSave || !isInteractive || settings.powerSavingModeEnabled) {
                // Increase segment duration to reduce IO churn
                val current = settings.segmentDurationSeconds
                if (current < 120) {
                    settings.segmentDurationSeconds = 120
                }
            }
        } catch (_: Exception) { }
        updateAudioSettings()
    }
    
    /** Perform manual segment rotation */
    fun rotateSegment(): File? {
        return audioRecorder.rotateSegment()
    }
    
    /** Get storage statistics */
    suspend fun getStorageStats() = segmentManager.getStorageStats()
    
    /** Check if storage is healthy */
    fun isStorageHealthy() = segmentManager.isStorageHealthy()
    
    /** Emergency cleanup */
    fun emergencyCleanup(requiredBytes: Long) = segmentManager.emergencyCleanup(requiredBytes)
    
    private fun ensureWakeLock() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (wakeLock == null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Listen:Recorder")
                wakeLock?.setReferenceCounted(false)
            }
            if (wakeLock?.isHeld != true) {
                wakeLock?.acquire()
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Failed to acquire persistent wake lock", e)
        }
    }
    
    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Failed to release wake lock", e)
        }
    }

    private fun requestIgnoreBatteryOptimizations() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                val pkg = packageName
                val ignoring = pm.isIgnoringBatteryOptimizations(pkg)
                if (!ignoring) {
                    val intent = Intent(AndroidSettings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:$pkg")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Battery optimization request failed", e)
        }
    }

    // Telephony monitoring
    private fun initTelephonyMonitoring() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                AppLog.w(TAG, "READ_PHONE_STATE not granted; call metadata and truncation on calls may be unavailable")
                return
            }
            telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            phoneStateListener = object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    handleCallStateChanged(state, phoneNumber)
                }
            }
            @Suppress("DEPRECATION")
            telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
            AppLog.d(TAG, "Telephony monitoring initialized")
        } catch (e: Exception) {
            AppLog.w(TAG, "Failed to initialize telephony monitoring", e)
        }
    }

    private fun unregisterTelephonyMonitoring() {
        try {
            telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        } catch (_: Exception) { }
        phoneStateListener = null
        telephonyManager = null
    }

    private fun handleCallStateChanged(state: Int, number: String?) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                sawRingingBeforeOffhook = true
                currentCallNumber = number
                lastState = state
                AppLog.d(TAG, "Phone ringing: $number")
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (!isCallActive) {
                    val direction = if (sawRingingBeforeOffhook) "INCOMING" else "OUTGOING"
                    onCallStarted(direction, currentCallNumber)
                }
                lastState = state
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                if (isCallActive) {
                    onCallEnded()
                }
                lastState = state
                sawRingingBeforeOffhook = false
                currentCallNumber = null
            }
        }
    }

    private fun onCallStarted(direction: String, number: String?) {
        AppLog.d(TAG, "Call started ($direction) ${number ?: ""}")
        isCallActive = true
        currentCallDirection = direction
        currentCallNumber = number

        // Try to resolve outgoing dialed number if missing
        if (direction == "OUTGOING" && currentCallNumber.isNullOrEmpty()) {
            currentCallNumber = resolveRecentOutgoingNumber()
        }
        
        // 1) Truncate current ambient segment at call start
        try {
            if (audioRecorder.isRecording()) {
                audioRecorder.stopRecording()
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Failed to stop ambient recording on call start", e)
        }
        
        // 2) Disable periodic rotation; we want a single call segment
        stopInServiceRotationScheduler()
        
        // 3) Start call recording segment (best-effort; capture may be limited on modern Android)
        currentSegmentIsPhoneCall = true
        val started = audioRecorder.startRecording()
        if (started) {
            updateNotification("Recording call… ${direction}${if (!currentCallNumber.isNullOrEmpty()) ": ${currentCallNumber}" else ""}")
        } else {
            updateNotification("Call recording not supported on this device")
        }
    }

    private fun onCallEnded() {
        AppLog.d(TAG, "Call ended")
        // Finalize the call segment
        try {
            if (audioRecorder.isRecording()) {
                audioRecorder.stopRecording()
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Failed to stop call recording", e)
        }
        
        // Reset call flags
        isCallActive = false
        currentSegmentIsPhoneCall = false
        val lastDirection = currentCallDirection
        val lastNumber = currentCallNumber
        currentCallDirection = null
        currentCallNumber = null
        
        // 4) Resume ambient recording and periodic rotation
        val started = startRecording()
        if (started) {
            startInServiceRotationScheduler()
            updateNotification("Recording… resumed after call${if (!lastDirection.isNullOrEmpty()) " ($lastDirection)" else ""}")
        } else {
            updateNotification("Recording failed to resume after call")
        }
    }

    private fun resolveRecentOutgoingNumber(): String? {
        return try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                return null
            }
            val uri = CallLog.Calls.CONTENT_URI
            val projection = arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE)
            val selection = "${CallLog.Calls.TYPE} = ?"
            val selectionArgs = arrayOf(CallLog.Calls.OUTGOING_TYPE.toString())
            val sortOrder = CallLog.Calls.DATE + " DESC LIMIT 1"
            val cursor: Cursor? = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
            cursor?.use {
                if (it.moveToFirst()) {
                    val numberIdx = it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                    return it.getString(numberIdx)
                }
            }
            null
        } catch (e: Exception) {
            AppLog.w(TAG, "Failed to resolve outgoing number from call log", e)
            null
        }
    }
} 