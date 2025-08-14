package com.listen.app.audio

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import com.listen.app.storage.StorageManager
import java.io.File
import com.listen.app.util.AppLog

/**
 * Handles continuous audio recording with optimal settings for speech clarity
 */
class AudioRecorderService(
    private val context: Context,
    private val storageManager: StorageManager
) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var currentSegmentFile: File? = null
    private var segmentStartTime: Long = 0
    private var isRecording = false
    
    /** Current audio settings */
    private var audioBitrate: Int = 32000 // 32 kbps
    private var audioSampleRate: Int = 16000 // 16 kHz
    private var audioChannels: Int = 1 // Mono
    
    /** Callback for when a segment is completed */
    var onSegmentCompleted: ((File, Long, Long) -> Unit)? = null
    
    /** Start recording with current settings */
    fun startRecording(): Boolean {
        return tryStartRecordingWithRetry()
    }
    
    private fun tryStartRecordingWithRetry(maxAttempts: Int = 3): Boolean {
        var attempt = 0
        var delayMs = 250L
        var lastError: Exception? = null
        
        while (attempt < maxAttempts) {
            attempt++
            try {
                if (isRecording) {
                    AppLog.w(TAG, "Already recording, stopping current session first")
                    stopRecording()
                }
                
                val segmentFile = storageManager.createSegmentFile(System.currentTimeMillis())
                segmentStartTime = System.currentTimeMillis()
                
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(audioSampleRate)
                    setAudioChannels(audioChannels)
                    setAudioEncodingBitRate(audioBitrate)
                    setOutputFile(segmentFile.absolutePath)
                    
                    prepare()
                    start()
                }
                
                currentSegmentFile = segmentFile
                isRecording = true
                
                AppLog.d(TAG, "Started recording to: ${segmentFile.absolutePath} (attempt=$attempt)")
                return true
                
            } catch (e: Exception) {
                lastError = e
                AppLog.e(TAG, "Failed to start recording (attempt=$attempt/$maxAttempts)", e)
                cleanup()
                
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(delayMs)
                    } catch (_: InterruptedException) {
                        // ignore
                    }
                    delayMs = (delayMs * 2).coerceAtMost(2000L)
                }
            }
        }
        AppLog.e(TAG, "All attempts to start recording failed", lastError)
        return false
    }
    
    /** Stop current recording and return the completed file */
    fun stopRecording(): File? {
        return try {
            if (!isRecording) {
                AppLog.w(TAG, "Not currently recording")
                return null
            }
            
            val completedFile = currentSegmentFile
            val endTime = System.currentTimeMillis()
            val duration = endTime - segmentStartTime
            
            mediaRecorder?.apply {
                stop()
                release()
            }
            
            mediaRecorder = null
            isRecording = false
            
            AppLog.d(TAG, "Stopped recording: ${completedFile?.absolutePath}, duration: ${duration}ms")
            
            // Notify completion
            completedFile?.let { file ->
                onSegmentCompleted?.invoke(file, segmentStartTime, duration)
            }
            
            completedFile
            
        } catch (e: Exception) {
            AppLog.e(TAG, "Error stopping recording", e)
            cleanup()
            null
        }
    }
    
    /** Rotate to a new segment (stop current, start new) */
    fun rotateSegment(): File? {
        val completedFile = stopRecording()
        
        // Start new segment immediately
        if (!startRecording()) {
            AppLog.e(TAG, "Failed to start new segment after rotation")
        }
        
        return completedFile
    }
    
    /** Update audio settings */
    fun updateSettings(bitrate: Int, sampleRate: Int, channels: Int) {
        audioBitrate = bitrate
        audioSampleRate = sampleRate
        audioChannels = channels
        
        AppLog.d(TAG, "Updated audio settings: ${bitrate}bps, ${sampleRate}Hz, ${channels}ch")
    }
    
    /** Check if currently recording */
    fun isRecording(): Boolean = isRecording
    
    /** Get current recording duration */
    fun getCurrentRecordingDuration(): Long {
        return if (isRecording) {
            System.currentTimeMillis() - segmentStartTime
        } else {
            0
        }
    }
    
    /** Get current segment file */
    fun getCurrentSegmentFile(): File? = currentSegmentFile
    
    /** Clean up resources */
    fun cleanup() {
        try {
            if (isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            }
        } catch (e: Exception) {
            AppLog.e(TAG, "Error during cleanup", e)
        } finally {
            mediaRecorder = null
            currentSegmentFile = null
            isRecording = false
        }
    }
    
    companion object {
        private const val TAG = "AudioRecorderService"
        
        /** Default audio settings for optimal speech recording */
        const val DEFAULT_BITRATE = 32000 // 32 kbps
        const val DEFAULT_SAMPLE_RATE = 16000 // 16 kHz
        const val DEFAULT_CHANNELS = 1 // Mono
    }
} 