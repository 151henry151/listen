package com.listen.app.service

import android.content.Context
import android.util.Log
import com.listen.app.data.ListenDatabase
import com.listen.app.data.Segment
import com.listen.app.settings.SettingsManager
import com.listen.app.storage.StorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import com.listen.app.util.AppLog

/**
 * Manages audio segment lifecycle, storage, and cleanup
 */
class SegmentManagerService(
    private val context: Context,
    private val database: ListenDatabase,
    private val storageManager: StorageManager,
    private val settings: SettingsManager
) {
    
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val segmentDao = database.segmentDao()
    
    /** Allow owner to cancel background work */
    fun cancel() {
        scope.cancel()
    }
    
    /** Add a new completed segment to the database */
    fun addSegment(file: File, startTime: Long, duration: Long, isPhoneCall: Boolean = false, callDirection: String? = null, phoneNumber: String? = null) {
        scope.launch {
            try {
                val endTime = startTime + duration
                val fileSize = file.length()
                
                val segment = Segment(
                    filePath = file.absolutePath,
                    startTime = startTime,
                    endTime = endTime,
                    duration = duration,
                    fileSize = fileSize,
                    isPhoneCall = isPhoneCall,
                    callDirection = callDirection,
                    phoneNumber = phoneNumber
                )
                
                val segmentId = segmentDao.insertSegment(segment)
                AppLog.d(TAG, "Added segment to database: ID=$segmentId, file=${file.name}")
                
                // Perform cleanup after adding new segment
                performCleanup()
                
            } catch (e: Exception) {
                AppLog.e(TAG, "Error adding segment to database", e)
            }
        }
    }
    
    /** Perform cleanup of old segments and storage management */
    fun performCleanup() {
        scope.launch {
            try {
                // Clean up old segments based on retention period
                cleanupOldSegments()
                
                // Enforce storage limits
                enforceStorageLimits()
                
                // Clean up orphaned files
                cleanupOrphanedFiles()
                
            } catch (e: Exception) {
                AppLog.e(TAG, "Error during cleanup", e)
            }
        }
    }
    
    /** Clean up segments older than retention period */
    private suspend fun cleanupOldSegments() {
        val currentTime = System.currentTimeMillis()
        val retentionPeriodMs = settings.retentionPeriodMinutes * 60 * 1000L
        val cutoffTime = currentTime - retentionPeriodMs
        
        val oldSegments = segmentDao.getSegmentsOlderThan(cutoffTime)
        
        if (oldSegments.isNotEmpty()) {
            AppLog.d(TAG, "Cleaning up ${oldSegments.size} old segments")
            
            oldSegments.forEach { segment ->
                if (storageManager.deleteFile(segment.filePath)) {
                    segmentDao.deleteSegment(segment)
                    AppLog.d(TAG, "Deleted old segment: ${segment.filePath}")
                } else {
                    AppLog.w(TAG, "Failed to delete old segment file: ${segment.filePath}")
                }
            }
        }
    }
    
    /** Enforce storage limits */
    private suspend fun enforceStorageLimits() {
        val maxStorageBytes = settings.maxStorageMB * 1024L * 1024L
        val currentUsage = segmentDao.getTotalStorageUsage() ?: 0L
        
        if (currentUsage > maxStorageBytes) {
            var excessBytes = currentUsage - maxStorageBytes
            AppLog.w(TAG, "Storage limit exceeded by ${excessBytes} bytes. Beginning targeted cleanup...")
            
            // Iteratively delete oldest segments until enough space is freed
            while (excessBytes > 0) {
                val oldestBatch = segmentDao.getOldestSegments(10)
                if (oldestBatch.isEmpty()) break
                
                for (segment in oldestBatch) {
                    if (excessBytes <= 0) break
                    val size = File(segment.filePath).length()
                    if (storageManager.deleteFile(segment.filePath)) {
                        segmentDao.deleteSegment(segment)
                        excessBytes -= size
                        AppLog.d(TAG, "Deleted segment (${size} bytes) for storage limit: ${segment.filePath}")
                    }
                }
            }
        }
    }
    
    /** Clean up orphaned files (files not in database) */
    private suspend fun cleanupOrphanedFiles() {
        val databaseFiles = segmentDao.getAllFilePaths().toSet()
        val deletedCount = storageManager.cleanupOrphanedFiles(databaseFiles)
        
        if (deletedCount > 0) {
            AppLog.d(TAG, "Cleaned up $deletedCount orphaned files")
        }
    }
    
    /** Emergency cleanup to free up space */
    fun emergencyCleanup(requiredBytes: Long): Long {
        return try {
            val freedBytes = storageManager.emergencyCleanup(requiredBytes)
            
            // Also clean up database entries for deleted files
            scope.launch {
                cleanupOrphanedFiles()
            }
            
            AppLog.w(TAG, "Emergency cleanup freed $freedBytes bytes")
            freedBytes
            
        } catch (e: Exception) {
            AppLog.e(TAG, "Error during emergency cleanup", e)
            0L
        }
    }
    
    /** Get storage statistics */
    suspend fun getStorageStats(): StorageStats {
        val totalUsage = segmentDao.getTotalStorageUsage() ?: 0L
        val segmentCount = segmentDao.getSegmentCount()
        val availableSpace = storageManager.getAvailableStorage()
        
        return StorageStats(
            totalUsage = totalUsage,
            segmentCount = segmentCount,
            availableSpace = availableSpace,
            formattedUsage = storageManager.getFormattedStorageUsage(),
            formattedAvailable = storageManager.getFormattedAvailableStorage()
        )
    }
    
    /** Delete all segments (for reset) */
    fun deleteAllSegments() {
        scope.launch {
            try {
                // Delete all files
                val allFiles = segmentDao.getAllFilePaths()
                allFiles.forEach { filePath ->
                    storageManager.deleteFile(filePath)
                }
                
                // Clear database
                segmentDao.deleteAllSegments()
                
                Log.d(TAG, "Deleted all segments")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting all segments", e)
            }
        }
    }
    
    /** Check if storage is healthy */
    fun isStorageHealthy(): Boolean {
        val requiredBytes = settings.calculateStorageUsage()
        return storageManager.isStorageHealthy(requiredBytes)
    }
    
    data class StorageStats(
        val totalUsage: Long,
        val segmentCount: Int,
        val availableSpace: Long,
        val formattedUsage: String,
        val formattedAvailable: String
    )
    
    companion object {
        private const val TAG = "SegmentManagerService"
    }
} 