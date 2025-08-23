package com.listen.app.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.listen.app.R
import com.listen.app.data.ListenDatabase
import com.listen.app.data.Segment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import com.listen.app.util.AppLog
import com.listen.app.util.FileUtils
import com.listen.app.util.SegmentManager
import android.app.AlertDialog
import android.widget.EditText
import android.widget.Toast

/**
 * Activity for playing back recorded audio segments
 */
class PlaybackActivity : AppCompatActivity() {
    
    private lateinit var database: ListenDatabase
    private var mediaPlayer: MediaPlayer? = null
    private var currentSegment: Segment? = null
    
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    
    private lateinit var rvSegments: RecyclerView
    private lateinit var tvCurrentSegment: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var btnPlayPause: Button
    private lateinit var btnStop: Button
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var btnSave: Button
    private lateinit var btnClearAll: Button
    private lateinit var adapter: SegmentAdapter
    private var segments: List<Segment> = emptyList()
    private var currentSegmentIndex: Int = -1
    
    private var progressJob: Job? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playback)
        
        // Initialize database
        database = ListenDatabase.getDatabase(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // Set up UI
        setupUI()
        
        // Load segments
        loadSegments()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
        abandonAudioFocus()
        progressJob?.cancel()
    }
    
    /** Set up the user interface */
    private fun setupUI() {
        rvSegments = findViewById(R.id.rv_segments)
        tvCurrentSegment = findViewById(R.id.tv_current_segment)
        seekBar = findViewById(R.id.seek_bar)
        tvCurrentTime = findViewById(R.id.tv_current_time)
        tvTotalTime = findViewById(R.id.tv_total_time)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnStop = findViewById(R.id.btn_stop)
        btnPrevious = findViewById(R.id.btn_previous)
        btnNext = findViewById(R.id.btn_next)
        btnSave = findViewById(R.id.btn_save)
        btnClearAll = findViewById(R.id.btn_clear_all)
        
        adapter = SegmentAdapter(
            emptyList(),
            onClick = { segment ->
                playSegment(segment)
                updateCurrentSegmentInfo(segment)
            },
            onSaveClick = { segment ->
                showSaveDialog(segment)
            },
            onDeleteClick = { segment ->
                showDeleteSegmentDialog(segment)
            }
        )
        rvSegments.layoutManager = LinearLayoutManager(this)
        rvSegments.adapter = adapter
        
        btnPlayPause.setOnClickListener {
            if (isPlaying()) {
                pausePlayback()
                updatePlayPauseButton()
            } else {
                resumePlayback()
                updatePlayPauseButton()
            }
        }
        
        btnStop.setOnClickListener {
            stopPlayback()
            updatePlayPauseButton()
        }
        
        btnPrevious.setOnClickListener {
            playPreviousSegment()
        }
        
        btnNext.setOnClickListener {
            playNextSegment()
        }
        
        btnSave.setOnClickListener {
            currentSegment?.let { segment ->
                showSaveDialog(segment)
            }
        }
        
        btnClearAll.setOnClickListener {
            showDeleteAllDialog()
        }
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    seekTo(progress)
                    tvCurrentTime.text = formatMs(progress.toLong())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        AppLog.d(TAG, "PlaybackActivity UI setup completed")
    }
    
    /** Update the play/pause button text */
    private fun updatePlayPauseButton() {
        btnPlayPause.text = if (isPlaying()) {
            getString(R.string.btn_pause)
        } else {
            getString(R.string.btn_play)
        }
    }
    
    /** Update current segment information display */
    private fun updateCurrentSegmentInfo(segment: Segment) {
        tvCurrentSegment.text = File(segment.filePath).name
        currentSegmentIndex = segments.indexOf(segment)
        updateNavigationButtons()
    }
    
    /** Update navigation buttons state */
    private fun updateNavigationButtons() {
        btnPrevious.isEnabled = currentSegmentIndex > 0
        btnNext.isEnabled = currentSegmentIndex < segments.size - 1
        btnSave.isEnabled = currentSegment != null
    }
    
    /** Play the previous segment */
    private fun playPreviousSegment() {
        if (currentSegmentIndex > 0) {
            val previousSegment = segments[currentSegmentIndex - 1]
            playSegment(previousSegment)
            updateCurrentSegmentInfo(previousSegment)
        }
    }
    
    /** Play the next segment */
    private fun playNextSegment() {
        if (currentSegmentIndex < segments.size - 1) {
            val nextSegment = segments[currentSegmentIndex + 1]
            playSegment(nextSegment)
            updateCurrentSegmentInfo(nextSegment)
        }
    }
    
    /** Show save dialog for a segment */
    private fun showSaveDialog(segment: Segment) {
        val defaultFilename = FileUtils.generateDefaultFilename(segment)
        
        val editText = EditText(this).apply {
            setText(defaultFilename)
            selectAll()
        }
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_save_title))
            .setMessage(getString(R.string.dialog_save_message))
            .setView(editText)
            .setPositiveButton(getString(R.string.dialog_save_positive)) { _, _ ->
                val filename = editText.text.toString().trim()
                if (FileUtils.isValidFilename(filename)) {
                    saveSegment(segment, filename)
                } else {
                    Toast.makeText(this, getString(R.string.msg_save_invalid_filename), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.dialog_save_negative), null)
            .show()
    }
    
    /** Save a segment to Downloads */
    private fun saveSegment(segment: Segment, filename: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val savedFile = FileUtils.saveSegmentToDownloads(this@PlaybackActivity, segment, filename)
                
                withContext(Dispatchers.Main) {
                    if (savedFile != null) {
                        // Mark segment as saved in database
                        val updatedSegment = segment.copy(isSavedToDownloads = true)
                        database.segmentDao().updateSegment(updatedSegment)
                        
                        Toast.makeText(
                            this@PlaybackActivity,
                            getString(R.string.msg_save_success),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@PlaybackActivity,
                            getString(R.string.msg_save_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                AppLog.e(TAG, "Error saving segment", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PlaybackActivity,
                        getString(R.string.msg_save_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /** Show delete segment confirmation dialog */
    private fun showDeleteSegmentDialog(segment: Segment) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_delete_segment_title))
            .setMessage(getString(R.string.dialog_delete_segment_message))
            .setPositiveButton(getString(R.string.btn_delete)) { _, _ ->
                deleteSegment(segment)
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }
    
    /** Show delete all segments confirmation dialog */
    private fun showDeleteAllDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_delete_all_title))
            .setMessage(getString(R.string.dialog_delete_all_message))
            .setPositiveButton(getString(R.string.btn_delete)) { _, _ ->
                deleteAllSegments()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }
    
    /** Delete a single segment */
    private fun deleteSegment(segment: Segment) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val success = SegmentManager.deleteSegment(this@PlaybackActivity, segment)
                
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(
                            this@PlaybackActivity,
                            getString(R.string.msg_delete_success),
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // If we deleted the currently playing segment, stop playback
                        if (currentSegment?.id == segment.id) {
                            stopPlayback()
                        }
                    } else {
                        Toast.makeText(
                            this@PlaybackActivity,
                            getString(R.string.msg_delete_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                AppLog.e(TAG, "Error deleting segment", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PlaybackActivity,
                        getString(R.string.msg_delete_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    /** Delete all rotating segments (preserve saved segments) */
    private fun deleteAllSegments() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val success = SegmentManager.deleteRotatingSegments(this@PlaybackActivity)
                
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(
                            this@PlaybackActivity,
                            getString(R.string.msg_delete_rotating_success),
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Stop playback since all rotating segments are gone
                        stopPlayback()
                    } else {
                        Toast.makeText(
                            this@PlaybackActivity,
                            getString(R.string.msg_delete_rotating_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                AppLog.e(TAG, "Error deleting rotating segments", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PlaybackActivity,
                        getString(R.string.msg_delete_rotating_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /** Load segments from database */
    private fun loadSegments() {
        lifecycleScope.launch {
            try {
                database.segmentDao().getAllSegments().collect { segments ->
                    AppLog.d(TAG, "Loaded ${segments.size} segments")
                    updateSegmentsList(segments)
                }
            } catch (e: Exception) {
                AppLog.e(TAG, "Error loading segments", e)
            }
        }
    }
    
    /** Update the segments list in the UI */
    private fun updateSegmentsList(segments: List<Segment>) {
        this.segments = segments
        adapter.submitList(segments)
        updateNavigationButtons()
    }
    
    /** Play a specific segment */
    fun playSegment(segment: Segment) {
        stopPlayback()
        
        try {
            val file = File(segment.filePath)
            if (!file.exists()) {
                AppLog.e(TAG, "Segment file does not exist: ${segment.filePath}")
                return
            }
            
            if (!requestAudioFocus()) {
                AppLog.w(TAG, "Audio focus not granted; continuing cautiously")
            }
            
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(segment.filePath)
                prepare()
                start()
            }
            
            currentSegment = segment
            btnPlayPause.isEnabled = true
            btnStop.isEnabled = true
            btnSave.isEnabled = true
            seekBar.isEnabled = true
            seekBar.max = getDuration()
            tvTotalTime.text = formatMs(getDuration().toLong())
            tvCurrentTime.text = formatMs(0)
            startProgressUpdater()
            updatePlayPauseButton()
            
            AppLog.d(TAG, "Started playing segment: ${segment.filePath}")
            
        } catch (e: Exception) {
            AppLog.e(TAG, "Error playing segment", e)
        }
    }
    
    /** Pause playback */
    fun pausePlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                AppLog.d(TAG, "Playback paused")
            }
        }
    }
    
    /** Resume playback */
    fun resumePlayback() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                if (!requestAudioFocus()) {
                    AppLog.w(TAG, "Audio focus not granted; continuing cautiously")
                }
                player.start()
                AppLog.d(TAG, "Playback resumed")
            }
        }
    }
    
    /** Stop playback */
    fun stopPlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }
        mediaPlayer = null
        currentSegment = null
        abandonAudioFocus()
        btnPlayPause.isEnabled = false
        btnStop.isEnabled = false
        btnSave.isEnabled = false
        seekBar.isEnabled = false
        updatePlayPauseButton()
        AppLog.d(TAG, "Playback stopped")
    }
    
    private fun requestAudioFocus(): Boolean {
        val manager = audioManager ?: return false
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener { /* no-op basic handling */ }
                .build()
            audioFocusRequest = req
            val res = manager.requestAudioFocus(req)
            res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            val res = manager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }
    
    private fun abandonAudioFocus() {
        val manager = audioManager ?: return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest?.let { manager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            manager.abandonAudioFocus(null)
        }
    }
    
    /** Get current playback position */
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }
    
    /** Get total duration of current segment */
    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }
    
    /** Seek to specific position */
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }
    
    /** Check if currently playing */
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
    
    private fun formatMs(ms: Long): String {
        val minutes = ms / 60000
        val seconds = (ms % 60000) / 1000
        return String.format("%d:%02d", minutes, seconds)
    }
    
    private fun startProgressUpdater() {
        progressJob?.cancel()
        progressJob = lifecycleScope.launchWhenStarted {
            while (isActive && isPlaying()) {
                val pos = getCurrentPosition()
                seekBar.progress = pos
                tvCurrentTime.text = formatMs(pos.toLong())
                delay(250)
            }
        }
    }
    
    companion object {
        private const val TAG = "PlaybackActivity"
    }
} 