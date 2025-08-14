package com.listen.app.ui.main

import androidx.lifecycle.viewModelScope
import com.listen.app.data.repository.ServiceRepository
import com.listen.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for MainActivity
 */
class MainViewModel(
    private val serviceRepository: ServiceRepository
) : BaseViewModel() {
    
    private val _serviceState = MutableStateFlow(false)
    val serviceState: StateFlow<Boolean> = _serviceState.asStateFlow()
    
    private val _storageUsage = MutableStateFlow("0 MB")
    val storageUsage: StateFlow<String> = _storageUsage.asStateFlow()
    
    private val _availableStorage = MutableStateFlow("Available: 0 MB")
    val availableStorage: StateFlow<String> = _availableStorage.asStateFlow()
    
    private val _healthStatus = MutableStateFlow<ServiceHealthStatus?>(null)
    val healthStatus: StateFlow<ServiceHealthStatus?> = _healthStatus.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    /** Load initial data */
    private fun loadInitialData() {
        launchWithErrorHandling {
            updateServiceState()
            updateStorageInfo()
            updateHealthStatus()
        }
    }
    
    /** Update service state */
    private suspend fun updateServiceState() {
        _serviceState.value = serviceRepository.isServiceEnabled()
    }
    
    /** Update storage information */
    private suspend fun updateStorageInfo() {
        _storageUsage.value = serviceRepository.getFormattedStorageUsage()
        _availableStorage.value = "Available: ${serviceRepository.getFormattedAvailableStorage()}"
    }
    
    /** Update health status */
    private suspend fun updateHealthStatus() {
        val health = serviceRepository.getHealthStatus()
        _healthStatus.value = health?.let { ServiceHealthStatus.from(it) }
    }
    
    /** Start the recording service */
    fun startService() {
        launchWithErrorHandling {
            serviceRepository.startService()
            updateServiceState()
            showSuccess("Recording service started")
        }
    }
    
    /** Stop the recording service */
    fun stopService() {
        launchWithErrorHandling {
            serviceRepository.stopService()
            updateServiceState()
            showSuccess("Recording service stopped")
        }
    }
    
    /** Refresh data */
    fun refreshData() {
        launchWithErrorHandling {
            updateServiceState()
            updateStorageInfo()
            updateHealthStatus()
        }
    }
    
    /** Force health check */
    fun forceHealthCheck() {
        launchWithErrorHandling {
            serviceRepository.forceHealthCheck()
            updateHealthStatus()
        }
    }
    
    /** Get debug info */
    fun getDebugInfo(): String {
        return serviceRepository.getDebugInfo()
    }
    
    private fun showSuccess(message: String) {
        // Could be expanded to show success messages
    }
    
    data class ServiceHealthStatus(
        val isHealthy: Boolean,
        val isRecording: Boolean,
        val recordingDuration: Long,
        val storageHealthy: Boolean,
        val audioDeviceHealthy: Boolean,
        val uptime: Long,
        val consecutiveFailures: Int
    ) {
        companion object {
            fun from(healthReport: com.listen.app.service.ServiceHealthMonitor.ServiceHealthReport): ServiceHealthStatus {
                return ServiceHealthStatus(
                    isHealthy = healthReport.isHealthy,
                    isRecording = healthReport.isRecording,
                    recordingDuration = healthReport.recordingDuration,
                    storageHealthy = healthReport.storageHealthy,
                    audioDeviceHealthy = healthReport.audioDeviceHealthy,
                    uptime = healthReport.uptime,
                    consecutiveFailures = healthReport.consecutiveFailures
                )
            }
        }
    }
} 