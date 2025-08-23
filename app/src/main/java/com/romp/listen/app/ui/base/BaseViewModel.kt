package com.romp.listen.app.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel class providing common functionality
 */
abstract class BaseViewModel : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }
    
    protected fun showLoading() {
        _isLoading.value = true
    }
    
    protected fun hideLoading() {
        _isLoading.value = false
    }
    
    protected fun showError(message: String) {
        _errorMessage.value = message
    }
    
    protected fun clearError() {
        _errorMessage.value = null
    }
    
    protected fun handleError(throwable: Throwable) {
        hideLoading()
        showError(throwable.message ?: "An unknown error occurred")
    }
    
    protected fun launchWithErrorHandling(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            showLoading()
            try {
                block()
            } finally {
                hideLoading()
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        clearError()
    }
} 