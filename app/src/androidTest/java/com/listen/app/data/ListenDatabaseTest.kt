package com.listen.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ListenDatabaseTest {
    
    private lateinit var database: ListenDatabase
    private lateinit var segmentDao: SegmentDao
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, ListenDatabase::class.java
        ).build()
        segmentDao = database.segmentDao()
    }
    
    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun insertAndReadSegment() = runTest {
        // Given
        val segment = Segment(
            id = 0, // Room will auto-generate
            startTime = 1000L,
            endTime = 2000L,
            duration = 1000L,
            filePath = "/test/path/file.aac",
            fileSize = 1024L
        )
        
        // When
        val insertedId = segmentDao.insertSegment(segment)
        val retrievedSegment = segmentDao.getSegmentById(insertedId)
        
        // Then
        assertNotNull(retrievedSegment)
        assertEquals(segment.startTime, retrievedSegment?.startTime)
        assertEquals(segment.endTime, retrievedSegment?.endTime)
        assertEquals(segment.duration, retrievedSegment?.duration)
        assertEquals(segment.filePath, retrievedSegment?.filePath)
        assertEquals(segment.fileSize, retrievedSegment?.fileSize)
    }
    
    @Test
    fun getAllSegments() = runTest {
        // Given
        val segment1 = Segment(
            id = 0,
            startTime = 1000L,
            endTime = 2000L,
            duration = 1000L,
            filePath = "/test/path/file1.aac",
            fileSize = 1024L
        )
        val segment2 = Segment(
            id = 0,
            startTime = 2000L,
            endTime = 3000L,
            duration = 1000L,
            filePath = "/test/path/file2.aac",
            fileSize = 2048L
        )
        
        // When
        segmentDao.insertSegment(segment1)
        segmentDao.insertSegment(segment2)
        
        val allSegments = segmentDao.getAllSegments().value
        
        // Then
        assertNotNull(allSegments)
        assertEquals(2, allSegments?.size)
        // Should be ordered by startTime DESC (newest first)
        assertEquals(2000L, allSegments?.get(0)?.startTime)
        assertEquals(1000L, allSegments?.get(1)?.startTime)
    }
    
    @Test
    fun getSegmentsInTimeRange() = runTest {
        // Given
        val segment1 = Segment(
            id = 0,
            startTime = 1000L,
            endTime = 2000L,
            duration = 1000L,
            filePath = "/test/path/file1.aac",
            fileSize = 1024L
        )
        val segment2 = Segment(
            id = 0,
            startTime = 2000L,
            endTime = 3000L,
            duration = 1000L,
            filePath = "/test/path/file2.aac",
            fileSize = 2048L
        )
        val segment3 = Segment(
            id = 0,
            startTime = 3000L,
            endTime = 4000L,
            duration = 1000L,
            filePath = "/test/path/file3.aac",
            fileSize = 3072L
        )
        
        // When
        segmentDao.insertSegment(segment1)
        segmentDao.insertSegment(segment2)
        segmentDao.insertSegment(segment3)
        
        val segmentsInRange = segmentDao.getSegmentsInTimeRange(1500L, 3500L)
        
        // Then
        assertEquals(2, segmentsInRange.size)
        assertTrue(segmentsInRange.any { it.startTime == 2000L })
        assertTrue(segmentsInRange.any { it.startTime == 3000L })
        assertFalse(segmentsInRange.any { it.startTime == 1000L })
    }
    
    @Test
    fun getSegmentsOlderThan() = runTest {
        // Given
        val segment1 = Segment(
            id = 0,
            startTime = 1000L,
            endTime = 2000L,
            duration = 1000L,
            filePath = "/test/path/file1.aac",
            fileSize = 1024L
        )
        val segment2 = Segment(
            id = 0,
            startTime = 2000L,
            endTime = 3000L,
            duration = 1000L,
            filePath = "/test/path/file2.aac",
            fileSize = 2048L
        )
        
        // When
        segmentDao.insertSegment(segment1)
        segmentDao.insertSegment(segment2)
        
        val oldSegments = segmentDao.getSegmentsOlderThan(2500L)
        
        // Then
        assertEquals(1, oldSegments.size)
        assertEquals(1000L, oldSegments[0].startTime)
    }
    
    @Test
    fun deleteSegment() = runTest {
        // Given
        val segment = Segment(
            id = 0,
            startTime = 1000L,
            endTime = 2000L,
            duration = 1000L,
            filePath = "/test/path/file.aac",
            fileSize = 1024L
        )
        
        // When
        val insertedId = segmentDao.insertSegment(segment)
        val retrievedBeforeDelete = segmentDao.getSegmentById(insertedId)
        segmentDao.deleteSegment(retrievedBeforeDelete!!)
        val retrievedAfterDelete = segmentDao.getSegmentById(insertedId)
        
        // Then
        assertNotNull(retrievedBeforeDelete)
        assertNull(retrievedAfterDelete)
    }
    
    @Test
    fun getTotalStorageUsage() = runTest {
        // Given
        val segment1 = Segment(
            id = 0,
            startTime = 1000L,
            endTime = 2000L,
            duration = 1000L,
            filePath = "/test/path/file1.aac",
            fileSize = 1024L
        )
        val segment2 = Segment(
            id = 0,
            startTime = 2000L,
            endTime = 3000L,
            duration = 1000L,
            filePath = "/test/path/file2.aac",
            fileSize = 2048L
        )
        
        // When
        segmentDao.insertSegment(segment1)
        segmentDao.insertSegment(segment2)
        
        val totalUsage = segmentDao.getTotalStorageUsage()
        
        // Then
        assertEquals(3072L, totalUsage) // 1024 + 2048
    }
    
    @Test
    fun getSegmentCount() = runTest {
        // Given
        val segment1 = Segment(
            id = 0,
            startTime = 1000L,
            endTime = 2000L,
            duration = 1000L,
            filePath = "/test/path/file1.aac",
            fileSize = 1024L
        )
        val segment2 = Segment(
            id = 0,
            startTime = 2000L,
            endTime = 3000L,
            duration = 1000L,
            filePath = "/test/path/file2.aac",
            fileSize = 2048L
        )
        
        // When
        segmentDao.insertSegment(segment1)
        segmentDao.insertSegment(segment2)
        
        val count = segmentDao.getSegmentCount()
        
        // Then
        assertEquals(2, count)
    }
} 