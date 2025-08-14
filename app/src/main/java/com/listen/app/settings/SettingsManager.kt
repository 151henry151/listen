package com.listen.app.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Manages app settings and configuration
 */
class SettingsManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    /** Whether the recording service is enabled */
    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_SERVICE_ENABLED, value) }
    
    /** Segment duration in seconds */
    var segmentDurationSeconds: Int
        get() = prefs.getInt(KEY_SEGMENT_DURATION, DEFAULT_SEGMENT_DURATION)
        set(value) = prefs.edit { putInt(KEY_SEGMENT_DURATION, value) }
    
    /** Retention period in minutes */
    var retentionPeriodMinutes: Int
        get() = prefs.getInt(KEY_RETENTION_PERIOD, DEFAULT_RETENTION_PERIOD)
        set(value) = prefs.edit { putInt(KEY_RETENTION_PERIOD, value) }
    
    /** Audio bitrate in kbps */
    var audioBitrate: Int
        get() = prefs.getInt(KEY_AUDIO_BITRATE, DEFAULT_AUDIO_BITRATE)
        set(value) = prefs.edit { putInt(KEY_AUDIO_BITRATE, value) }
    
    /** Audio sample rate in Hz */
    var audioSampleRate: Int
        get() = prefs.getInt(KEY_AUDIO_SAMPLE_RATE, DEFAULT_AUDIO_SAMPLE_RATE)
        set(value) = prefs.edit { putInt(KEY_AUDIO_SAMPLE_RATE, value) }
    
    /** Maximum storage usage in MB */
    var maxStorageMB: Int
        get() = prefs.getInt(KEY_MAX_STORAGE, DEFAULT_MAX_STORAGE)
        set(value) = prefs.edit { putInt(KEY_MAX_STORAGE, value) }
    
    /** Whether to auto-start on boot */
    var autoStartOnBoot: Boolean
        get() = prefs.getBoolean(KEY_AUTO_START_BOOT, true)
        set(value) = prefs.edit { putBoolean(KEY_AUTO_START_BOOT, value) }
    
    /** Last service start time */
    var lastServiceStartTime: Long
        get() = prefs.getLong(KEY_LAST_SERVICE_START, 0)
        set(value) = prefs.edit { putLong(KEY_LAST_SERVICE_START, value) }
    
    /** Calculate total storage usage for current settings */
    fun calculateStorageUsage(): Long {
        val segmentSizeBytes = (audioBitrate.toLong() * 1000 * segmentDurationSeconds) / 8
        val segmentsCount = (retentionPeriodMinutes * 60) / segmentDurationSeconds
        return segmentSizeBytes * segmentsCount
    }
    
    /** Calculate storage usage with margin (default 20% margin) */
    fun calculateStorageUsageWithMargin(marginPercent: Double = 20.0): Long {
        val baseUsage = calculateStorageUsage()
        val margin = (baseUsage * marginPercent) / 100.0
        return (baseUsage + margin).toLong()
    }
    
    /** Whether to show notifications */
    var showNotification: Boolean
        get() = prefs.getBoolean(KEY_SHOW_NOTIFICATION, true)
        set(value) = prefs.edit { putBoolean(KEY_SHOW_NOTIFICATION, value) }
    
    /** Get recent segments (placeholder - would need database access) */
    fun getRecentSegments(limit: Int = 10): List<String> {
        // This is a placeholder implementation
        // In a real implementation, this would query the database
        return emptyList()
    }
    
    /** Get formatted storage usage string */
    fun getFormattedStorageUsage(): String {
        val usageBytes = calculateStorageUsage()
        return when {
            usageBytes >= 1024 * 1024 * 1024 -> {
                val gb = usageBytes / (1024.0 * 1024.0 * 1024.0)
                String.format("%.1f GB", gb)
            }
            usageBytes >= 1024 * 1024 -> {
                val mb = usageBytes / (1024.0 * 1024.0)
                String.format("%.1f MB", mb)
            }
            usageBytes >= 1024 -> {
                val kb = usageBytes / 1024.0
                String.format("%.1f KB", kb)
            }
            else -> "$usageBytes B"
        }
    }
    
    companion object {
        private const val PREFS_NAME = "listen_settings"
        
        // Keys
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_SEGMENT_DURATION = "segment_duration"
        private const val KEY_RETENTION_PERIOD = "retention_period"
        private const val KEY_AUDIO_BITRATE = "audio_bitrate"
        private const val KEY_AUDIO_SAMPLE_RATE = "audio_sample_rate"
        private const val KEY_MAX_STORAGE = "max_storage"
        private const val KEY_AUTO_START_BOOT = "auto_start_boot"
        private const val KEY_LAST_SERVICE_START = "last_service_start"
        private const val KEY_SHOW_NOTIFICATION = "show_notification"
        
        // Default values
        const val DEFAULT_SEGMENT_DURATION = 60 // 1 minute
        const val DEFAULT_RETENTION_PERIOD = 10 // 10 minutes
        const val DEFAULT_AUDIO_BITRATE = 32 // 32 kbps
        const val DEFAULT_AUDIO_SAMPLE_RATE = 16000 // 16 kHz
        const val DEFAULT_MAX_STORAGE = 100 // 100 MB
    }
} 