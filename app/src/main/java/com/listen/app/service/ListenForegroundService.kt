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
    
    private var isServiceRunning = false
    
    // Service-scoped coroutine context for timers/broadcasts
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    
    // Jobs
    private var rotationJobActive = false
    private var statusBroadcastJobActive = false
    
    private var wakeLock: PowerManager.WakeLock? = null
    
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
        
        // Set up audio recorder callback
        audioRecorder.onSegmentCompleted = { file, startTime, duration ->
            segmentManager.addSegment(file, startTime, duration)
            // Update notification content subtly to show recent rotation
            updateNotification("Recording... (rotated)")
        }
        
        // Create notification channel
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppLog.d(TAG, "Service started")
        
        if (intent?.action == ACTION_UPDATE_SETTINGS) {
            AppLog.d(TAG, "Applying updated settings to recorder")
            updateAudioSettings()
            return START_STICKY
        }
        
        if (!isServiceRunning) {
            startForegroundService()
            ensureWakeLock()
            val started = startRecording()
            if (started) {
                startInServiceRotationScheduler()
                startStatusBroadcasts()
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
        stopStatusBroadcasts()
        stopInServiceRotationScheduler()
        stopRecording()
        cancelScheduledSegmentRotationWork()
        segmentManager.cancel()
        releaseWakeLock()
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
                val segmentDuration = settings.segmentDurationSeconds.toLong().coerceAtLeast(1L)
                delay(segmentDuration * 1000L)
                try {
                    audioRecorder.rotateSegment()
                    broadcastStatus()
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
            .setRequiresBatteryNotLow(false)
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
        audioRecorder.updateSettings(
            settings.audioBitrate * 1000, // convert kbps to bps
            settings.audioSampleRate,
            1 // Mono channel
        )
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
} 