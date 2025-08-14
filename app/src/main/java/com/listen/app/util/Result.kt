package com.listen.app.util

/**
 * Result wrapper for handling success and error states
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Error -> exception
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
    }
    
    fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }
    
    fun onError(action: (Throwable) -> Unit): Result<T> {
        if (this is Error) {
            action(exception)
        }
        return this
    }
    
    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun <T> error(exception: Throwable): Result<T> = Error(exception)
    }
} 