package com.listen.app.util

/**
 * Application constants
 */
object Constants {
    
    // Service constants
    const val NOTIFICATION_ID = 1001
    const val CHANNEL_ID = "listen_recording_channel"
    const val CHANNEL_NAME = "Listen Recording"
    
    // Audio constants
    const val DEFAULT_AUDIO_BITRATE = 32000
    const val DEFAULT_AUDIO_SAMPLE_RATE = 16000
    const val DEFAULT_AUDIO_CHANNELS = 1
    const val MIN_AUDIO_BITRATE = 16000
    const val MAX_AUDIO_BITRATE = 128000
    const val MIN_AUDIO_SAMPLE_RATE = 8000
    const val MAX_AUDIO_SAMPLE_RATE = 48000
    
    // Storage constants
    const val DEFAULT_SEGMENT_DURATION_MINUTES = 5
    const val DEFAULT_RETENTION_DAYS = 7
    const val DEFAULT_MAX_STORAGE_MB = 100
    const val MIN_SEGMENT_DURATION_MINUTES = 1
    const val MAX_SEGMENT_DURATION_MINUTES = 60
    const val MIN_RETENTION_DAYS = 1
    const val MAX_RETENTION_DAYS = 365
    const val MIN_MAX_STORAGE_MB = 10
    const val MAX_MAX_STORAGE_MB = 10000
    
    // Health monitoring constants
    const val HEALTH_CHECK_INTERVAL_MS = 30_000L
    const val RECORDING_CHECK_INTERVAL_MS = 10_000L
    const val MAX_CONSECUTIVE_FAILURES = 3
    const val MIN_RECORDING_DURATION_MS = 5_000L
    
    // Audio level monitoring constants
    const val AUDIO_LEVEL_UPDATE_INTERVAL_MS = 100L
    const val AUDIO_BUFFER_SIZE = 1024
    const val AUDIO_SAMPLE_RATE = 16000
    
    // WorkManager constants
    const val SEGMENT_ROTATION_WORK_NAME = "segment_rotation_work"
    const val SEGMENT_CLEANUP_WORK_NAME = "segment_cleanup_work"
    
    // File constants
    const val AUDIO_FILE_EXTENSION = ".aac"
    const val AUDIO_FILE_PREFIX = "segment_"
    
    // UI constants
    const val ANIMATION_DURATION_MS = 300L
    const val PROGRESS_UPDATE_INTERVAL_MS = 100L
    
    // Permission constants
    const val PERMISSION_REQUEST_CODE = 100
    const val NOTIFICATION_PERMISSION_REQUEST_CODE = 101
    
    // Error messages
    const val ERROR_SERVICE_ALREADY_RUNNING = "Service is already running"
    const val ERROR_SERVICE_NOT_RUNNING = "Service is not running"
    const val ERROR_STORAGE_NOT_HEALTHY = "Storage is not healthy"
    const val ERROR_INSUFFICIENT_STORAGE = "Insufficient storage space"
    const val ERROR_MICROPHONE_PERMISSION = "Microphone permission required"
    const val ERROR_RECORDING_FAILED = "Failed to start recording"
    const val ERROR_PLAYBACK_FAILED = "Failed to play audio"
    
    // Success messages
    const val SUCCESS_SERVICE_STARTED = "Recording service started"
    const val SUCCESS_SERVICE_STOPPED = "Recording service stopped"
    const val SUCCESS_SETTINGS_SAVED = "Settings saved successfully"
    const val SUCCESS_SETTINGS_RESET = "Settings reset to defaults"
    
    // Format constants
    const val TIME_FORMAT = "HH:mm:ss"
    const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val FILE_SIZE_FORMAT = "%.1f %s"
    
    // Validation constants
    const val MIN_FILE_SIZE_BYTES = 1024L
    const val MAX_FILE_SIZE_BYTES = 100 * 1024 * 1024L // 100MB
    
    // Recovery constants
    const val RECOVERY_DELAY_MS = 5000L
    const val MAX_RECOVERY_ATTEMPTS = 3
} 