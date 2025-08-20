package com.listen.app.ui

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.listen.app.R
import com.listen.app.settings.AudioQualityPreset
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
		val rgAudioQuality: RadioGroup = findViewById(R.id.rg_audio_quality)
		val rbQualityLow: RadioButton = findViewById(R.id.rb_quality_low)
		val rbQualityMedium: RadioButton = findViewById(R.id.rb_quality_medium)
		val rbQualityHigh: RadioButton = findViewById(R.id.rb_quality_high)
		val etMaxStorage: EditText = findViewById(R.id.et_max_storage)
		val swAutoStart: Switch = findViewById(R.id.sw_auto_start)
		val swPowerSaving: Switch = findViewById(R.id.sw_power_saving)
		val swAdaptive: Switch = findViewById(R.id.sw_adaptive)
		val swAutoMusic: Switch = findViewById(R.id.sw_auto_music)
		val btnSave: Button = findViewById(R.id.btn_save_settings)
		
		// Pre-fill current values
		etSegmentDuration.setText(settings.segmentDurationSeconds.toString())
		etRetentionMinutes.setText(settings.retentionPeriodMinutes.toString())
		etMaxStorage.setText(settings.maxStorageMB.toString())
		swAutoStart.isChecked = settings.autoStartOnBoot
		swPowerSaving.isChecked = settings.powerSavingModeEnabled
		swAdaptive.isChecked = settings.adaptivePerformanceEnabled
		swAutoMusic.isChecked = settings.autoMusicModeEnabled
		
		// Set current audio quality preset
		when (settings.audioQualityPreset) {
			AudioQualityPreset.LOW -> rbQualityLow.isChecked = true
			AudioQualityPreset.MEDIUM -> rbQualityMedium.isChecked = true
			AudioQualityPreset.HIGH -> rbQualityHigh.isChecked = true
		}
		
		// Disable manual input when auto mode is enabled (visual hint)
		fun updateSegmentDurationEnabled() {
			etSegmentDuration.isEnabled = !swAutoMusic.isChecked
		}
		swAutoMusic.setOnCheckedChangeListener { _, _ -> updateSegmentDurationEnabled() }
		updateSegmentDurationEnabled()
		
		btnSave.setOnClickListener {
			settings.segmentDurationSeconds = etSegmentDuration.text.toString().toIntOrNull() ?: settings.segmentDurationSeconds
			settings.retentionPeriodMinutes = etRetentionMinutes.text.toString().toIntOrNull() ?: settings.retentionPeriodMinutes
			settings.maxStorageMB = etMaxStorage.text.toString().toIntOrNull() ?: settings.maxStorageMB
			swAutoStart.isChecked.also { settings.autoStartOnBoot = it }
			swPowerSaving.isChecked.also { settings.powerSavingModeEnabled = it }
			swAdaptive.isChecked.also { settings.adaptivePerformanceEnabled = it }
			swAutoMusic.isChecked.also { settings.autoMusicModeEnabled = it }
			
			// Save audio quality preset
			val selectedQuality = when (rgAudioQuality.checkedRadioButtonId) {
				R.id.rb_quality_low -> AudioQualityPreset.LOW
				R.id.rb_quality_medium -> AudioQualityPreset.MEDIUM
				R.id.rb_quality_high -> AudioQualityPreset.HIGH
				else -> AudioQualityPreset.MEDIUM // Default fallback
			}
			settings.audioQualityPreset = selectedQuality
			
			// Notify running service to apply new settings
			ListenForegroundService.applyUpdatedSettings(this)
			finish()
		}
	}
}