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
    
    /** Whether auto music mode is enabled */
    var autoMusicModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_MUSIC_MODE, false)
        set(value) = prefs.edit { putBoolean(KEY_AUTO_MUSIC_MODE, value) }
    
    /** Retention period in minutes */
    var retentionPeriodMinutes: Int
        get() = prefs.getInt(KEY_RETENTION_PERIOD, DEFAULT_RETENTION_PERIOD)
        set(value) = prefs.edit { putInt(KEY_RETENTION_PERIOD, value) }
    
    /** Audio quality preset */
    var audioQualityPreset: AudioQualityPreset
        get() {
            val presetOrdinal = prefs.getInt(KEY_AUDIO_QUALITY_PRESET, AudioQualityPreset.MEDIUM.ordinal)
            return AudioQualityPreset.values()[presetOrdinal]
        }
        set(value) = prefs.edit { putInt(KEY_AUDIO_QUALITY_PRESET, value.ordinal) }
    
    /** Audio bitrate in kbps (computed from quality preset) */
    val audioBitrate: Int
        get() = audioQualityPreset.bitrate
    
    /** Audio sample rate in Hz (computed from quality preset) */
    val audioSampleRate: Int
        get() = audioQualityPreset.sampleRate
    
    /** Maximum storage usage in MB */
    var maxStorageMB: Int
        get() = prefs.getInt(KEY_MAX_STORAGE, DEFAULT_MAX_STORAGE)
        set(value) = prefs.edit { putInt(KEY_MAX_STORAGE, value) }
    
    /** Whether to auto-start on boot */
    var autoStartOnBoot: Boolean
        get() = prefs.getBoolean(KEY_AUTO_START_BOOT, true)
        set(value) = prefs.edit { putBoolean(KEY_AUTO_START_BOOT, value) }
    
    /** Whether recording was active when the device shut down */
    var wasRecordingOnShutdown: Boolean
        get() = prefs.getBoolean(KEY_WAS_RECORDING_ON_SHUTDOWN, false)
        set(value) = prefs.edit { putBoolean(KEY_WAS_RECORDING_ON_SHUTDOWN, value) }
    
    /** Last service start time */
    var lastServiceStartTime: Long
        get() = prefs.getLong(KEY_LAST_SERVICE_START, 0)
        set(value) = prefs.edit { putLong(KEY_LAST_SERVICE_START, value) }

    /** Whether power-saving mode is enabled by the user */
    var powerSavingModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_POWER_SAVING_MODE, false)
        set(value) = prefs.edit { putBoolean(KEY_POWER_SAVING_MODE, value) }

    /** Whether adaptive performance behaviors are enabled */
    var adaptivePerformanceEnabled: Boolean
        get() = prefs.getBoolean(KEY_ADAPTIVE_PERFORMANCE, true)
        set(value) = prefs.edit { putBoolean(KEY_ADAPTIVE_PERFORMANCE, value) }
    
    /** Calculate total storage usage for current settings */
    fun calculateStorageUsage(): Long {
        val segmentSeconds = if (autoMusicModeEnabled) AUTO_MUSIC_TARGET_SECONDS else segmentDurationSeconds
        val segmentSizeBytes = (audioBitrate.toLong() * 1000 * segmentSeconds) / 8
        val segmentsCount = (retentionPeriodMinutes * 60) / segmentSeconds
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
    
    /** Whether user has consented to audio recording */
    var hasUserConsentedToRecording: Boolean
        get() = prefs.getBoolean(KEY_USER_CONSENTED_TO_RECORDING, false)
        set(value) = prefs.edit { putBoolean(KEY_USER_CONSENTED_TO_RECORDING, value) }
    
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
        private const val KEY_AUTO_MUSIC_MODE = "auto_music_mode"
        private const val KEY_RETENTION_PERIOD = "retention_period"
        private const val KEY_AUDIO_QUALITY_PRESET = "audio_quality_preset"
        private const val KEY_MAX_STORAGE = "max_storage"
        private const val KEY_AUTO_START_BOOT = "auto_start_boot"
        private const val KEY_WAS_RECORDING_ON_SHUTDOWN = "was_recording_on_shutdown"
        private const val KEY_LAST_SERVICE_START = "last_service_start"
        private const val KEY_POWER_SAVING_MODE = "power_saving_mode"
        private const val KEY_ADAPTIVE_PERFORMANCE = "adaptive_performance"
        private const val KEY_SHOW_NOTIFICATION = "show_notification"
        private const val KEY_USER_CONSENTED_TO_RECORDING = "user_consented_to_recording"
        
        // Default values
        const val DEFAULT_SEGMENT_DURATION = 60 // 1 minute
        const val DEFAULT_RETENTION_PERIOD = 10 // 10 minutes
        const val DEFAULT_MAX_STORAGE = 100 // 100 MB
        const val AUTO_MUSIC_TARGET_SECONDS = 300 // ~5 minutes
    }
}

/**
 * Audio quality presets for user-friendly selection
 */
enum class AudioQualityPreset(
    val displayName: String,
    val description: String,
    val bitrate: Int,
    val sampleRate: Int
) {
    LOW(
        displayName = "Low Quality",
        description = "Telephone quality - Best for battery life and storage",
        bitrate = 16, // 16 kbps
        sampleRate = 8000 // 8 kHz
    ),
    MEDIUM(
        displayName = "Medium Quality", 
        description = "Standard quality - Good balance of quality and efficiency",
        bitrate = 32, // 32 kbps
        sampleRate = 16000 // 16 kHz
    ),
    HIGH(
        displayName = "High Quality",
        description = "CD quality - Best audio fidelity, uses more storage",
        bitrate = 128, // 128 kbps
        sampleRate = 44100 // 44.1 kHz
    );
    
    /** Get formatted bitrate string */
    fun getBitrateString(): String = "${bitrate} kbps"
    
    /** Get formatted sample rate string */
    fun getSampleRateString(): String = "${sampleRate / 1000} kHz"
} 