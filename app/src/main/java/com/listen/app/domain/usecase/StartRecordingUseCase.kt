package com.listen.app.domain.usecase

import com.listen.app.data.repository.ServiceRepository
import com.listen.app.settings.SettingsManager
import com.listen.app.storage.StorageManager

/**
 * Use case for starting recording service
 */
class StartRecordingUseCase(
    private val serviceRepository: ServiceRepository,
    private val settings: SettingsManager,
    private val storageManager: StorageManager
) {
    
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Check if service is already running
            if (serviceRepository.isServiceEnabled()) {
                return Result.failure(IllegalStateException("Service is already running"))
            }
            
            // Check storage health
            val storageHealth = serviceRepository.getStorageHealthReport()
            if (!storageHealth.isHealthy) {
                return Result.failure(IllegalStateException("Storage is not healthy"))
            }
            
            // Check if we have sufficient storage
            val requiredStorage = settings.calculateStorageUsageWithMargin()
            if (!serviceRepository.isStorageHealthy(requiredStorage)) {
                return Result.failure(IllegalStateException("Insufficient storage space"))
            }
            
            // Start the service
            serviceRepository.startService()
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 