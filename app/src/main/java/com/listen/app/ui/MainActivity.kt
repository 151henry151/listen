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
import com.listen.app.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter

/**
 * Main activity with dashboard and service controls
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var settings: SettingsManager
    private lateinit var database: ListenDatabase
    private lateinit var storageManager: StorageManager
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Microphone permission granted")
            requestNotificationPermission()
        } else {
            Log.w(TAG, "Microphone permission denied")
            Toast.makeText(this, "Microphone permission required for recording", Toast.LENGTH_LONG).show()
        }
    }
    
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
        } else {
            Log.w(TAG, "Notification permission denied")
        }
        checkAndStartService()
    }
    
    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ListenForegroundService.ACTION_RECORDING_STATUS) {
                val isRecording = intent.getBooleanExtra(ListenForegroundService.EXTRA_IS_RECORDING, false)
                val elapsed = intent.getLongExtra(ListenForegroundService.EXTRA_ELAPSED_MS, 0L)
                updateRecordingStatus(isRecording, elapsed)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
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
        registerReceiver(statusReceiver, IntentFilter(ListenForegroundService.ACTION_RECORDING_STATUS))
        updateUI()
    }
    
    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(statusReceiver)
        } catch (_: Exception) {}
    }
    
    /** Set up the user interface */
    private fun setupUI() {
        // Set up button click listeners
        binding.btnStartStop.setOnClickListener {
            if (settings.isServiceEnabled) {
                stopService()
            } else {
                startService()
            }
        }
        
        binding.btnPlayback.setOnClickListener {
            openPlayback()
        }
        
        binding.btnSettings.setOnClickListener {
            openSettings()
        }
        
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
                requestNotificationPermission()
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
    
    /** Request notification permission for Android 13+ */
    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Notification permission already granted")
                    checkAndStartService()
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            checkAndStartService()
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
                
                // Update UI elements
                binding.tvServiceStatus.text = if (isServiceEnabled) {
                    getString(R.string.status_service_running)
                } else {
                    getString(R.string.status_service_stopped)
                }
                
                binding.btnStartStop.text = if (isServiceEnabled) {
                    getString(R.string.btn_stop_recording)
                } else {
                    getString(R.string.btn_start_recording)
                }
                
                binding.tvStorageUsage.text = storageStats
                binding.tvAvailableStorage.text = "Available: $availableStorage"
                
                Log.d(TAG, "Service enabled: $isServiceEnabled")
                Log.d(TAG, "Storage usage: $storageStats")
                Log.d(TAG, "Available storage: $availableStorage")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error updating UI", e)
            }
        }
    }
    
    private fun updateRecordingStatus(isRecording: Boolean, elapsedMs: Long) {
        val minutes = elapsedMs / 60000
        val seconds = (elapsedMs % 60000) / 1000
        binding.tvRecordingStatus.text = if (isRecording) {
            getString(R.string.status_recording) + "  ${minutes}:${String.format("%02d", seconds)}"
        } else {
            getString(R.string.status_stopped)
        }
        // Keep start/stop label in sync with serviceEnabled toggle
        binding.btnStartStop.text = if (settings.isServiceEnabled) {
            getString(R.string.btn_stop_recording)
        } else {
            getString(R.string.btn_start_recording)
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
        startActivity(Intent(this, SettingsActivity::class.java))
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
} 