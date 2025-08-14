package com.listen.app.settings

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SettingsManagerTest {
    
    private lateinit var settingsManager: SettingsManager
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    
    @Before
    fun setUp() {
        context = mockk()
        sharedPreferences = mockk()
        editor = mockk()
        
        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        
        settingsManager = SettingsManager(context)
    }
    
    @Test
    fun `isServiceEnabled should return false by default`() {
        // Given
        every { sharedPreferences.getBoolean("service_enabled", false) } returns false
        
        // When
        val result = settingsManager.isServiceEnabled
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isServiceEnabled should return true when set`() {
        // Given
        every { sharedPreferences.getBoolean("service_enabled", false) } returns true
        
        // When
        val result = settingsManager.isServiceEnabled
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `setting isServiceEnabled should save to preferences`() {
        // When
        settingsManager.isServiceEnabled = true
        
        // Then
        verify { editor.putBoolean("service_enabled", true) }
        verify { editor.apply() }
    }
    
    @Test
    fun `segmentDurationSeconds should return default value`() {
        // Given
        every { sharedPreferences.getInt("segment_duration", 300) } returns 300
        
        // When
        val result = settingsManager.segmentDurationSeconds
        
        // Then
        assertEquals(300, result)
    }
    
    @Test
    fun `audioBitrate should return default value`() {
        // Given
        every { sharedPreferences.getInt("audio_bitrate", 32000) } returns 32000
        
        // When
        val result = settingsManager.audioBitrate
        
        // Then
        assertEquals(32000, result)
    }
    
    @Test
    fun `audioSampleRate should return default value`() {
        // Given
        every { sharedPreferences.getInt("audio_sample_rate", 16000) } returns 16000
        
        // When
        val result = settingsManager.audioSampleRate
        
        // Then
        assertEquals(16000, result)
    }
    
    @Test
    fun `maxStorageMB should return default value`() {
        // Given
        every { sharedPreferences.getInt("max_storage", 100) } returns 100
        
        // When
        val result = settingsManager.maxStorageMB
        
        // Then
        assertEquals(100, result)
    }
    
    @Test
    fun `calculateStorageUsage should return reasonable value`() {
        // Given
        every { sharedPreferences.getInt("audio_bitrate", 32000) } returns 32000
        every { sharedPreferences.getInt("segment_duration", 300) } returns 300
        every { sharedPreferences.getInt("retention_period", 10080) } returns 10080
        
        // When
        val result = settingsManager.calculateStorageUsage()
        
        // Then
        assertTrue(result > 0)
        // 32kbps * 300s / 8 * 1.05 * (10080 * 60 / 300) = reasonable size
    }
    
    @Test
    fun `calculateStorageUsageWithMargin should include margin`() {
        // Given
        every { sharedPreferences.getInt("audio_bitrate", 32000) } returns 32000
        every { sharedPreferences.getInt("segment_duration", 300) } returns 300
        every { sharedPreferences.getInt("retention_period", 10080) } returns 10080
        
        // When
        val baseUsage = settingsManager.calculateStorageUsage()
        val usageWithMargin = settingsManager.calculateStorageUsageWithMargin(20.0)
        
        // Then
        assertTrue(usageWithMargin > baseUsage)
        assertEquals(baseUsage * 1.2, usageWithMargin)
    }
    
    @Test
    fun `wouldExceedMaxStorage should return true when usage exceeds limit`() {
        // Given
        every { sharedPreferences.getInt("audio_bitrate", 32000) } returns 32000
        every { sharedPreferences.getInt("segment_duration", 300) } returns 300
        every { sharedPreferences.getInt("retention_period", 10080) } returns 10080
        every { sharedPreferences.getInt("max_storage", 100) } returns 1 // 1MB limit
        
        // When
        val result = settingsManager.wouldExceedMaxStorage()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `wouldExceedMaxStorage should return false when usage is within limit`() {
        // Given
        every { sharedPreferences.getInt("audio_bitrate", 32000) } returns 32000
        every { sharedPreferences.getInt("segment_duration", 300) } returns 300
        every { sharedPreferences.getInt("retention_period", 10080) } returns 10080
        every { sharedPreferences.getInt("max_storage", 100) } returns 10000 // 10GB limit
        
        // When
        val result = settingsManager.wouldExceedMaxStorage()
        
        // Then
        assertFalse(result)
    }
} 