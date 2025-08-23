package com.romp.listen.app.util

import org.junit.Assert.*
import org.junit.Test

class ResultTest {
    
    @Test
    fun `success should create Success result`() {
        // When
        val result = Result.success("test")
        
        // Then
        assertTrue(result.isSuccess())
        assertFalse(result.isError())
        assertEquals("test", result.getOrNull())
        assertNull(result.exceptionOrNull())
    }
    
    @Test
    fun `error should create Error result`() {
        // Given
        val exception = RuntimeException("test error")
        
        // When
        val result: Result<String> = Result.error(exception)
        
        // Then
        assertFalse(result.isSuccess())
        assertTrue(result.isError())
        assertNull(result.getOrNull())
        assertEquals(exception, result.exceptionOrNull())
    }
    
    @Test
    fun `getOrThrow should return data for Success`() {
        // Given
        val result = Result.success("test")
        
        // When
        val data = result.getOrThrow()
        
        // Then
        assertEquals("test", data)
    }
    
    @Test(expected = RuntimeException::class)
    fun `getOrThrow should throw exception for Error`() {
        // Given
        val exception = RuntimeException("test error")
        val result: Result<String> = Result.error(exception)
        
        // When
        result.getOrThrow()
        
        // Then
        // Should throw exception
    }
    
    @Test
    fun `onSuccess should execute action for Success`() {
        // Given
        val result: Result<String> = Result.success("test")
        var executed = false
        
        // When
        result.onSuccess { data ->
            assertEquals("test", data)
            executed = true
        }
        
        // Then
        assertTrue(executed)
    }
    
    @Test
    fun `onSuccess should not execute action for Error`() {
        // Given
        val exception = RuntimeException("test error")
        val result: Result<String> = Result.error(exception)
        var executed = false
        
        // When
        result.onSuccess { data ->
            executed = true
        }
        
        // Then
        assertFalse(executed)
    }
    
    @Test
    fun `onError should execute action for Error`() {
        // Given
        val exception = RuntimeException("test error")
        val result: Result<String> = Result.error(exception)
        var executed = false
        
        // When
        result.onError { ex ->
            assertEquals(exception, ex)
            executed = true
        }
        
        // Then
        assertTrue(executed)
    }
    
    @Test
    fun `onError should not execute action for Success`() {
        // Given
        val result: Result<String> = Result.success("test")
        var executed = false
        
        // When
        result.onError { ex ->
            executed = true
        }
        
        // Then
        assertFalse(executed)
    }
    
    @Test
    fun `chaining onSuccess and onError should work correctly`() {
        // Given
        val successResult: Result<String> = Result.success("test")
        val errorResult: Result<String> = Result.error(RuntimeException("error"))
        
        // When & Then
        val successChain = successResult
            .onSuccess { data -> assertEquals("test", data) }
            .onError { ex -> fail("Should not execute onError for success") }
        
        val errorChain = errorResult
            .onSuccess { data -> fail("Should not execute onSuccess for error") }
            .onError { ex -> assertTrue(ex is RuntimeException) }
        
        assertTrue(successChain.isSuccess())
        assertTrue(errorChain.isError())
    }
} 