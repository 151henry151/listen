package com.listen.app.service

import android.content.Context
import android.util.Log
import com.listen.app.data.ListenDatabase
import com.listen.app.data.Segment
import com.listen.app.settings.SettingsManager
import com.listen.app.storage.StorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * Manages audio segment lifecycle, storage, and cleanup
 */
class SegmentManagerService(
    private val context: Context,
    private val database: ListenDatabase,
    private val storageManager: StorageManager,
    private val settings: SettingsManager
) {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val segmentDao = database.segmentDao()
    
    /** Add a new completed segment to the database */
    fun addSegment(file: File, startTime: Long, duration: Long) {
        scope.launch {
            try {
                val endTime = startTime + duration
                val fileSize = file.length()
                
                val segment = Segment(
                    filePath = file.absolutePath,
                    startTime = startTime,
                    endTime = endTime,
                    duration = duration,
                    fileSize = fileSize
                )
                
                val segmentId = segmentDao.insertSegment(segment)
                Log.d(TAG, "Added segment to database: ID=$segmentId, file=${file.name}")
                
                // Perform cleanup after adding new segment
                performCleanup()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error adding segment to database", e)
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
                Log.e(TAG, "Error during cleanup", e)
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
            Log.d(TAG, "Cleaning up ${oldSegments.size} old segments")
            
            oldSegments.forEach { segment ->
                if (storageManager.deleteFile(segment.filePath)) {
                    segmentDao.deleteSegment(segment)
                    Log.d(TAG, "Deleted old segment: ${segment.filePath}")
                } else {
                    Log.w(TAG, "Failed to delete old segment file: ${segment.filePath}")
                }
            }
        }
    }
    
    /** Enforce storage limits */
    private suspend fun enforceStorageLimits() {
        val maxStorageBytes = settings.maxStorageMB * 1024L * 1024L
        val currentUsage = segmentDao.getTotalStorageUsage() ?: 0L
        
        if (currentUsage > maxStorageBytes) {
            val excessBytes = currentUsage - maxStorageBytes
            val segmentsToDelete = segmentDao.getOldestSegments(
                (excessBytes / 1024).toInt() + 1 // Add buffer
            )
            
            Log.w(TAG, "Storage limit exceeded. Deleting ${segmentsToDelete.size} segments to free ${excessBytes} bytes")
            
            segmentsToDelete.forEach { segment ->
                if (storageManager.deleteFile(segment.filePath)) {
                    segmentDao.deleteSegment(segment)
                    Log.d(TAG, "Deleted segment for storage limit: ${segment.filePath}")
                }
            }
        }
    }
    
    /** Clean up orphaned files (files not in database) */
    private suspend fun cleanupOrphanedFiles() {
        val databaseFiles = segmentDao.getAllFilePaths().toSet()
        val deletedCount = storageManager.cleanupOrphanedFiles(databaseFiles)
        
        if (deletedCount > 0) {
            Log.d(TAG, "Cleaned up $deletedCount orphaned files")
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
            
            Log.w(TAG, "Emergency cleanup freed $freedBytes bytes")
            freedBytes
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during emergency cleanup", e)
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