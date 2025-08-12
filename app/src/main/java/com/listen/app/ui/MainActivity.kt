package com.listen.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.listen.app.R
import com.listen.app.data.ListenDatabase
import com.listen.app.service.ListenForegroundService
import com.listen.app.settings.SettingsManager
import com.listen.app.storage.StorageManager
import kotlinx.coroutines.launch

/**
 * Main activity with dashboard and service controls
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var settings: SettingsManager
    private lateinit var database: ListenDatabase
    private lateinit var storageManager: StorageManager
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Microphone permission granted")
            checkAndStartService()
        } else {
            Log.w(TAG, "Microphone permission denied")
            Toast.makeText(this, "Microphone permission required for recording", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize components
        settings = SettingsManager(this)
        database = ListenDatabase.getDatabase(this)
        storageManager = StorageManager(this)
        
        // Set up UI
        setupUI()
        
        // Check permissions and service status
        checkPermissionsAndService()
    }
    
    override fun onResume() {
        super.onResume()
        updateUI()
    }
    
    /** Set up the user interface */
    private fun setupUI() {
        // TODO: Implement UI setup with ViewBinding
        // For now, we'll just log that the activity is set up
        Log.d(TAG, "MainActivity UI setup completed")
    }
    
    /** Check permissions and service status */
    private fun checkPermissionsAndService() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Microphone permission already granted")
                checkAndStartService()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // Show permission rationale
                showPermissionRationale()
            }
            else -> {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    /** Show permission rationale dialog */
    private fun showPermissionRationale() {
        // TODO: Show custom dialog explaining why microphone permission is needed
        Toast.makeText(
            this,
            "Microphone permission is required to record audio in the background",
            Toast.LENGTH_LONG
        ).show()
        
        // Request permission after showing rationale
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    
    /** Check if service should be started and start it */
    private fun checkAndStartService() {
        if (settings.isServiceEnabled) {
            Log.d(TAG, "Service is enabled, starting...")
            ListenForegroundService.start(this)
        } else {
            Log.d(TAG, "Service is disabled")
        }
    }
    
    /** Update the UI with current status */
    private fun updateUI() {
        lifecycleScope.launch {
            try {
                // Update service status
                val isServiceEnabled = settings.isServiceEnabled
                
                // Update storage information
                val storageStats = storageManager.getFormattedStorageUsage()
                val availableStorage = storageManager.getFormattedAvailableStorage()
                
                // TODO: Update UI elements with this information
                Log.d(TAG, "Service enabled: $isServiceEnabled")
                Log.d(TAG, "Storage usage: $storageStats")
                Log.d(TAG, "Available storage: $availableStorage")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error updating UI", e)
            }
        }
    }
    
    /** Start the recording service */
    fun startService() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED) {
            
            settings.isServiceEnabled = true
            ListenForegroundService.start(this)
            Toast.makeText(this, "Recording service started", Toast.LENGTH_SHORT).show()
            updateUI()
            
        } else {
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    /** Stop the recording service */
    fun stopService() {
        settings.isServiceEnabled = false
        ListenForegroundService.stop(this)
        Toast.makeText(this, "Recording service stopped", Toast.LENGTH_SHORT).show()
        updateUI()
    }
    
    /** Open playback activity */
    fun openPlayback() {
        val intent = Intent(this, PlaybackActivity::class.java)
        startActivity(intent)
    }
    
    /** Open settings activity */
    fun openSettings() {
        // TODO: Implement settings activity
        Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show()
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
} 