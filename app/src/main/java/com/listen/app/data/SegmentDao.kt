package com.listen.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Segment entities
 */
@Dao
interface SegmentDao {
    
    /** Get all segments ordered by start time (newest first) */
    @Query("SELECT * FROM segments ORDER BY startTime DESC")
    fun getAllSegments(): Flow<List<Segment>>
    
    /** Get segments within a time range */
    @Query("SELECT * FROM segments WHERE startTime >= :startTime AND endTime <= :endTime ORDER BY startTime DESC")
    fun getSegmentsInRange(startTime: Long, endTime: Long): Flow<List<Segment>>
    
    /** Get segments within a time range (suspend version) */
    @Query("SELECT * FROM segments WHERE startTime >= :startTime AND endTime <= :endTime ORDER BY startTime DESC")
    suspend fun getSegmentsInTimeRange(startTime: Long, endTime: Long): List<Segment>
    
    /** Get segments older than the given timestamp */
    @Query("SELECT * FROM segments WHERE endTime < :timestamp ORDER BY startTime ASC")
    suspend fun getSegmentsOlderThan(timestamp: Long): List<Segment>
    
    /** Get only rotating segments (not saved to Downloads) */
    @Query("SELECT * FROM segments WHERE isSavedToDownloads = 0 ORDER BY startTime DESC")
    suspend fun getRotatingSegments(): List<Segment>
    
    /** Get the oldest segments (for cleanup) */
    @Query("SELECT * FROM segments ORDER BY startTime ASC LIMIT :limit")
    suspend fun getOldestSegments(limit: Int): List<Segment>
    
    /** Get all file paths (for orphaned file cleanup) */
    @Query("SELECT filePath FROM segments")
    suspend fun getAllFilePaths(): List<String>
    
    /** Get total storage usage */
    @Query("SELECT SUM(fileSize) FROM segments")
    suspend fun getTotalStorageUsage(): Long?
    
    /** Get segment count */
    @Query("SELECT COUNT(*) FROM segments")
    suspend fun getSegmentCount(): Int
    
    /** Insert a new segment */
    @Insert
    suspend fun insertSegment(segment: Segment): Long
    
    /** Update an existing segment */
    @Update
    suspend fun updateSegment(segment: Segment)
    
    /** Delete a segment */
    @Delete
    suspend fun deleteSegment(segment: Segment)
    
    /** Delete segments by IDs */
    @Query("DELETE FROM segments WHERE id IN (:segmentIds)")
    suspend fun deleteSegmentsByIds(segmentIds: List<Long>)
    
    /** Delete all segments */
    @Query("DELETE FROM segments")
    suspend fun deleteAllSegments()
    
    /** Delete only rotating segments (not saved to Downloads) */
    @Query("DELETE FROM segments WHERE isSavedToDownloads = 0")
    suspend fun deleteRotatingSegments()
    
    /** Get segment by ID */
    @Query("SELECT * FROM segments WHERE id = :segmentId")
    suspend fun getSegmentById(segmentId: Long): Segment?
} 