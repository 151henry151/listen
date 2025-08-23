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
import android.app.ActivityManager
import java.io.File

/**
 * Main activity with dashboard and service controls
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var settings: SettingsManager
    private lateinit var database: ListenDatabase
    private lateinit var storageManager: StorageManager
    
    private var lastStatusBroadcastTime: Long = 0
    private var lastRecordingState: Boolean = false
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            AppLog.d(TAG, "Microphone permission granted")
            requestForegroundServicePermission()
        } else {
            AppLog.w(TAG, "Microphone permission denied")
            Toast.makeText(this, "Microphone permission required for recording", Toast.LENGTH_LONG).show()
        }
    }
    
    private val requestForegroundServicePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            AppLog.d(TAG, "Foreground service microphone permission granted")
            requestNotificationPermission()
        } else {
            AppLog.w(TAG, "Foreground service microphone permission denied")
            Toast.makeText(this, "Foreground service permission required for recording", Toast.LENGTH_LONG).show()
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
        // Skip phone call permissions for now
        writeDebugLog("Skipping phone call permissions")
    }

    private val requestPhoneStatePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            AppLog.w(TAG, "READ_PHONE_STATE denied; call metadata unavailable")
        }
        // Next, request READ_CALL_LOG (optional)
        requestCallLogPermissionIfNeeded()
    }

    private val requestCallLogPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Regardless of grant/deny, proceed to start service
        checkAndStartService()
    }
    
    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ListenForegroundService.ACTION_RECORDING_STATUS) {
                lastStatusBroadcastTime = System.currentTimeMillis()
                // Extract the actual recording state from the broadcast
                lastRecordingState = intent.getBooleanExtra(ListenForegroundService.EXTRA_IS_RECORDING, false)
                // For simplified status, we just update the UI to reflect the current state
                updateUI()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            writeDebugLog("MainActivity onCreate started")
            Log.d(TAG, "MainActivity onCreate started")
            
            // Step 1: View binding
            try {
                writeDebugLog("Starting view binding...")
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
                writeDebugLog("View binding and content view set successfully")
            Log.d(TAG, "View binding and content view set")
            } catch (e: Exception) {
                writeDebugLog("View binding failed: ${e.message}")
                Log.e(TAG, "View binding failed", e)
                throw e
            }
            
            // Step 2: Initialize SettingsManager
            try {
                writeDebugLog("Initializing SettingsManager...")
            settings = SettingsManager(this)
                writeDebugLog("SettingsManager initialized successfully")
            Log.d(TAG, "SettingsManager initialized")
            } catch (e: Exception) {
                writeDebugLog("SettingsManager initialization failed: ${e.message}")
                Log.e(TAG, "SettingsManager initialization failed", e)
                throw e
            }
            
            // Step 3: Initialize database with fallback
            try {
                writeDebugLog("Initializing database...")
                database = ListenDatabase.getDatabase(this)
                writeDebugLog("Database initialized successfully")
                Log.d(TAG, "Database initialized")
            } catch (e: Exception) {
                writeDebugLog("Database initialization failed: ${e.message}")
                Log.e(TAG, "Database initialization failed", e)
                // Continue without database for now
                Toast.makeText(this, "Database unavailable - some features may not work", Toast.LENGTH_SHORT).show()
            }
            
            // Step 4: Initialize StorageManager
            try {
                writeDebugLog("Initializing StorageManager...")
            storageManager = StorageManager(this)
                writeDebugLog("StorageManager initialized successfully")
            Log.d(TAG, "StorageManager initialized")
            } catch (e: Exception) {
                writeDebugLog("StorageManager initialization failed: ${e.message}")
                Log.e(TAG, "StorageManager initialization failed", e)
                throw e
            }
            
            // Step 5: Set up UI
            try {
                writeDebugLog("Setting up UI...")
            setupUI()
                writeDebugLog("UI setup completed successfully")
            Log.d(TAG, "UI setup completed")
            } catch (e: Exception) {
                writeDebugLog("UI setup failed: ${e.message}")
                Log.e(TAG, "UI setup failed", e)
                throw e
            }
            
            // Step 6: Check basic permissions
            try {
                writeDebugLog("Checking basic permissions...")
            checkBasicPermissions()
                writeDebugLog("Basic permissions check completed successfully")
            Log.d(TAG, "Basic permissions check completed")
            } catch (e: Exception) {
                writeDebugLog("Basic permissions check failed: ${e.message}")
                Log.e(TAG, "Basic permissions check failed", e)
                throw e
            }
            
            writeDebugLog("MainActivity onCreate completed successfully")
            Log.d(TAG, "MainActivity onCreate completed successfully")
        } catch (e: Exception) {
            val errorMsg = "Error in MainActivity onCreate: ${e.message}"
            Log.e(TAG, errorMsg, e)
            writeDebugLog(errorMsg)
            writeDebugLog(e.stackTraceToString())
            throw e // Re-throw to see the crash
        }
    }
    
    private fun writeDebugLog(message: String) {
        try {
            val file = File(filesDir, "debug_log.txt")
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            val logEntry = "[$timestamp] MainActivity: $message\n"
            file.appendText(logEntry)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write debug log", e)
        }
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
    
    /** Check basic permissions only */
    private fun checkBasicPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                writeDebugLog("Microphone permission already granted")
                AppLog.d(TAG, "Microphone permission already granted")
                // Check foreground service permission for Android 14+
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.FOREGROUND_SERVICE_MICROPHONE
                        ) != PackageManager.PERMISSION_GRANTED) {
                        writeDebugLog("Requesting foreground service microphone permission")
                        requestForegroundServicePermissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
                        return
                    }
                }
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                writeDebugLog("Showing permission rationale")
                showPermissionRationale()
            }
            else -> {
                writeDebugLog("Requesting microphone permission")
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
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
    
    /** Request foreground service microphone permission for Android 14+ */
    private fun requestForegroundServicePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.FOREGROUND_SERVICE_MICROPHONE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    AppLog.d(TAG, "Foreground service microphone permission already granted")
                    requestNotificationPermission()
                }
                else -> {
                    requestForegroundServicePermissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
                }
            }
        } else {
            // For Android 13 and below, this permission is not required
            requestNotificationPermission()
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
                    requestPhoneStatePermissionOrStart()
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            requestPhoneStatePermissionOrStart()
        }
    }



    private fun requestPhoneStatePermissionOrStart() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED -> {
                requestCallLogPermissionIfNeeded()
            }
            else -> {
                requestPhoneStatePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            }
        }
    }

    private fun requestCallLogPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            checkAndStartService()
        } else {
            requestCallLogPermissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
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
            ListenForegroundService.start(this)
        } else {
            AppLog.d(TAG, "Service is disabled")
        }
    }
    
    /** Update the UI with current status */
    private fun updateUI() {
        lifecycleScope.launch {
            try {
                writeDebugLog("updateUI started")
                
                // Check actual service state
                val isServiceActuallyRunning = isServiceActuallyRunning()
                val isServiceEnabled = settings.isServiceEnabled
                writeDebugLog("Service state checked: enabled=$isServiceEnabled, running=$isServiceActuallyRunning")
                
                // Update storage information with error handling
                val storageStats = try {
                    storageManager.getFormattedStorageUsage()
                } catch (e: Exception) {
                    writeDebugLog("Storage stats failed: ${e.message}")
                    "0 MB"
                }
                
                val availableStorage = try {
                    storageManager.getFormattedAvailableStorage()
                } catch (e: Exception) {
                    writeDebugLog("Available storage failed: ${e.message}")
                    "0 MB"
                }
                
                val segmentCount = try {
                    if (::database.isInitialized) {
                        database.segmentDao().getSegmentCount()
                    } else {
                        writeDebugLog("Database not initialized, using 0 for segment count")
                        0
                    }
                } catch (e: Exception) {
                    writeDebugLog("Segment count failed: ${e.message}")
                    0
                }
                
                writeDebugLog("Storage info retrieved: stats=$storageStats, available=$availableStorage, segments=$segmentCount")
                
                // Simplified status display - just show Recording or Stopped
                if (lastRecordingState) {
                    binding.tvServiceStatus.text = getString(R.string.status_recording)
                    binding.tvServiceStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.status_running_green))
                    // Hide the recording status text view since we're using service status for everything
                    binding.tvRecordingStatus.visibility = android.view.View.GONE
                } else {
                    binding.tvServiceStatus.text = getString(R.string.status_stopped)
                    binding.tvServiceStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.recording_red))
                    // Hide the recording status text view since we're using service status for everything
                    binding.tvRecordingStatus.visibility = android.view.View.GONE
                }
                
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
                AppLog.d(TAG, "Recording state: $lastRecordingState")
                AppLog.d(TAG, "Storage usage: $storageStats")
                AppLog.d(TAG, "Available storage: $availableStorage")
                
                writeDebugLog("updateUI completed successfully")
                
            } catch (e: Exception) {
                val errorMsg = "Error updating UI: ${e.message}"
                AppLog.e(TAG, errorMsg, e)
                writeDebugLog(errorMsg)
                writeDebugLog(e.stackTraceToString())
            }
        }
    }
    
    /** Start the recording service */
    fun startService() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED) {
            
            // Check foreground service permission for Android 14+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.FOREGROUND_SERVICE_MICROPHONE
                    ) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Foreground service permission required", Toast.LENGTH_SHORT).show()
                    requestForegroundServicePermissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
                    return
                }
            }
            
            // Check if user has given consent for recording
            if (!settings.hasUserConsentedToRecording) {
                showRecordingConsentDialog()
                return
            }
            
            settings.isServiceEnabled = true
            ListenForegroundService.start(this)
            Toast.makeText(this, getString(R.string.msg_service_started), Toast.LENGTH_SHORT).show()
            
            // Set optimistic recording state since we just started the service
            lastRecordingState = true
            updateUI()
            
        } else {
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    /** Show recording consent dialog */
    private fun showRecordingConsentDialog() {
        val consentDialog = ConsentDialog.newInstance()
        consentDialog.setOnConsentGranted {
            settings.hasUserConsentedToRecording = true
            startService() // Retry starting service after consent
        }
        consentDialog.setOnConsentDenied {
            Toast.makeText(this, "Recording consent required to use this app", Toast.LENGTH_LONG).show()
        }
        consentDialog.show(supportFragmentManager, ConsentDialog.TAG)
    }
    
    /** Stop the recording service */
    fun stopService() {
        settings.isServiceEnabled = false
        lastStatusBroadcastTime = 0  // Reset broadcast tracking
        lastRecordingState = false   // Reset recording state
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
            
            // Method 2: Check if we're receiving broadcasts AND service is enabled
            // Only consider broadcasts as valid if the service is supposed to be enabled
            val lastBroadcast = lastStatusBroadcastTime
            val isReceivingBroadcasts = lastBroadcast > 0 && 
                (System.currentTimeMillis() - lastBroadcast) < 3000 &&
                settings.isServiceEnabled
            
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