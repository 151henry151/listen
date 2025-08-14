package com.listen.app.data.repository

import com.listen.app.data.Segment
import com.listen.app.data.SegmentDao
import com.listen.app.storage.StorageManager
import kotlinx.coroutines.flow.Flow

/**
 * Repository for segment data operations
 */
class SegmentRepository(
    private val segmentDao: SegmentDao,
    private val storageManager: StorageManager
) {
    
    /** Get all segments as a Flow */
    fun getAllSegments(): Flow<List<Segment>> = segmentDao.getAllSegments()
    
    /** Get segments older than timestamp */
    suspend fun getSegmentsOlderThan(timestamp: Long): List<Segment> {
        return segmentDao.getSegmentsOlderThan(timestamp)
    }
    
    /** Get segments within time range */
    suspend fun getSegmentsInTimeRange(startTime: Long, endTime: Long): List<Segment> {
        return segmentDao.getSegmentsInTimeRange(startTime, endTime)
    }
    
    /** Get recent segments within time window */
    suspend fun getRecentSegments(timeWindowMs: Long): List<Segment> {
        val currentTime = System.currentTimeMillis()
        val cutoffTime = currentTime - timeWindowMs
        return segmentDao.getSegmentsInTimeRange(cutoffTime, currentTime)
    }
    
    /** Add a new segment */
    suspend fun addSegment(segment: Segment): Long {
        return segmentDao.insertSegment(segment)
    }
    
    /** Delete segments */
    suspend fun deleteSegments(segments: List<Segment>) {
        val segmentIds = segments.map { it.id }
        segmentDao.deleteSegmentsByIds(segmentIds)
        
        // Also delete the actual files
        segments.forEach { segment ->
            storageManager.deleteFile(segment.filePath)
        }
    }
    
    /** Delete segment by ID */
    suspend fun deleteSegmentById(segmentId: Long) {
        val segment = segmentDao.getSegmentById(segmentId)
        segment?.let {
            segmentDao.deleteSegment(it)
            storageManager.deleteFile(it.filePath)
        }
    }
    
    /** Get segment by ID */
    suspend fun getSegmentById(segmentId: Long): Segment? {
        return segmentDao.getSegmentById(segmentId)
    }
    
    /** Get total storage usage */
    suspend fun getTotalStorageUsage(): Long {
        return segmentDao.getTotalStorageUsage() ?: 0L
    }
    
    /** Get segment count */
    suspend fun getSegmentCount(): Int {
        return segmentDao.getSegmentCount()
    }
    
    /** Get oldest segments for cleanup */
    suspend fun getOldestSegments(limit: Int): List<Segment> {
        return segmentDao.getOldestSegments(limit)
    }
    
    /** Validate segment file exists */
    suspend fun validateSegmentFile(segment: Segment): Boolean {
        return storageManager.validateFile(segment.filePath)
    }
} 