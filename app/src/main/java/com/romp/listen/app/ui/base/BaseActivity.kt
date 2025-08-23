package com.romp.listen.app.ui.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Base Activity class providing common functionality
 */
abstract class BaseActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupObservers()
    }
    
    /** Set up observers for common UI state */
    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe loading state
                getViewModel()?.isLoading?.collect { isLoading ->
                    onLoadingChanged(isLoading)
                }
            }
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe error messages
                getViewModel()?.errorMessage?.collect { errorMessage ->
                    errorMessage?.let { onError(it) }
                }
            }
        }
    }
    
    /** Get the ViewModel for this activity */
    abstract fun getViewModel(): BaseViewModel?
    
    /** Handle loading state changes */
    protected open fun onLoadingChanged(isLoading: Boolean) {
        // Override in subclasses to show/hide loading indicators
    }
    
    /** Handle error messages */
    protected open fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    /** Show success message */
    protected fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    /** Show info message */
    protected fun showInfo(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    /** Collect StateFlow safely */
    protected fun <T> collectStateFlow(
        stateFlow: StateFlow<T>,
        onCollect: (T) -> Unit
    ) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                stateFlow.collect { value ->
                    onCollect(value)
                }
            }
        }
    }
} 