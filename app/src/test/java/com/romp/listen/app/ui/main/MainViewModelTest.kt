package com.romp.listen.app.ui.main

import com.romp.listen.app.data.repository.ServiceRepository
import com.romp.listen.app.service.ServiceHealthMonitor
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    
    private lateinit var viewModel: MainViewModel
    private lateinit var serviceRepository: ServiceRepository
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        serviceRepository = mockk()
        viewModel = MainViewModel(serviceRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadInitialData should update service state`() = runTest {
        // Given
        coEvery { serviceRepository.isServiceEnabled() } returns true
        coEvery { serviceRepository.getFormattedStorageUsage() } returns "50 MB"
        coEvery { serviceRepository.getFormattedAvailableStorage() } returns "1 GB"
        coEvery { serviceRepository.getHealthStatus() } returns ServiceHealthMonitor.ServiceHealthReport(
            timestamp = System.currentTimeMillis(),
            isRecording = true,
            recordingDuration = 5000L,
            recordingHealthy = true,
            storageHealthy = true,
            audioDeviceHealthy = true,
            uptime = 10000L,
            hasRecentActivity = true,
            consecutiveFailures = 0,
            isHealthy = true
        )
        
        // When
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(true, viewModel.serviceState.value)
        assertEquals("50 MB", viewModel.storageUsage.value)
        assertEquals("Available: 1 GB", viewModel.availableStorage.value)
        assertNotNull(viewModel.healthStatus.value)
        assertEquals(true, viewModel.healthStatus.value?.isHealthy)
    }
    
    @Test
    fun `startService should call repository and update state`() = runTest {
        // Given
        coEvery { serviceRepository.isServiceEnabled() } returns false
        coEvery { serviceRepository.startService() } returns Unit
        coEvery { serviceRepository.getFormattedStorageUsage() } returns "50 MB"
        coEvery { serviceRepository.getFormattedAvailableStorage() } returns "1 GB"
        coEvery { serviceRepository.getHealthStatus() } returns ServiceHealthMonitor.ServiceHealthReport(
            timestamp = System.currentTimeMillis(),
            isRecording = false,
            recordingDuration = 0L,
            recordingHealthy = false,
            storageHealthy = true,
            audioDeviceHealthy = true,
            uptime = 0L,
            hasRecentActivity = false,
            consecutiveFailures = 0,
            isHealthy = false
        )
        
        // When
        viewModel.startService()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify { serviceRepository.startService() }
        assertEquals(true, viewModel.serviceState.value)
    }
    
    @Test
    fun `stopService should call repository and update state`() = runTest {
        // Given
        coEvery { serviceRepository.isServiceEnabled() } returns true
        coEvery { serviceRepository.stopService() } returns Unit
        coEvery { serviceRepository.getFormattedStorageUsage() } returns "50 MB"
        coEvery { serviceRepository.getFormattedAvailableStorage() } returns "1 GB"
        coEvery { serviceRepository.getHealthStatus() } returns ServiceHealthMonitor.ServiceHealthReport(
            timestamp = System.currentTimeMillis(),
            isRecording = false,
            recordingDuration = 0L,
            recordingHealthy = false,
            storageHealthy = true,
            audioDeviceHealthy = true,
            uptime = 0L,
            hasRecentActivity = false,
            consecutiveFailures = 0,
            isHealthy = false
        )
        
        // When
        viewModel.stopService()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify { serviceRepository.stopService() }
        assertEquals(false, viewModel.serviceState.value)
    }
    
    @Test
    fun `refreshData should update all state`() = runTest {
        // Given
        coEvery { serviceRepository.isServiceEnabled() } returns true
        coEvery { serviceRepository.getFormattedStorageUsage() } returns "75 MB"
        coEvery { serviceRepository.getFormattedAvailableStorage() } returns "2 GB"
        coEvery { serviceRepository.getHealthStatus() } returns ServiceHealthMonitor.ServiceHealthReport(
            timestamp = System.currentTimeMillis(),
            isRecording = true,
            recordingDuration = 10000L,
            recordingHealthy = true,
            storageHealthy = true,
            audioDeviceHealthy = true,
            uptime = 20000L,
            hasRecentActivity = true,
            consecutiveFailures = 0,
            isHealthy = true
        )
        
        // When
        viewModel.refreshData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(true, viewModel.serviceState.value)
        assertEquals("75 MB", viewModel.storageUsage.value)
        assertEquals("Available: 2 GB", viewModel.availableStorage.value)
        assertNotNull(viewModel.healthStatus.value)
        assertEquals(10000L, viewModel.healthStatus.value?.recordingDuration)
    }
    
    @Test
    fun `forceHealthCheck should call repository`() = runTest {
        // Given
        coEvery { serviceRepository.forceHealthCheck() } returns Unit
        coEvery { serviceRepository.getHealthStatus() } returns ServiceHealthMonitor.ServiceHealthReport(
            timestamp = System.currentTimeMillis(),
            isRecording = true,
            recordingDuration = 5000L,
            recordingHealthy = true,
            storageHealthy = true,
            audioDeviceHealthy = true,
            uptime = 10000L,
            hasRecentActivity = true,
            consecutiveFailures = 0,
            isHealthy = true
        )
        
        // When
        viewModel.forceHealthCheck()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify { serviceRepository.forceHealthCheck() }
    }
    
    @Test
    fun `getDebugInfo should return repository debug info`() {
        // Given
        val debugInfo = "ServiceRunning: true, RotationRequested: false"
        coEvery { serviceRepository.getDebugInfo() } returns debugInfo
        
        // When
        val result = viewModel.getDebugInfo()
        
        // Then
        assertEquals(debugInfo, result)
    }
} 