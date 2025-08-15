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
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.listen.app.util.AppLog
import android.Manifest
import android.app.ActivityManager
import android.app.BatteryManager

/**
 * Main activity with dashboard and service controls
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var settings: SettingsManager
    private lateinit var database: ListenDatabase
    private lateinit var storageManager: StorageManager
    
    private var lastStatusBroadcastTime: Long = 0
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            AppLog.d(TAG, "Microphone permission granted")
            requestNotificationPermission()
        } else {
            AppLog.w(TAG, "Microphone permission denied")
            Toast.makeText(this, "Microphone permission required for recording", Toast.LENGTH_LONG).show()
        }
    }
    
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            AppLog.d(TAG, "Notification permission granted")
        } else {
            AppLog.w(TAG, "Notification permission denied")
        }
        checkAndStartService()
    }
    
    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ListenForegroundService.ACTION_RECORDING_STATUS) {
                lastStatusBroadcastTime = System.currentTimeMillis()
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                statusReceiver,
                IntentFilter(ListenForegroundService.ACTION_RECORDING_STATUS),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(
                statusReceiver,
                IntentFilter(ListenForegroundService.ACTION_RECORDING_STATUS)
            )
        }
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
        
        AppLog.d(TAG, "MainActivity UI setup completed")
    }
    
    /** Check permissions and service status */
    private fun checkPermissionsAndService() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                AppLog.d(TAG, "Microphone permission already granted")
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
                    AppLog.d(TAG, "Notification permission already granted")
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
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_microphone_title))
            .setMessage(getString(R.string.permission_microphone_message))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    /** Check if service should be started and start it */
    private fun checkAndStartService() {
        if (settings.isServiceEnabled) {
            AppLog.d(TAG, "Service is enabled, starting...")
            promptBatteryOptimizationIfNeeded()
            ListenForegroundService.start(this)
        } else {
            AppLog.d(TAG, "Service is disabled")
        }
    }
    
    /** Update the UI with current status */
    private fun updateUI() {
        lifecycleScope.launch {
            try {
                // Check actual service state
                val isServiceActuallyRunning = isServiceActuallyRunning()
                val isServiceEnabled = settings.isServiceEnabled
                
                // Update storage information
                val storageStats = storageManager.getFormattedStorageUsage()
                val availableStorage = storageManager.getFormattedAvailableStorage()
                val segmentCount = database.segmentDao().getSegmentCount()
                
                // Update UI elements based on actual service state
                binding.tvServiceStatus.text = when {
                    isServiceActuallyRunning -> getString(R.string.status_service_running)
                    isServiceEnabled -> getString(R.string.status_service_starting)
                    else -> getString(R.string.status_service_stopped)
                }
                val serviceColorRes = when {
                    isServiceActuallyRunning -> R.color.status_running_green
                    isServiceEnabled -> R.color.status_starting_orange
                    else -> R.color.stopped_gray
                }
                binding.tvServiceStatus.setTextColor(ContextCompat.getColor(this@MainActivity, serviceColorRes))
                
                // Update button text based on settings (what user wants)
                binding.btnStartStop.text = if (isServiceEnabled) {
                    getString(R.string.btn_stop_recording)
                } else {
                    getString(R.string.btn_start_recording)
                }
                
                // If service should be running but isn't, try to restart it
                if (isServiceEnabled && !isServiceActuallyRunning) {
                    AppLog.w(TAG, "Service should be running but isn't - attempting restart")
                    ListenForegroundService.start(this@MainActivity)
                }
                
                binding.tvStorageUsage.text = storageStats
                binding.tvAvailableStorage.text = "Available: $availableStorage"
                binding.tvSegmentsCount.text = "Segments: $segmentCount"
                
                maybeWarnLowStorage()
                
                AppLog.d(TAG, "Service enabled: $isServiceEnabled, Actually running: $isServiceActuallyRunning")
                AppLog.d(TAG, "Storage usage: $storageStats")
                AppLog.d(TAG, "Available storage: $availableStorage")
                
            } catch (e: Exception) {
                AppLog.e(TAG, "Error updating UI", e)
            }
        }
    }
    
    private fun updateRecordingStatus(isRecording: Boolean, elapsedMs: Long) {
        val minutes = elapsedMs / 60000
        val seconds = (elapsedMs % 60000) / 1000
        binding.tvRecordingStatus.text = if (isRecording) {
            getString(R.string.label_recording) + ":  ${minutes}:${String.format("%02d", seconds)}"
        } else {
            getString(R.string.label_recording) + ": " + getString(R.string.status_stopped)
        }
        val recordingColorRes = if (isRecording) R.color.recording_red else R.color.stopped_gray
        binding.tvRecordingStatus.setTextColor(ContextCompat.getColor(this, recordingColorRes))
        
        // Update service status to show it's actually running
        val isServiceActuallyRunning = isServiceActuallyRunning()
        binding.tvServiceStatus.text = when {
            isServiceActuallyRunning -> getString(R.string.status_service_running)
            settings.isServiceEnabled -> getString(R.string.status_service_starting)
            else -> getString(R.string.status_service_stopped)
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
            promptBatteryOptimizationIfNeeded()
            ListenForegroundService.start(this)
            Toast.makeText(this, getString(R.string.msg_service_started), Toast.LENGTH_SHORT).show()
            updateUI()
            
        } else {
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    /** Stop the recording service */
    fun stopService() {
        settings.isServiceEnabled = false
        lastStatusBroadcastTime = 0  // Reset broadcast tracking
        ListenForegroundService.stop(this)
        Toast.makeText(this, getString(R.string.msg_service_stopped), Toast.LENGTH_SHORT).show()
        updateUI()
    }
    
    /** Check if the service is actually running */
    private fun isServiceActuallyRunning(): Boolean {
        return try {
            // For API 26+ getRunningServices is deprecated and may not work reliably
            // So we combine multiple approaches
            
            // Method 1: Try using ActivityManager (works on older devices)
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION")
            val runningServices = activityManager.getRunningServices(50)
            
            val isRunningViaActivityManager = runningServices.any { 
                it.service.className == ListenForegroundService::class.java.name 
            }
            
            // Method 2: Check if we're receiving broadcasts (more reliable indicator)
            // If we've received a broadcast in the last 3 seconds, service is likely running
            val lastBroadcast = lastStatusBroadcastTime
            val isReceivingBroadcasts = lastBroadcast > 0 && 
                (System.currentTimeMillis() - lastBroadcast) < 3000
            
            // Return true if either method indicates the service is running
            isRunningViaActivityManager || isReceivingBroadcasts
        } catch (e: Exception) {
            AppLog.e(TAG, "Error checking service status", e)
            false
        }
    }
    
    private fun promptBatteryOptimizationIfNeeded() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val pkg = packageName
            val ignoring = pm.isIgnoringBatteryOptimizations(pkg)
            if (!ignoring && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = android.net.Uri.parse("package:$pkg")
                startActivity(intent)
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Battery optimization prompt failed", e)
        }
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
    
    private fun maybeWarnLowStorage() {
        try {
            val estRequired = settings.calculateStorageUsage()
            val available = storageManager.getAvailableStorage()
            if (available < estRequired * 2 / 10) { // <20% of estimated requirement
                Toast.makeText(this, getString(R.string.msg_storage_full), Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {}
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
} 