package com.listen.app

import android.app.Application
import android.os.StrictMode
import android.util.Log

class ListenApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		// Enable StrictMode for debugging
		StrictMode.setThreadPolicy(
			StrictMode.ThreadPolicy.Builder()
				.detectDiskReads()
				.detectDiskWrites()
				.detectNetwork()
				.penaltyLog()
				.build()
		)
		StrictMode.setVmPolicy(
			StrictMode.VmPolicy.Builder()
				.detectActivityLeaks()
				.detectLeakedClosableObjects()
				.penaltyLog()
				.build()
		)
		Log.d("ListenApplication", "Application initialized")
	}
}