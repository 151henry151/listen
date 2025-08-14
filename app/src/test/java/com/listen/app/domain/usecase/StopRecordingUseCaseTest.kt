package com.listen.app.domain.usecase

import com.listen.app.data.repository.ServiceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StopRecordingUseCaseTest {
    
    private lateinit var useCase: StopRecordingUseCase
    private lateinit var serviceRepository: ServiceRepository
    
    @Before
    fun setUp() {
        serviceRepository = mockk()
        useCase = StopRecordingUseCase(serviceRepository)
    }
    
    @Test
    fun `invoke should return success when service is running`() = runTest {
        // Given
        coEvery { serviceRepository.isServiceEnabled() } returns true
        coEvery { serviceRepository.stopService() } returns Unit
        
        // When
        val result = useCase.invoke()
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `invoke should return failure when service is not running`() = runTest {
        // Given
        coEvery { serviceRepository.isServiceEnabled() } returns false
        
        // When
        val result = useCase.invoke()
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Service is not running", result.exceptionOrNull()?.message)
    }
} 