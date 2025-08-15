package com.listen.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.listen.app.service.ListenForegroundService
import com.listen.app.settings.SettingsManager
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import com.listen.app.util.AppLog

/**
 * Broadcast receiver for auto-starting the service after device boot
 */
class ListenBootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        AppLog.d(TAG, "Boot receiver triggered: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                
                val settings = SettingsManager(context)
                
                // Check if we should resume recording after boot
                // This happens if:
                // 1. Recording was active when the device shut down
                // 2. Auto-start on boot is enabled
                val shouldResumeRecording = settings.wasRecordingOnShutdown && settings.autoStartOnBoot
                
                if (shouldResumeRecording) {
                    // Verify microphone permission is granted before starting
                    val micGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!micGranted) {
                        AppLog.w(TAG, "Skipping auto-start: RECORD_AUDIO not granted")
                        return
                    }
                    
                    AppLog.d(TAG, "Resuming recording after boot (was recording on shutdown)")
                    
                    // Delay restart to allow system to stabilize
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            // Re-enable the service flag since we're resuming
                            settings.isServiceEnabled = true
                            ListenForegroundService.start(context)
                            // Clear the flag now that we've resumed
                            settings.wasRecordingOnShutdown = false
                            AppLog.d(TAG, "Recording resumed successfully after boot")
                        } catch (e: Exception) {
                            AppLog.e(TAG, "Failed to resume recording after boot", e)
                            // Clear the flag even on failure to avoid repeated attempts
                            settings.wasRecordingOnShutdown = false
                        }
                    }, BOOT_DELAY_MS)
                    
                } else {
                    AppLog.d(TAG, "Recording not resumed: wasRecording=${settings.wasRecordingOnShutdown}, autoStart=${settings.autoStartOnBoot}")
                }
            }
        }
    }
    
    companion object {
        private const val TAG = "ListenBootReceiver"
        private const val BOOT_DELAY_MS = 30000L // 30 seconds delay
    }
} 