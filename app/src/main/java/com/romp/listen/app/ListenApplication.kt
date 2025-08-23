package com.romp.listen.app

import android.app.Application
import android.os.StrictMode
import android.util.Log
import java.io.File

class ListenApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		
		// Write debug info to a file that can be accessed
		writeDebugLog("Application onCreate started")
		
		// Add crash logging
		Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
			val errorMsg = "Uncaught exception in thread ${thread.name}: ${throwable.message}"
			Log.e("ListenApplication", errorMsg, throwable)
			writeDebugLog(errorMsg)
			writeDebugLog(throwable.stackTraceToString())
		}
		
		try {
			writeDebugLog("Application onCreate started")
			
			// Temporarily disable StrictMode to prevent crashes
			// StrictMode.setThreadPolicy(
			// 	StrictMode.ThreadPolicy.Builder()
			// 		.detectDiskReads()
			// 		.detectDiskWrites()
			// 		.detectNetwork()
			// 		.penaltyLog()
			// 		.build()
			// )
			// StrictMode.setVmPolicy(
			// 	StrictMode.VmPolicy.Builder()
			// 		.detectActivityLeaks()
			// 		.detectLeakedClosableObjects()
			// 		.penaltyLog()
			// 		.build()
			// )
			
			writeDebugLog("Application initialized successfully")
			Log.d("ListenApplication", "Application initialized successfully")
		} catch (e: Exception) {
			val errorMsg = "Error during application initialization: ${e.message}"
			Log.e("ListenApplication", errorMsg, e)
			writeDebugLog(errorMsg)
			writeDebugLog(e.stackTraceToString())
		}
	}
	
	private fun writeDebugLog(message: String) {
		try {
			val file = File(filesDir, "debug_log.txt")
			val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
			val logEntry = "[$timestamp] $message\n"
			file.appendText(logEntry)
		} catch (e: Exception) {
			Log.e("ListenApplication", "Failed to write debug log", e)
		}
	}
}