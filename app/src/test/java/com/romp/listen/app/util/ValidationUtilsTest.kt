package com.romp.listen.app.util

import com.romp.listen.app.data.Segment
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ValidationUtilsTest {
    
    @Test
    fun `validateAudioSettings should return success for valid settings`() {
        // When
        val result = ValidationUtils.validateAudioSettings(
            bitrate = 32000,
            sampleRate = 16000,
            channels = 1
        )
        
        // Then
        assertTrue(result.isSuccess())
        assertFalse(result.isError())
    }
    
    @Test
    fun `validateAudioSettings should return error for invalid bitrate`() {
        // When
        val result = ValidationUtils.validateAudioSettings(
            bitrate = 1000, // Too low
            sampleRate = 16000,
            channels = 1
        )
        
        // Then
        assertFalse(result.isSuccess())
        assertTrue(result.isError())
        assertTrue(result.getErrorMessages().any { it.contains("bitrate") })
    }
    
    @Test
    fun `validateAudioSettings should return error for invalid sample rate`() {
        // When
        val result = ValidationUtils.validateAudioSettings(
            bitrate = 32000,
            sampleRate = 1000, // Too low
            channels = 1
        )
        
        // Then
        assertFalse(result.isSuccess())
        assertTrue(result.isError())
        assertTrue(result.getErrorMessages().any { it.contains("sample rate") })
    }
    
    @Test
    fun `validateAudioSettings should return error for invalid channels`() {
        // When
        val result = ValidationUtils.validateAudioSettings(
            bitrate = 32000,
            sampleRate = 16000,
            channels = 3 // Invalid
        )
        
        // Then
        assertFalse(result.isSuccess())
        assertTrue(result.isError())
        assertTrue(result.getErrorMessages().any { it.contains("channels") })
    }
    
    @Test
    fun `validateStorageSettings should return success for valid settings`() {
        // When
        val result = ValidationUtils.validateStorageSettings(
            segmentDurationMinutes = 5,
            retentionDays = 7,
            maxStorageMB = 100
        )
        
        // Then
        assertTrue(result.isSuccess())
        assertFalse(result.isError())
    }
    
    @Test
    fun `validateStorageSettings should return error for invalid segment duration`() {
        // When
        val result = ValidationUtils.validateStorageSettings(
            segmentDurationMinutes = 0, // Too low
            retentionDays = 7,
            maxStorageMB = 100
        )
        
        // Then
        assertFalse(result.isSuccess())
        assertTrue(result.isError())
        assertTrue(result.getErrorMessages().any { it.contains("duration") })
    }
    
    @Test
    fun `validateSegment should return success for valid segment`() {
        // Given
        val segment = Segment(
            id = 1,
            startTime = 1000L,
            endTime = 2000L,
            duration = 1000L,
            filePath = "/test/path/file.aac",
            fileSize = 1024L
        )
        
        // When
        val result = ValidationUtils.validateSegment(segment)
        
        // Then
        assertTrue(result.isSuccess())
        assertFalse(result.isError())
    }
    
    @Test
    fun `validateSegment should return error for invalid start time`() {
        // Given
        val segment = Segment(
            id = 1,
            startTime = -1L, // Invalid
            endTime = 2000L,
            duration = 1000L,
            filePath = "/test/path/file.aac",
            fileSize = 1024L
        )
        
        // When
        val result = ValidationUtils.validateSegment(segment)
        
        // Then
        assertFalse(result.isSuccess())
        assertTrue(result.isError())
        assertTrue(result.getErrorMessages().any { it.contains("start time") })
    }
    
    @Test
    fun `validateSegment should return error for invalid end time`() {
        // Given
        val segment = Segment(
            id = 1,
            startTime = 2000L,
            endTime = 1000L, // Before start time
            duration = 1000L,
            filePath = "/test/path/file.aac",
            fileSize = 1024L
        )
        
        // When
        val result = ValidationUtils.validateSegment(segment)
        
        // Then
        assertFalse(result.isSuccess())
        assertTrue(result.isError())
        assertTrue(result.getErrorMessages().any { it.contains("end time") })
    }
    
    @Test
    fun `validateSegment should return error for empty file path`() {
        // Given
        val segment = Segment(
            id = 1,
            startTime = 1000L,
            endTime = 2000L,
            duration = 1000L,
            filePath = "", // Empty
            fileSize = 1024L
        )
        
        // When
        val result = ValidationUtils.validateSegment(segment)
        
        // Then
        assertFalse(result.isSuccess())
        assertTrue(result.isError())
        assertTrue(result.getErrorMessages().any { it.contains("file path") })
    }
    
    @Test
    fun `validateTimeRange should return success for valid range`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - 1000L
        val endTime = currentTime
        
        // When
        val result = ValidationUtils.validateTimeRange(startTime, endTime)
        
        // Then
        assertTrue(result.isSuccess())
        assertFalse(result.isError())
    }
    
    @Test
    fun `validateTimeRange should return error for negative start time`() {
        // When
        val result = ValidationUtils.validateTimeRange(-1L, 1000L)
        
        // Then
        assertFalse(result.isSuccess())
        assertTrue(result.isError())
        assertTrue(result.getErrorMessages().any { it.contains("negative") })
    }
    
    @Test
    fun `validateTimeRange should return error for end time before start time`() {
        // When
        val result = ValidationUtils.validateTimeRange(2000L, 1000L)
        
        // Then
        assertFalse(result.isSuccess())
        assertTrue(result.isError())
        assertTrue(result.getErrorMessages().any { it.contains("after start time") })
    }
    
    @Test
    fun `validateTimeRange should return error for future times`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val futureTime = currentTime + 10000L
        
        // When
        val result = ValidationUtils.validateTimeRange(futureTime, futureTime + 1000L)
        
        // Then
        assertFalse(result.isSuccess())
        assertTrue(result.isError())
        assertTrue(result.getErrorMessages().any { it.contains("future") })
    }
} 