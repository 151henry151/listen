package com.listen.app.service

import android.content.Context
import android.media.AudioManager
import android.media.AudioRecord
import android.util.Log
import com.listen.app.settings.SettingsManager
import com.listen.app.storage.StorageManager
import com.listen.app.audio.AudioRecorderService
import kotlinx.coroutines.*
import java.io.File

/**
 * Monitors the health of the ListenForegroundService and provides automatic recovery
 */
class ServiceHealthMonitor(
    private val context: Context,
    private val settings: SettingsManager,
    private val storageManager: StorageManager,
    private val audioRecorder: AudioRecorderService,
    private val segmentManager: SegmentManagerService
) {
    
    private val monitorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var healthCheckJob: Job? = null
    private var lastHealthCheck = 0L
    private var consecutiveFailures = 0
    private var lastRecordingCheck = 0L
    
    companion object {
        private const val TAG = "ServiceHealthMonitor"
        private const val HEALTH_CHECK_INTERVAL = 30_000L // 30 seconds
        private const val RECORDING_CHECK_INTERVAL = 10_000L // 10 seconds
        private const val MAX_CONSECUTIVE_FAILURES = 3
        private const val MIN_RECORDING_DURATION = 5_000L // 5 seconds
    }
    
    /** Start health monitoring */
    fun startMonitoring() {
        if (healthCheckJob?.isActive == true) {
            Log.d(TAG, "Health monitoring already active")
            return
        }
        
        healthCheckJob = monitorScope.launch {
            while (isActive) {
                try {
                    performHealthCheck()
                    delay(HEALTH_CHECK_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in health monitoring loop", e)
                    delay(HEALTH_CHECK_INTERVAL)
                }
            }
        }
        
        Log.d(TAG, "Health monitoring started")
    }
    
    /** Stop health monitoring */
    fun stopMonitoring() {
        healthCheckJob?.cancel()
        healthCheckJob = null
        Log.d(TAG, "Health monitoring stopped")
    }
    
    /** Perform comprehensive health check */
    private suspend fun performHealthCheck() {
        val currentTime = System.currentTimeMillis()
        lastHealthCheck = currentTime
        
        try {
            Log.d(TAG, "Performing health check...")
            
            val healthReport = generateHealthReport()
            
            if (!healthReport.isHealthy) {
                Log.w(TAG, "Service health check failed: $healthReport")
                consecutiveFailures++
                
                if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                    Log.e(TAG, "Too many consecutive failures, attempting recovery")
                    performRecovery()
                    consecutiveFailures = 0
                }
            } else {
                consecutiveFailures = 0
                Log.d(TAG, "Service health check passed")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during health check", e)
            consecutiveFailures++
        }
    }
    
    /** Generate comprehensive health report */
    private suspend fun generateHealthReport(): ServiceHealthReport {
        val currentTime = System.currentTimeMillis()
        
        // Check recording status
        val isRecording = audioRecorder.isRecording()
        val recordingDuration = audioRecorder.getCurrentRecordingDuration()
        val recordingHealthy = isRecording && recordingDuration > MIN_RECORDING_DURATION
        
        // Check storage health
        val storageHealth = storageManager.getStorageHealthReport()
        
        // Check audio device availability
        val audioDeviceHealthy = checkAudioDeviceHealth()
        
        // Check service uptime
        val serviceStartTime = settings.lastServiceStartTime
        val uptime = if (serviceStartTime > 0) currentTime - serviceStartTime else 0L
        
        // Check for recent activity
        val hasRecentActivity = checkRecentActivity()
        
        return ServiceHealthReport(
            timestamp = currentTime,
            isRecording = isRecording,
            recordingDuration = recordingDuration,
            recordingHealthy = recordingHealthy,
            storageHealthy = storageHealth.isHealthy,
            audioDeviceHealthy = audioDeviceHealthy,
            uptime = uptime,
            hasRecentActivity = hasRecentActivity,
            consecutiveFailures = consecutiveFailures,
            isHealthy = recordingHealthy && storageHealth.isHealthy && audioDeviceHealthy && hasRecentActivity
        )
    }
    
    /** Check if audio device is available and working */
    private fun checkAudioDeviceHealth(): Boolean {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            // Check if microphone is available
            val isMicrophoneAvailable = audioManager.getMode() != AudioManager.MODE_IN_CALL
            
            // Try to create a test AudioRecord to verify microphone access
            val testAudioRecord = AudioRecord(
                android.media.MediaRecorder.AudioSource.MIC,
                16000,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT,
                1024
            )
            
            val isAudioRecordAvailable = testAudioRecord.state == AudioRecord.STATE_INITIALIZED
            
            testAudioRecord.release()
            
            isMicrophoneAvailable && isAudioRecordAvailable
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking audio device health", e)
            false
        }
    }
    
    /** Check for recent activity (segments created, etc.) */
    private suspend fun checkRecentActivity(): Boolean {
        return try {
            // Check if segments have been created recently
            val recentSegments = segmentManager.getRecentSegments(5 * 60 * 1000L) // Last 5 minutes
            recentSegments.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking recent activity", e)
            false
        }
    }
    
    /** Perform recovery actions */
    private suspend fun performRecovery() {
        Log.w(TAG, "Starting service recovery...")
        
        try {
            // Step 1: Stop current recording
            audioRecorder.cleanup()
            
            // Step 2: Check storage health
            val storageHealth = storageManager.getStorageHealthReport()
            if (!storageHealth.isHealthy) {
                Log.w(TAG, "Storage is unhealthy, performing cleanup")
                segmentManager.performCleanup()
            }
            
            // Step 3: Restart recording
            val recordingStarted = audioRecorder.startRecording()
            if (!recordingStarted) {
                Log.e(TAG, "Failed to restart recording during recovery")
                // Try one more time after a delay
                delay(5000)
                audioRecorder.startRecording()
            }
            
            Log.d(TAG, "Service recovery completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during service recovery", e)
        }
    }
    
    /** Get current health status */
    suspend fun getCurrentHealth(): ServiceHealthReport {
        return generateHealthReport()
    }
    
    /** Force a health check */
    fun forceHealthCheck() {
        monitorScope.launch {
            performHealthCheck()
        }
    }
    
    /** Check if monitoring is active */
    fun isMonitoring(): Boolean = healthCheckJob?.isActive == true
    
    data class ServiceHealthReport(
        val timestamp: Long,
        val isRecording: Boolean,
        val recordingDuration: Long,
        val recordingHealthy: Boolean,
        val storageHealthy: Boolean,
        val audioDeviceHealthy: Boolean,
        val uptime: Long,
        val hasRecentActivity: Boolean,
        val consecutiveFailures: Int,
        val isHealthy: Boolean
    ) {
        override fun toString(): String {
            return "ServiceHealthReport(" +
                    "recording=$isRecording, " +
                    "recordingHealthy=$recordingHealthy, " +
                    "storageHealthy=$storageHealthy, " +
                    "audioDeviceHealthy=$audioDeviceHealthy, " +
                    "uptime=${uptime / 1000}s, " +
                    "hasRecentActivity=$hasRecentActivity, " +
                    "failures=$consecutiveFailures, " +
                    "isHealthy=$isHealthy)"
        }
    }
} 