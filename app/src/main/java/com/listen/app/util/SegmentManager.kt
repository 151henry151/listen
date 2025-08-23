package com.listen.app.util

import android.content.Context
import android.util.Log
import com.listen.app.data.ListenDatabase
import com.listen.app.data.Segment
import kotlinx.coroutines.flow.first
import java.io.File

/**
 * Utility class for managing segment operations including deletion
 */
object SegmentManager {
    
    private const val TAG = "SegmentManager"
    
    /**
     * Delete a single segment
     * @param context Application context
     * @param segment The segment to delete
     * @return true if successful, false otherwise
     */
    suspend fun deleteSegment(context: Context, segment: Segment): Boolean {
        return try {
            val database = ListenDatabase.getDatabase(context)
            
            // Delete the file
            val file = File(segment.filePath)
            val fileDeleted = if (file.exists()) {
                file.delete()
            } else {
                Log.w(TAG, "Segment file does not exist: ${segment.filePath}")
                true // Consider it "deleted" if it doesn't exist
            }
            
            // Delete from database
            database.segmentDao().deleteSegment(segment)
            
            Log.d(TAG, "Successfully deleted segment: ${segment.filePath}")
            fileDeleted
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting segment", e)
            false
        }
    }
    
    /**
     * Delete all segments
     * @param context Application context
     * @return true if successful, false otherwise
     */
    suspend fun deleteAllSegments(context: Context): Boolean {
        return try {
            val database = ListenDatabase.getDatabase(context)
            
            // Get all segments - use a one-time collection
            val segments = database.segmentDao().getAllSegments().first()
            
            var successCount = 0
            val totalCount = segments.size
            
            // Delete each file
            for (segment in segments) {
                val file = File(segment.filePath)
                if (file.exists() && file.delete()) {
                    successCount++
                } else if (!file.exists()) {
                    successCount++ // Consider it "deleted" if it doesn't exist
                }
            }
            
            // Clear database
            database.segmentDao().deleteAllSegments()
            
            Log.d(TAG, "Successfully deleted $successCount/$totalCount segments")
            successCount == totalCount
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all segments", e)
            false
        }
    }
    
    /**
     * Delete only rotating segments (not saved to Downloads)
     * @param context Application context
     * @return true if successful, false otherwise
     */
    suspend fun deleteRotatingSegments(context: Context): Boolean {
        return try {
            val database = ListenDatabase.getDatabase(context)
            
            // Get only rotating segments
            val rotatingSegments = database.segmentDao().getRotatingSegments()
            
            var successCount = 0
            var totalCount = rotatingSegments.size
            
            // Delete each file
            for (segment in rotatingSegments) {
                val file = File(segment.filePath)
                if (file.exists() && file.delete()) {
                    successCount++
                } else if (!file.exists()) {
                    successCount++ // Consider it "deleted" if it doesn't exist
                }
            }
            
            // Clear only rotating segments from database
            database.segmentDao().deleteRotatingSegments()
            
            Log.d(TAG, "Successfully deleted $successCount/$totalCount rotating segments")
            successCount == totalCount
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting rotating segments", e)
            false
        }
    }
    
    /**
     * Get the total number of segments
     * @param context Application context
     * @return Number of segments
     */
    suspend fun getSegmentCount(context: Context): Int {
        return try {
            val database = ListenDatabase.getDatabase(context)
            database.segmentDao().getSegmentCount()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting segment count", e)
            0
        }
    }
    
    /**
     * Get total storage used by segments
     * @param context Application context
     * @return Total size in bytes
     */
    suspend fun getTotalStorageUsed(context: Context): Long {
        return try {
            val database = ListenDatabase.getDatabase(context)
            val segments = mutableListOf<Segment>()
            database.segmentDao().getAllSegments().collect { segmentList ->
                segments.clear()
                segments.addAll(segmentList)
            }
            
            segments.sumOf { segment ->
                val file = File(segment.filePath)
                if (file.exists()) file.length() else 0L
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating total storage", e)
            0L
        }
    }
} 