package com.romp.listen.app.domain.usecase

import com.romp.listen.app.data.repository.ServiceRepository

/**
 * Use case for stopping recording service
 */
class StopRecordingUseCase(
    private val serviceRepository: ServiceRepository
) {
    
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Check if service is running
            if (!serviceRepository.isServiceEnabled()) {
                return Result.failure(IllegalStateException("Service is not running"))
            }
            
            // Stop the service
            serviceRepository.stopService()
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 