package com.listen.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.listen.app.R
import com.listen.app.settings.SettingsManager
import com.listen.app.service.ListenForegroundService

class SettingsActivity : AppCompatActivity() {
	private lateinit var settings: SettingsManager
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)
		settings = SettingsManager(this)
		
		val etSegmentDuration: EditText = findViewById(R.id.et_segment_duration)
		val etRetentionMinutes: EditText = findViewById(R.id.et_retention_minutes)
		val etBitrate: EditText = findViewById(R.id.et_bitrate_kbps)
		val etSampleRate: EditText = findViewById(R.id.et_sample_rate)
		val etMaxStorage: EditText = findViewById(R.id.et_max_storage)
		val swAutoStart: Switch = findViewById(R.id.sw_auto_start)
		val btnSave: Button = findViewById(R.id.btn_save_settings)
		
		// Pre-fill current values
		etSegmentDuration.setText(settings.segmentDurationSeconds.toString())
		etRetentionMinutes.setText(settings.retentionPeriodMinutes.toString())
		etBitrate.setText(settings.audioBitrate.toString())
		etSampleRate.setText(settings.audioSampleRate.toString())
		etMaxStorage.setText(settings.maxStorageMB.toString())
		swAutoStart.isChecked = settings.autoStartOnBoot
		
		btnSave.setOnClickListener {
			settings.segmentDurationSeconds = etSegmentDuration.text.toString().toIntOrNull() ?: settings.segmentDurationSeconds
			settings.retentionPeriodMinutes = etRetentionMinutes.text.toString().toIntOrNull() ?: settings.retentionPeriodMinutes
			settings.audioBitrate = etBitrate.text.toString().toIntOrNull() ?: settings.audioBitrate
			settings.audioSampleRate = etSampleRate.text.toString().toIntOrNull() ?: settings.audioSampleRate
			settings.maxStorageMB = etMaxStorage.text.toString().toIntOrNull() ?: settings.maxStorageMB
			settings.autoStartOnBoot = swAutoStart.isChecked
			
			// Notify running service to apply new settings
			ListenForegroundService.applyUpdatedSettings(this)
			finish()
		}
	}
}