package com.listen.app.storage

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages file system operations for audio segments
 */
class StorageManager(private val context: Context) {
    
    private val segmentsDir: File = File(context.filesDir, "segments").apply {
        if (!exists()) {
            mkdirs()
        }
    }
    
    /** Get the segments directory */
    fun getSegmentsDirectory(): File = segmentsDir
    
    /** Create a new segment file with timestamp-based naming */
    fun createSegmentFile(timestamp: Long): File {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US)
        val fileName = "segment_${dateFormat.format(Date(timestamp))}.m4a"
        return File(segmentsDir, fileName)
    }
    
    /** Get current storage usage in bytes */
    fun getCurrentStorageUsage(): Long {
        return try {
            segmentsDir.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating storage usage", e)
            0L
        }
    }
    
    /** Get available storage space in bytes */
    fun getAvailableStorage(): Long {
        return try {
            segmentsDir.freeSpace
        } catch (e: Exception) {
            Log.e(TAG, "Error getting available storage", e)
            0L
        }
    }
    
    /** Clean up orphaned files (files not in database) */
    fun cleanupOrphanedFiles(databaseFilePaths: Set<String>): Int {
        return try {
            val actualFiles = segmentsDir.listFiles()
                ?.filter { it.isFile }
                ?.map { it.absolutePath }
                ?.toSet() ?: emptySet()
            
            val orphanedFiles = actualFiles - databaseFilePaths
            var deletedCount = 0
            
            orphanedFiles.forEach { filePath ->
                val file = File(filePath)
                if (file.delete()) {
                    deletedCount++
                    Log.d(TAG, "Deleted orphaned file: $filePath")
                } else {
                    Log.w(TAG, "Failed to delete orphaned file: $filePath")
                }
            }
            
            deletedCount
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up orphaned files", e)
            0
        }
    }
    
    /** Delete a specific file */
    fun deleteFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                true // File doesn't exist, consider it "deleted"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file: $filePath", e)
            false
        }
    }
    
    /** Get formatted storage usage string */
    fun getFormattedStorageUsage(): String {
        val usageBytes = getCurrentStorageUsage()
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
    
    /** Get formatted available storage string */
    fun getFormattedAvailableStorage(): String {
        val availableBytes = getAvailableStorage()
        return when {
            availableBytes >= 1024 * 1024 * 1024 -> {
                val gb = availableBytes / (1024.0 * 1024.0 * 1024.0)
                String.format("%.1f GB", gb)
            }
            availableBytes >= 1024 * 1024 -> {
                val mb = availableBytes / (1024.0 * 1024.0)
                String.format("%.1f MB", mb)
            }
            availableBytes >= 1024 -> {
                val kb = availableBytes / 1024.0
                String.format("%.1f KB", kb)
            }
            else -> "$availableBytes B"
        }
    }
    
    /** Check if storage is healthy (sufficient space) */
    fun isStorageHealthy(requiredBytes: Long): Boolean {
        return getAvailableStorage() >= requiredBytes
    }
    
    /** Emergency cleanup to free up space */
    fun emergencyCleanup(requiredBytes: Long): Long {
        val files = segmentsDir.listFiles()
            ?.filter { it.isFile }
            ?.sortedBy { it.lastModified() } // Delete oldest first
            ?: emptyList()
        
        var freedBytes = 0L
        var currentRequired = requiredBytes
        
        for (file in files) {
            if (currentRequired <= 0) break
            
            val fileSize = file.length()
            if (file.delete()) {
                freedBytes += fileSize
                currentRequired -= fileSize
                Log.d(TAG, "Emergency cleanup deleted: ${file.name} (${fileSize} bytes)")
            }
        }
        
        return freedBytes
    }
    
    companion object {
        private const val TAG = "StorageManager"
    }
} 