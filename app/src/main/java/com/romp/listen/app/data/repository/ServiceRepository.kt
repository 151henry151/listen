package com.romp.listen.app.data.repository

import android.content.Context
import com.romp.listen.app.audio.AudioRecorderService
import com.romp.listen.app.service.ListenForegroundService
import com.romp.listen.app.service.ServiceHealthMonitor
import com.romp.listen.app.service.ServiceStateManager
import com.romp.listen.app.settings.SettingsManager
import com.romp.listen.app.storage.StorageManager
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for service management operations
 */
class ServiceRepository(
    private val context: Context,
    private val settings: SettingsManager,
    private val storageManager: StorageManager,
    private val audioRecorder: AudioRecorderService,
    private val serviceStateManager: ServiceStateManager,
    private val healthMonitor: ServiceHealthMonitor
) {
    
    /** Check if service is enabled */
    fun isServiceEnabled(): Boolean = settings.isServiceEnabled
    
    /** Get service state flow */
    fun getServiceState(): StateFlow<Boolean> = serviceStateManager.serviceRunning
    
    /** Get rotation requested flow */
    fun getRotationRequested(): StateFlow<Boolean> = serviceStateManager.rotationRequested
    
    /** Start the recording service */
    fun startService() {
        settings.isServiceEnabled = true
        ListenForegroundService.start(context)
    }
    
    /** Stop the recording service */
    fun stopService() {
        settings.isServiceEnabled = false
        ListenForegroundService.stop(context)
    }
    
    /** Check if service is running */
    fun isServiceRunning(): Boolean = serviceStateManager.isServiceRunning()
    
    /** Request segment rotation */
    fun requestSegmentRotation() {
        serviceStateManager.requestSegmentRotation()
    }
    
    /** Acknowledge rotation request */
    fun acknowledgeRotationRequest() {
        serviceStateManager.acknowledgeRotationRequest()
    }
    
    /** Get current health status */
    suspend fun getHealthStatus() = healthMonitor.getCurrentHealth()
    
    /** Force health check */
    fun forceHealthCheck() {
        healthMonitor.forceHealthCheck()
    }
    
    /** Check if health monitoring is active */
    fun isHealthMonitoring(): Boolean = healthMonitor.isMonitoring()
    
    /** Get storage health report */
    suspend fun getStorageHealthReport() = storageManager.getStorageHealthReport()
    
    /** Get formatted storage usage */
    suspend fun getFormattedStorageUsage(): String = storageManager.getFormattedStorageUsage()
    
    /** Get formatted available storage */
    suspend fun getFormattedAvailableStorage(): String = storageManager.getFormattedAvailableStorage()
    
    /** Check if storage is healthy */
    suspend fun isStorageHealthy(requiredBytes: Long): Boolean = storageManager.isStorageHealthy(requiredBytes)
    
    /** Get debug info */
    fun getDebugInfo(): String = serviceStateManager.getDebugInfo()
} 