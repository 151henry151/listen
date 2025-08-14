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

/**
 * Activity for playing back recorded audio segments
 */
class PlaybackActivity : AppCompatActivity() {
    
    private lateinit var database: ListenDatabase
    private var mediaPlayer: MediaPlayer? = null
    private var currentSegment: Segment? = null
    
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    
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
    }
    
    /** Set up the user interface */
    private fun setupUI() {
        // TODO: Implement UI setup with ViewBinding
        Log.d(TAG, "PlaybackActivity UI setup completed")
    }
    
    /** Load segments from database */
    private fun loadSegments() {
        lifecycleScope.launch {
            try {
                database.segmentDao().getAllSegments().collect { segments ->
                    Log.d(TAG, "Loaded ${segments.size} segments")
                    // TODO: Update UI with segments list
                    updateSegmentsList(segments)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading segments", e)
            }
        }
    }
    
    /** Update the segments list in the UI */
    private fun updateSegmentsList(segments: List<Segment>) {
        // TODO: Update RecyclerView or ListView with segments
        Log.d(TAG, "Updating segments list with ${segments.size} items")
    }
    
    /** Play a specific segment */
    fun playSegment(segment: Segment) {
        stopPlayback()
        
        try {
            val file = File(segment.filePath)
            if (!file.exists()) {
                Log.e(TAG, "Segment file does not exist: ${segment.filePath}")
                return
            }
            
            if (!requestAudioFocus()) {
                Log.w(TAG, "Audio focus not granted; continuing cautiously")
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
            Log.d(TAG, "Started playing segment: ${segment.filePath}")
            
            // TODO: Update UI to show playing state
            
        } catch (e: Exception) {
            Log.e(TAG, "Error playing segment", e)
        }
    }
    
    /** Pause playback */
    fun pausePlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                Log.d(TAG, "Playback paused")
                // TODO: Update UI to show paused state
            }
        }
    }
    
    /** Resume playback */
    fun resumePlayback() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                if (!requestAudioFocus()) {
                    Log.w(TAG, "Audio focus not granted; continuing cautiously")
                }
                player.start()
                Log.d(TAG, "Playback resumed")
                // TODO: Update UI to show playing state
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
        Log.d(TAG, "Playback stopped")
        // TODO: Update UI to show stopped state
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
    
    companion object {
        private const val TAG = "PlaybackActivity"
    }
} 