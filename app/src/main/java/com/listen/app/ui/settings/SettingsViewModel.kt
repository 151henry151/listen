package com.listen.app.ui.settings

import androidx.lifecycle.viewModelScope
import com.listen.app.settings.SettingsManager
import com.listen.app.storage.StorageManager
import com.listen.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for SettingsActivity
 */
class SettingsViewModel(
    private val settings: SettingsManager,
    private val storageManager: StorageManager
) : BaseViewModel() {
    
    private val _recordingDuration = MutableStateFlow(settings.segmentDurationSeconds / 60)
    val recordingDuration: StateFlow<Int> = _recordingDuration.asStateFlow()
    
    private val _retentionDays = MutableStateFlow(settings.retentionPeriodMinutes / (24 * 60))
    val retentionDays: StateFlow<Int> = _retentionDays.asStateFlow()
    
    private val _maxStorageMB = MutableStateFlow(settings.maxStorageMB)
    val maxStorageMB: StateFlow<Int> = _maxStorageMB.asStateFlow()
    
    private val _audioBitrate = MutableStateFlow(settings.audioBitrate)
    val audioBitrate: StateFlow<Int> = _audioBitrate.asStateFlow()
    
    private val _audioSampleRate = MutableStateFlow(settings.audioSampleRate)
    val audioSampleRate: StateFlow<Int> = _audioSampleRate.asStateFlow()
    
    private val _autoStartOnBoot = MutableStateFlow(settings.autoStartOnBoot)
    val autoStartOnBoot: StateFlow<Boolean> = _autoStartOnBoot.asStateFlow()
    
    private val _showNotifications = MutableStateFlow(settings.showNotification)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()
    
    private val _estimatedUsage = MutableStateFlow("0 MB")
    val estimatedUsage: StateFlow<String> = _estimatedUsage.asStateFlow()
    
    private val _storageWarning = MutableStateFlow<String?>(null)
    val storageWarning: StateFlow<String?> = _storageWarning.asStateFlow()
    
    init {
        updateEstimatedUsage()
    }
    
    /** Update recording duration */
    fun updateRecordingDuration(duration: Int) {
        _recordingDuration.value = duration
        updateEstimatedUsage()
    }
    
    /** Update retention days */
    fun updateRetentionDays(days: Int) {
        _retentionDays.value = days
        updateEstimatedUsage()
    }
    
    /** Update max storage */
    fun updateMaxStorage(mb: Int) {
        _maxStorageMB.value = mb
        updateEstimatedUsage()
    }
    
    /** Update audio bitrate */
    fun updateAudioBitrate(bitrate: Int) {
        _audioBitrate.value = bitrate
        updateEstimatedUsage()
    }
    
    /** Update audio sample rate */
    fun updateAudioSampleRate(sampleRate: Int) {
        _audioSampleRate.value = sampleRate
        updateEstimatedUsage()
    }
    
    /** Update auto start on boot */
    fun updateAutoStartOnBoot(enabled: Boolean) {
        _autoStartOnBoot.value = enabled
    }
    
    /** Update show notifications */
    fun updateShowNotifications(enabled: Boolean) {
        _showNotifications.value = enabled
    }
    
    /** Update estimated usage */
    private fun updateEstimatedUsage() {
        launchWithErrorHandling {
            val usage = settings.calculateStorageUsage()
            val usageMB = usage / (1024 * 1024)
            _estimatedUsage.value = "$usageMB MB"
            
            // Check if usage exceeds max storage
            val maxStorageBytes = _maxStorageMB.value * 1024L * 1024L
            if (usage > maxStorageBytes) {
                _storageWarning.value = "Estimated usage exceeds maximum storage limit"
            } else {
                _storageWarning.value = null
            }
        }
    }
    
    /** Save all settings */
    fun saveSettings() {
        launchWithErrorHandling {
            settings.apply {
                segmentDurationSeconds = _recordingDuration.value * 60
                retentionPeriodMinutes = _retentionDays.value * 24 * 60
                maxStorageMB = _maxStorageMB.value
                audioBitrate = _audioBitrate.value
                audioSampleRate = _audioSampleRate.value
                autoStartOnBoot = _autoStartOnBoot.value
                showNotification = _showNotifications.value
            }
            showSuccess("Settings saved successfully")
        }
    }
    
    /** Reset settings to defaults */
    fun resetToDefaults() {
        launchWithErrorHandling {
            // Reset to default values
            _recordingDuration.value = 5 // 5 minutes
            _retentionDays.value = 7 // 7 days
            _maxStorageMB.value = 100 // 100 MB
            _audioBitrate.value = 32000 // 32 kbps
            _audioSampleRate.value = 16000 // 16 kHz
            _autoStartOnBoot.value = true
            _showNotifications.value = true
            
            // Save the defaults
            saveSettings()
            showSuccess("Settings reset to defaults")
        }
    }
    
    /** Load current settings */
    fun loadCurrentSettings() {
        launchWithErrorHandling {
            _recordingDuration.value = settings.segmentDurationSeconds / 60
            _retentionDays.value = settings.retentionPeriodMinutes / (24 * 60)
            _maxStorageMB.value = settings.maxStorageMB
            _audioBitrate.value = settings.audioBitrate
            _audioSampleRate.value = settings.audioSampleRate
            _autoStartOnBoot.value = settings.autoStartOnBoot
            _showNotifications.value = settings.showNotification
            updateEstimatedUsage()
        }
    }
    
    /** Get available storage */
    suspend fun getAvailableStorage(): String {
        return storageManager.getFormattedAvailableStorage()
    }
    
    /** Check if settings are valid */
    fun validateSettings(): Boolean {
        return _recordingDuration.value > 0 &&
               _retentionDays.value > 0 &&
               _maxStorageMB.value > 0 &&
               _audioBitrate.value > 0 &&
               _audioSampleRate.value > 0
    }
    
    private fun showSuccess(message: String) {
        // Could be expanded to show success messages
    }
} 