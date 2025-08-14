package com.listen.app

import android.app.Application
import android.os.StrictMode
import timber.log.Timber

class ListenApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		if (BuildConfig.DEBUG) {
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
			Timber.plant(Timber.DebugTree())
		}
	}
}