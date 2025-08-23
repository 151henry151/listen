package com.romp.listen.app.domain.usecase

import com.romp.listen.app.data.repository.ServiceRepository
import com.romp.listen.app.settings.SettingsManager
import com.romp.listen.app.storage.StorageManager
import com.romp.listen.app.storage.StorageManager.StorageHealthReport
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StartRecordingUseCaseTest {
    
    private lateinit var useCase: StartRecordingUseCase
    private lateinit var serviceRepository: ServiceRepository
    private lateinit var settings: SettingsManager
    private lateinit var storageManager: StorageManager
    
    @Before
    fun setUp() {
        serviceRepository = mockk()
        settings = mockk()
        storageManager = mockk()
        useCase = StartRecordingUseCase(serviceRepository, settings, storageManager)
    }
    
    @Test
    fun `invoke should return success when all conditions are met`() = runTest {
        // Given
        coEvery { serviceRepository.isServiceEnabled() } returns false
        coEvery { serviceRepository.getStorageHealthReport() } returns StorageHealthReport(
            currentUsage = 1024L,
            availableSpace = 1024 * 1024 * 100L, // 100MB
            isWritable = true,
            fileCount = 10,
            isHealthy = true
        )
        coEvery { settings.calculateStorageUsageWithMargin() } returns 1024 * 1024 * 50L // 50MB
        coEvery { serviceRepository.isStorageHealthy(any()) } returns true
        coEvery { serviceRepository.startService() } returns Unit
        
        // When
        val result = useCase.invoke()
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `invoke should return failure when service is already running`() = runTest {
        // Given
        coEvery { serviceRepository.isServiceEnabled() } returns true
        
        // When
        val result = useCase.invoke()
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Service is already running", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `invoke should return failure when storage is not healthy`() = runTest {
        // Given
        coEvery { serviceRepository.isServiceEnabled() } returns false
        coEvery { serviceRepository.getStorageHealthReport() } returns StorageHealthReport(
            currentUsage = 1024L,
            availableSpace = 1024L,
            isWritable = false,
            fileCount = 0,
            isHealthy = false
        )
        
        // When
        val result = useCase.invoke()
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Storage is not healthy", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `invoke should return failure when insufficient storage`() = runTest {
        // Given
        coEvery { serviceRepository.isServiceEnabled() } returns false
        coEvery { serviceRepository.getStorageHealthReport() } returns StorageHealthReport(
            currentUsage = 1024L,
            availableSpace = 1024 * 1024 * 100L, // 100MB
            isWritable = true,
            fileCount = 10,
            isHealthy = true
        )
        coEvery { settings.calculateStorageUsageWithMargin() } returns 1024 * 1024 * 200L // 200MB
        coEvery { serviceRepository.isStorageHealthy(any()) } returns false
        
        // When
        val result = useCase.invoke()
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Insufficient storage space", result.exceptionOrNull()?.message)
    }
} 