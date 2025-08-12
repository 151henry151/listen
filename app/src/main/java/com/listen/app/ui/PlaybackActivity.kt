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

/**
 * Activity for playing back recorded audio segments
 */
class PlaybackActivity : AppCompatActivity() {
    
    private lateinit var database: ListenDatabase
    private var mediaPlayer: MediaPlayer? = null
    private var currentSegment: Segment? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playback)
        
        // Initialize database
        database = ListenDatabase.getDatabase(this)
        
        // Set up UI
        setupUI()
        
        // Load segments
        loadSegments()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
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
            
            mediaPlayer = MediaPlayer().apply {
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
        Log.d(TAG, "Playback stopped")
        // TODO: Update UI to show stopped state
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