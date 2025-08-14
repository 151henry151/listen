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
    private lateinit var adapter: SegmentAdapter
    
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
        
        adapter = SegmentAdapter(emptyList()) { segment ->
            playSegment(segment)
            tvCurrentSegment.text = File(segment.filePath).name
        }
        rvSegments.layoutManager = LinearLayoutManager(this)
        rvSegments.adapter = adapter
        
        btnPlayPause.setOnClickListener {
            if (isPlaying()) {
                pausePlayback()
                btnPlayPause.text = getString(R.string.status_stopped)
            } else {
                resumePlayback()
                btnPlayPause.text = getString(R.string.status_recording)
            }
        }
        
        btnStop.setOnClickListener {
            stopPlayback()
            btnPlayPause.text = getString(R.string.status_stopped)
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
        adapter.submitList(segments)
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
            seekBar.isEnabled = true
            seekBar.max = getDuration()
            tvTotalTime.text = formatMs(getDuration().toLong())
            tvCurrentTime.text = formatMs(0)
            startProgressUpdater()
            
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
        seekBar.isEnabled = false
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