package com.listen.app.util

import com.listen.app.BuildConfig
import timber.log.Timber

object AppLog {
	fun d(tag: String, msg: String) {
		if (BuildConfig.DEBUG) Timber.tag(tag).d(msg)
	}
	fun i(tag: String, msg: String) {
		if (BuildConfig.DEBUG) Timber.tag(tag).i(msg)
	}
	fun w(tag: String, msg: String, t: Throwable? = null) {
		if (BuildConfig.DEBUG) {
			if (t != null) Timber.tag(tag).w(t, msg) else Timber.tag(tag).w(msg)
		}
	}
	fun e(tag: String, msg: String, t: Throwable? = null) {
		if (BuildConfig.DEBUG) {
			if (t != null) Timber.tag(tag).e(t, msg) else Timber.tag(tag).e(msg)
		}
	}
}