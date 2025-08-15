package com.listen.app.perf

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Debug
import android.os.Process
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.listen.app.util.AppLog
import java.util.concurrent.atomic.AtomicLong

/**
 * Periodically samples simple performance metrics and logs them.
 */
class PerformanceMonitor(private val context: Context) {
    private var samplingJob: Job? = null

    // Rotation timing aggregation
    private val totalRotations = AtomicLong(0)
    private val totalRotationDurationMs = AtomicLong(0)

    // Last CPU time snapshot
    private var lastCpuTimeMs: Long = 0

    fun start(scope: CoroutineScope, samplingIntervalMs: Long = 60_000L) {
        if (samplingJob != null) return
        // Initialize CPU baseline
        lastCpuTimeMs = Process.getElapsedCpuTime()
        samplingJob = scope.launch {
            while (isActive) {
                try {
                    sampleAndLog()
                } catch (t: Throwable) {
                    AppLog.w(TAG, "Performance sampling error", t)
                }
                delay(samplingIntervalMs)
            }
        }
        AppLog.d(TAG, "Performance monitor started")
    }

    fun stop() {
        samplingJob?.cancel()
        samplingJob = null
        AppLog.d(TAG, "Performance monitor stopped")
    }

    fun recordSegmentRotation(durationMs: Long) {
        totalRotations.incrementAndGet()
        totalRotationDurationMs.addAndGet(durationMs)
    }

    private fun sampleAndLog() {
        val batteryInfo = readBatteryInfo()
        val memInfo = readMemoryInfo()
        val cpuDeltaMs = readCpuDeltaMs()

        val rotations = totalRotations.get()
        val avgRotationMs = if (rotations > 0) totalRotationDurationMs.get() / rotations else 0

        val logMsg = "battery=${batteryInfo.levelPercent}% charging=${batteryInfo.isCharging} " +
            "pss=${memInfo.pssKb}KB privateDirty=${memInfo.privateDirtyKb}KB " +
            "heapUsed=${memInfo.heapUsedKb}KB cpuDeltaMs=${cpuDeltaMs} " +
            "rotations=$rotations avgRotationMs=$avgRotationMs"
        AppLog.i(TAG, logMsg)
    }

    private fun readBatteryInfo(): BatterySnapshot {
        return try {
            val intent: Intent? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED), Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            }
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
            val pct = if (level >= 0 && scale > 0) (level * 100) / scale else -1
            BatterySnapshot(pct, isCharging)
        } catch (_: Exception) {
            BatterySnapshot(-1, false)
        }
    }

    private fun readMemoryInfo(): MemorySnapshot {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfos = activityManager.getProcessMemoryInfo(intArrayOf(Process.myPid()))
            val mi = memInfos.firstOrNull()
            if (mi != null) {
                val runtime = Runtime.getRuntime()
                val heapUsedKb = ((runtime.totalMemory() - runtime.freeMemory()) / 1024L).toInt()
                MemorySnapshot(mi.totalPss, mi.getTotalPrivateDirty(), heapUsedKb)
            } else {
                MemorySnapshot(0, 0, 0)
            }
        } catch (_: Exception) {
            MemorySnapshot(0, 0, 0)
        }
    }

    private fun readCpuDeltaMs(): Long {
        return try {
            val now = Process.getElapsedCpuTime()
            val delta = if (lastCpuTimeMs == 0L) 0L else (now - lastCpuTimeMs)
            lastCpuTimeMs = now
            delta
        } catch (_: Exception) {
            0L
        }
    }

    data class BatterySnapshot(val levelPercent: Int, val isCharging: Boolean)
    data class MemorySnapshot(val pssKb: Int, val privateDirtyKb: Int, val heapUsedKb: Int)

    companion object {
        private const val TAG = "PerformanceMonitor"
    }
}