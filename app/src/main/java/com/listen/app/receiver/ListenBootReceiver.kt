package com.listen.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.listen.app.service.ListenForegroundService
import com.listen.app.settings.SettingsManager

/**
 * Broadcast receiver for auto-starting the service after device boot
 */
class ListenBootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Boot receiver triggered: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_QUICKBOOT_POWERON,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                
                val settings = SettingsManager(context)
                
                // Check if service should auto-start
                if (settings.isServiceEnabled && settings.autoStartOnBoot) {
                    Log.d(TAG, "Auto-starting Listen service after boot")
                    
                    // Delay restart to allow system to stabilize
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            ListenForegroundService.start(context)
                            Log.d(TAG, "Service auto-started successfully")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to auto-start service", e)
                        }
                    }, BOOT_DELAY_MS)
                    
                } else {
                    Log.d(TAG, "Service auto-start disabled or service not enabled")
                }
            }
        }
    }
    
    companion object {
        private const val TAG = "ListenBootReceiver"
        private const val BOOT_DELAY_MS = 30000L // 30 seconds delay
    }
} 