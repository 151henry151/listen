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
    
    companion object {
        private const val TAG = "ListenForegroundService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "listen_recording_channel"
        
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
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
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
        }
        
        // Create notification channel
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        
        if (!isServiceRunning) {
            startForegroundService()
            startRecording()
            scheduleSegmentRotation()
            settings.lastServiceStartTime = System.currentTimeMillis()
        }
        
        // Return START_STICKY to restart service if killed
        return START_STICKY
    }
    
    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        stopRecording()
        isServiceRunning = false
        super.onDestroy()
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "App removed from recent tasks, restarting service")
        // Restart service if app is removed from recent tasks
        val restartServiceIntent = Intent(this, ListenForegroundService::class.java)
        startService(restartServiceIntent)
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    /** Start the foreground service with notification */
    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        isServiceRunning = true
        Log.d(TAG, "Started foreground service")
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
            .setContentTitle("Listen - Recording")
            .setContentText("Recording audio in background")
            .setSmallIcon(R.drawable.ic_mic)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
    
    /** Create notification channel for Android O+ */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Listen Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background audio recording service"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /** Start audio recording */
    private fun startRecording() {
        if (audioRecorder.startRecording()) {
            Log.d(TAG, "Audio recording started")
        } else {
            Log.e(TAG, "Failed to start audio recording")
        }
    }
    
    /** Stop audio recording */
    private fun stopRecording() {
        audioRecorder.stopRecording()
        Log.d(TAG, "Audio recording stopped")
    }
    
    /** Schedule periodic segment rotation using WorkManager */
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
        
        Log.d(TAG, "Scheduled segment rotation every $segmentDuration seconds")
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
            settings.audioBitrate,
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
} 