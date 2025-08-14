package com.listen.app.util

import android.util.Log

object AppLog {
	fun d(tag: String, msg: String) {
		Log.d(tag, msg)
	}
	fun i(tag: String, msg: String) {
		Log.i(tag, msg)
	}
	fun w(tag: String, msg: String, t: Throwable? = null) {
		if (t != null) Log.w(tag, msg, t) else Log.w(tag, msg)
	}
	fun e(tag: String, msg: String, t: Throwable? = null) {
		if (t != null) Log.e(tag, msg, t) else Log.e(tag, msg)
	}
}