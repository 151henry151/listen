package com.listen.app.util

import com.listen.app.data.Segment
import java.io.File

/**
 * Validation utilities for data integrity and input validation
 */
object ValidationUtils {
    
    /**
     * Validate audio settings
     */
    fun validateAudioSettings(
        bitrate: Int,
        sampleRate: Int,
        channels: Int
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (bitrate < Constants.MIN_AUDIO_BITRATE || bitrate > Constants.MAX_AUDIO_BITRATE) {
            errors.add("Audio bitrate must be between ${Constants.MIN_AUDIO_BITRATE} and ${Constants.MAX_AUDIO_BITRATE} bps")
        }
        
        if (sampleRate < Constants.MIN_AUDIO_SAMPLE_RATE || sampleRate > Constants.MAX_AUDIO_SAMPLE_RATE) {
            errors.add("Audio sample rate must be between ${Constants.MIN_AUDIO_SAMPLE_RATE} and ${Constants.MAX_AUDIO_SAMPLE_RATE} Hz")
        }
        
        if (channels != 1 && channels != 2) {
            errors.add("Audio channels must be 1 (mono) or 2 (stereo)")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    /**
     * Validate storage settings
     */
    fun validateStorageSettings(
        segmentDurationMinutes: Int,
        retentionDays: Int,
        maxStorageMB: Int
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (segmentDurationMinutes < Constants.MIN_SEGMENT_DURATION_MINUTES || 
            segmentDurationMinutes > Constants.MAX_SEGMENT_DURATION_MINUTES) {
            errors.add("Segment duration must be between ${Constants.MIN_SEGMENT_DURATION_MINUTES} and ${Constants.MAX_SEGMENT_DURATION_MINUTES} minutes")
        }
        
        if (retentionDays < Constants.MIN_RETENTION_DAYS || 
            retentionDays > Constants.MAX_RETENTION_DAYS) {
            errors.add("Retention period must be between ${Constants.MIN_RETENTION_DAYS} and ${Constants.MAX_RETENTION_DAYS} days")
        }
        
        if (maxStorageMB < Constants.MIN_MAX_STORAGE_MB || 
            maxStorageMB > Constants.MAX_MAX_STORAGE_MB) {
            errors.add("Maximum storage must be between ${Constants.MIN_MAX_STORAGE_MB} and ${Constants.MAX_MAX_STORAGE_MB} MB")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    /**
     * Validate segment data
     */
    fun validateSegment(segment: Segment): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (segment.startTime <= 0) {
            errors.add("Segment start time must be positive")
        }
        
        if (segment.endTime <= segment.startTime) {
            errors.add("Segment end time must be after start time")
        }
        
        if (segment.duration <= 0) {
            errors.add("Segment duration must be positive")
        }
        
        if (segment.fileSize < Constants.MIN_FILE_SIZE_BYTES) {
            errors.add("Segment file size must be at least ${Constants.MIN_FILE_SIZE_BYTES} bytes")
        }
        
        if (segment.fileSize > Constants.MAX_FILE_SIZE_BYTES) {
            errors.add("Segment file size must be less than ${Constants.MAX_FILE_SIZE_BYTES} bytes")
        }
        
        if (segment.filePath.isBlank()) {
            errors.add("Segment file path cannot be empty")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    /**
     * Validate file exists and is readable
     */
    fun validateFile(file: File): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (!file.exists()) {
            errors.add("File does not exist: ${file.absolutePath}")
        } else {
            if (!file.canRead()) {
                errors.add("File is not readable: ${file.absolutePath}")
            }
            
            if (file.length() == 0L) {
                errors.add("File is empty: ${file.absolutePath}")
            }
            
            if (file.length() > Constants.MAX_FILE_SIZE_BYTES) {
                errors.add("File is too large: ${file.absolutePath}")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    /**
     * Validate storage path
     */
    fun validateStoragePath(path: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (path.isBlank()) {
            errors.add("Storage path cannot be empty")
        } else {
            val file = File(path)
            if (!file.exists()) {
                errors.add("Storage directory does not exist: $path")
            } else if (!file.isDirectory) {
                errors.add("Storage path is not a directory: $path")
            } else if (!file.canWrite()) {
                errors.add("Storage directory is not writable: $path")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    /**
     * Validate time range
     */
    fun validateTimeRange(startTime: Long, endTime: Long): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (startTime < 0) {
            errors.add("Start time cannot be negative")
        }
        
        if (endTime < 0) {
            errors.add("End time cannot be negative")
        }
        
        if (endTime <= startTime) {
            errors.add("End time must be after start time")
        }
        
        val currentTime = System.currentTimeMillis()
        if (startTime > currentTime) {
            errors.add("Start time cannot be in the future")
        }
        
        if (endTime > currentTime) {
            errors.add("End time cannot be in the future")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val errors: List<String>) : ValidationResult()
        
        fun isSuccess(): Boolean = this is Success
        fun isError(): Boolean = this is Error
        
        fun getErrorMessages(): List<String> = when (this) {
            is Success -> emptyList()
            is Error -> errors
        }
    }
} 