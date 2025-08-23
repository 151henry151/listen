package com.romp.listen.app.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import com.romp.listen.app.settings.SettingsManager
import com.romp.listen.app.storage.StorageManager
import com.romp.listen.app.audio.AudioRecorderService
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Monitors audio levels in real-time for visual feedback
 */
class AudioLevelMonitor(
    private val context: Context
) {
    
    private var audioRecord: AudioRecord? = null
    private var isMonitoring = false
    private val monitorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitoringJob: Job? = null
    
    /** Callback for audio level updates */
    var onAudioLevelChanged: ((Float) -> Unit)? = null
    
    companion object {
        private const val TAG = "AudioLevelMonitor"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE = 1024
        private const val UPDATE_INTERVAL = 100L // 100ms updates
    }
    
    /** Start monitoring audio levels */
    fun startMonitoring(): Boolean {
        if (isMonitoring) {
            Log.w(TAG, "Already monitoring audio levels")
            return true
        }
        
        // Check microphone permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Microphone permission not granted")
            return false
        }
        
        return try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "Failed to initialize AudioRecord")
                return false
            }
            
            isMonitoring = true
            
            monitoringJob = monitorScope.launch {
                val buffer = ShortArray(BUFFER_SIZE)
                
                audioRecord?.startRecording()
                
                while (isActive && isMonitoring) {
                    try {
                        val readSize = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0
                        
                        if (readSize > 0) {
                            val amplitude = calculateAmplitude(buffer, readSize)
                            val dbLevel = amplitudeToDb(amplitude)
                            
                            onAudioLevelChanged?.invoke(dbLevel)
                        }
                        
                        delay(UPDATE_INTERVAL)
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Error reading audio data", e)
                        delay(UPDATE_INTERVAL)
                    }
                }
            }
            
            Log.d(TAG, "Audio level monitoring started")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio level monitoring", e)
            cleanup()
            false
        }
    }
    
    /** Stop monitoring audio levels */
    fun stopMonitoring() {
        isMonitoring = false
        monitoringJob?.cancel()
        monitoringJob = null
        cleanup()
        Log.d(TAG, "Audio level monitoring stopped")
    }
    
    /** Calculate RMS amplitude from audio buffer */
    private fun calculateAmplitude(buffer: ShortArray, readSize: Int): Float {
        var sum = 0.0
        for (i in 0 until readSize) {
            sum += buffer[i] * buffer[i]
        }
        return sqrt(sum / readSize).toFloat()
    }
    
    /** Convert amplitude to decibels */
    private fun amplitudeToDb(amplitude: Float): Float {
        return if (amplitude > 0) {
            (20 * log10(amplitude / 32767.0)).toFloat()
        } else {
            -60f // Minimum dB level
        }
    }
    
    /** Clean up resources */
    private fun cleanup() {
        try {
            audioRecord?.apply {
                stop()
                release()
            }
            audioRecord = null
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up AudioRecord", e)
        }
    }
    
    /** Check if monitoring is active */
    fun isMonitoring(): Boolean = isMonitoring
    
    /** Get current audio level (0.0 to 1.0) for UI display */
    fun getNormalizedLevel(dbLevel: Float): Float {
        // Convert dB to normalized level (0.0 to 1.0)
        // Typical speech is around -30dB to -10dB
        val minDb = -60f
        val maxDb = -10f
        val normalized = (dbLevel - minDb) / (maxDb - minDb)
        return normalized.coerceIn(0f, 1f)
    }
} 