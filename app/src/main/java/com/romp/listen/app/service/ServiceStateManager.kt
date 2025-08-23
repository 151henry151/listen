package com.romp.listen.app.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages shared state between WorkManager and ListenForegroundService
 * This allows WorkManager to communicate with the running service
 */
class ServiceStateManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    private val _rotationRequested = MutableStateFlow(false)
    val rotationRequested: StateFlow<Boolean> = _rotationRequested.asStateFlow()
    
    private val _serviceRunning = MutableStateFlow(false)
    val serviceRunning: StateFlow<Boolean> = _serviceRunning.asStateFlow()
    
    /** Request a segment rotation from the service */
    fun requestSegmentRotation() {
        Log.d(TAG, "Segment rotation requested")
        _rotationRequested.value = true
        prefs.edit {
            putLong(KEY_LAST_ROTATION_REQUEST, System.currentTimeMillis())
        }
    }
    
    /** Acknowledge that rotation request has been processed */
    fun acknowledgeRotationRequest() {
        Log.d(TAG, "Segment rotation request acknowledged")
        _rotationRequested.value = false
    }
    
    /** Set service running state */
    fun setServiceRunning(running: Boolean) {
        Log.d(TAG, "Service running state: $running")
        _serviceRunning.value = running
        prefs.edit {
            putBoolean(KEY_SERVICE_RUNNING, running)
            if (running) {
                putLong(KEY_SERVICE_START_TIME, System.currentTimeMillis())
            }
        }
    }
    
    /** Check if service is running */
    fun isServiceRunning(): Boolean = _serviceRunning.value
    
    /** Get last rotation request time */
    fun getLastRotationRequestTime(): Long = prefs.getLong(KEY_LAST_ROTATION_REQUEST, 0)
    
    /** Get service start time */
    fun getServiceStartTime(): Long = prefs.getLong(KEY_SERVICE_START_TIME, 0)
    
    /** Check if rotation is overdue */
    fun isRotationOverdue(segmentDurationSeconds: Int): Boolean {
        val lastRequest = getLastRotationRequestTime()
        val currentTime = System.currentTimeMillis()
        val expectedInterval = segmentDurationSeconds * 1000L
        
        return (currentTime - lastRequest) > expectedInterval
    }
    
    /** Get debug information */
    fun getDebugInfo(): String {
        val lastRequest = getLastRotationRequestTime()
        val serviceStart = getServiceStartTime()
        val currentTime = System.currentTimeMillis()
        
        return "ServiceRunning: ${_serviceRunning.value}, " +
               "RotationRequested: ${_rotationRequested.value}, " +
               "LastRequest: ${if (lastRequest > 0) "${(currentTime - lastRequest) / 1000}s ago" else "never"}, " +
               "ServiceStart: ${if (serviceStart > 0) "${(currentTime - serviceStart) / 1000}s ago" else "never"}"
    }
    
    companion object {
        private const val TAG = "ServiceStateManager"
        private const val PREFS_NAME = "service_state"
        private const val KEY_LAST_ROTATION_REQUEST = "last_rotation_request"
        private const val KEY_SERVICE_RUNNING = "service_running"
        private const val KEY_SERVICE_START_TIME = "service_start_time"
    }
} 