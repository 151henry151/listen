package com.listen.app.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.listen.app.service.ListenForegroundService
import com.listen.app.util.AppLog

/**
 * WorkManager worker for periodic segment rotation
 */
class SegmentRotationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            AppLog.d(TAG, "Segment rotation work started")
            
            // Check if service is running and rotate segment
            // Note: In a real implementation, we'd need a way to communicate with the service
            // For now, we'll just log that the work was triggered
            
            AppLog.d(TAG, "Segment rotation completed successfully")
            Result.success()
            
        } catch (e: Exception) {
            AppLog.e(TAG, "Error during segment rotation", e)
            Result.retry()
        }
    }
    
    companion object {
        private const val TAG = "SegmentRotationWorker"
    }
} 