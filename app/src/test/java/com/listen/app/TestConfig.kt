package com.listen.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Test configuration and utilities
 */
@OptIn(ExperimentalCoroutinesApi::class)
object TestConfig {
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    /**
     * Set up test environment
     */
    fun setupTestEnvironment() {
        Dispatchers.setMain(testDispatcher)
    }
    
    /**
     * Clean up test environment
     */
    fun cleanupTestEnvironment() {
        Dispatchers.resetMain()
    }
    
    /**
     * Get test dispatcher
     */
    fun getTestDispatcher(): TestDispatcher = testDispatcher
    
    /**
     * Test data constants
     */
    object TestData {
        const val TEST_SEGMENT_ID = 1L
        const val TEST_START_TIME = 1000L
        const val TEST_END_TIME = 2000L
        const val TEST_DURATION = 1000L
        const val TEST_FILE_SIZE = 1024L
        const val TEST_FILE_PATH = "/test/path/file.aac"
        
        const val TEST_AUDIO_BITRATE = 32000
        const val TEST_AUDIO_SAMPLE_RATE = 16000
        const val TEST_AUDIO_CHANNELS = 1
        
        const val TEST_SEGMENT_DURATION_MINUTES = 5
        const val TEST_RETENTION_DAYS = 7
        const val TEST_MAX_STORAGE_MB = 100
    }
} 