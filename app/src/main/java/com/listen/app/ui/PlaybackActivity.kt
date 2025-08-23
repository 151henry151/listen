package com.listen.app.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.listen.app.R
import com.listen.app.data.ListenDatabase
import com.listen.app.data.Segment
import com.listen.app.data.SavedSegment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
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
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.listen.app.ui.fragments.RotatingSegmentsFragment
import com.listen.app.ui.fragments.SavedSegmentsFragment

/**
 * Activity for playing back recorded audio segments
 */
class PlaybackActivity : AppCompatActivity() {
    
    private lateinit var database: ListenDatabase
    private var mediaPlayer: MediaPlayer? = null
    private var currentSegment: Segment? = null
    private var currentSavedSegment: SavedSegment? = null
    
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: PlaybackPagerAdapter
    private lateinit var rotatingSegmentsFragment: RotatingSegmentsFragment
    private lateinit var savedSegmentsFragment: SavedSegmentsFragment
    
    private lateinit var tvCurrentSegment: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var btnPlayPause: Button
    private lateinit var btnStop: Button
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var btnSave: Button
    private lateinit var btnShare: Button
    private lateinit var btnDelete: Button
    private lateinit var btnClearAll: Button
    
    private var segments: List<Segment> = emptyList()
    private var savedSegments: List<SavedSegment> = emptyList()
    private var currentSegmentIndex: Int = -1
    private var currentSavedSegmentIndex: Int = -1
    
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
        loadSavedSegments()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
        abandonAudioFocus()
        progressJob?.cancel()
    }
    
    /** Set up the user interface */
    private fun setupUI() {
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
        tvCurrentSegment = findViewById(R.id.tv_current_segment)
        seekBar = findViewById(R.id.seek_bar)
        tvCurrentTime = findViewById(R.id.tv_current_time)
        tvTotalTime = findViewById(R.id.tv_total_time)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnStop = findViewById(R.id.btn_stop)
        btnPrevious = findViewById(R.id.btn_previous)
        btnNext = findViewById(R.id.btn_next)
        btnSave = findViewById(R.id.btn_save)
        btnShare = findViewById(R.id.btn_share)
        btnDelete = findViewById(R.id.btn_delete)
        btnClearAll = findViewById(R.id.btn_clear_all)
        
        // Set up ViewPager and TabLayout
        pagerAdapter = PlaybackPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_rotating_segments)
                1 -> getString(R.string.tab_saved_segments)
                else -> null
            }
        }.attach()
        
        // Set up fragment click listeners after ViewPager is initialized
        viewPager.post {
            setupFragmentListeners()
        }
        
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
            when (viewPager.currentItem) {
                0 -> currentSegment?.let { segment ->
                    showSaveDialog(segment)
                }
                1 -> currentSavedSegment?.let { savedSegment ->
                    // Already saved, show message
                    Toast.makeText(this, "Segment is already saved", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        btnShare.setOnClickListener {
            AppLog.d(TAG, "Share button clicked, current tab: ${viewPager.currentItem}")
            when (viewPager.currentItem) {
                0 -> {
                    AppLog.d(TAG, "Share not available for rotating segments")
                    // Share not available for rotating segments
                }
                1 -> {
                    AppLog.d(TAG, "Share clicked for saved segments tab, currentSavedSegment: $currentSavedSegment")
                    currentSavedSegment?.let { savedSegment ->
                        AppLog.d(TAG, "Calling shareSavedSegment with: ${savedSegment.filePath}")
                        shareSavedSegment(savedSegment)
                    } ?: run {
                        AppLog.w(TAG, "No saved segment selected for sharing")
                        Toast.makeText(this@PlaybackActivity, "Please select a saved recording to share", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
        btnDelete.setOnClickListener {
            when (viewPager.currentItem) {
                0 -> currentSegment?.let { segment ->
                    showDeleteSegmentDialog(segment)
                }
                1 -> currentSavedSegment?.let { savedSegment ->
                    showDeleteSavedSegmentDialog(savedSegment)
                }
            }
        }
        
        btnClearAll.setOnClickListener {
            when (viewPager.currentItem) {
                0 -> showDeleteAllDialog()
                1 -> showDeleteAllSavedDialog()
            }
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
    
    /** Set up fragment click listeners */
    private fun setupFragmentListeners() {
        // Set up listeners after a short delay to ensure fragments are created
        viewPager.post {
            setupFragmentListenersForCurrentTab()
        }
        
        // Set up page change listener to update listeners when tabs change
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setupFragmentListenersForCurrentTab()
                updateNavigationButtons() // Update button visibility when tab changes
            }
        })
    }
    
    /** Set up fragment listeners for the current tab */
    private fun setupFragmentListenersForCurrentTab() {
        try {
            AppLog.d(TAG, "Setting up fragment listeners for current tab: ${viewPager.currentItem}")
            // Get all fragments and set up listeners
            val fragments = supportFragmentManager.fragments
            AppLog.d(TAG, "Found ${fragments.size} fragments")
            
            for (fragment in fragments) {
                AppLog.d(TAG, "Fragment: ${fragment.javaClass.simpleName}")
                when (fragment) {
                    is RotatingSegmentsFragment -> {
                        AppLog.d(TAG, "Setting up RotatingSegmentsFragment listener")
                        fragment.setOnSegmentClickListener { segment ->
                            AppLog.d(TAG, "RotatingSegmentsFragment segment clicked: ${segment.filePath}")
                            playSegment(segment)
                            updateCurrentSegmentInfo(segment)
                        }
                    }
                    is SavedSegmentsFragment -> {
                        AppLog.d(TAG, "Setting up SavedSegmentsFragment listener")
                        fragment.setOnSavedSegmentClickListener { savedSegment ->
                            AppLog.d(TAG, "SavedSegmentsFragment segment clicked: ${savedSegment.filePath}")
                            playSavedSegment(savedSegment)
                            updateCurrentSavedSegmentInfo(savedSegment)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            AppLog.e(TAG, "Error setting up fragment listeners for current tab", e)
        }
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
        currentSegment = segment
        tvCurrentSegment.text = File(segment.filePath).name
        currentSegmentIndex = segments.indexOf(segment)
        updateNavigationButtons()
        AppLog.d(TAG, "Updated current segment info: ${segment.filePath}")
    }
    
    /** Update navigation buttons state */
    private fun updateNavigationButtons() {
        when (viewPager.currentItem) {
            0 -> {
                // Rotating segments tab
                btnPrevious.isEnabled = currentSegmentIndex > 0
                btnNext.isEnabled = currentSegmentIndex < segments.size - 1
                btnSave.isEnabled = currentSegment != null
                btnDelete.isEnabled = currentSegment != null
                updateButtonVisibilityForRotatingTab()
            }
            1 -> {
                // Saved segments tab
                btnPrevious.isEnabled = currentSavedSegmentIndex > 0
                btnNext.isEnabled = currentSavedSegmentIndex < savedSegments.size - 1
                btnSave.isEnabled = false // Not needed for saved segments
                btnDelete.isEnabled = currentSavedSegment != null
                updateButtonVisibilityForSavedTab()
            }
        }
    }
    
    /** Update button visibility for rotating segments tab */
    private fun updateButtonVisibilityForRotatingTab() {
        btnSave.visibility = android.view.View.VISIBLE
        btnShare.visibility = android.view.View.GONE
    }
    
    /** Update button visibility for saved segments tab */
    private fun updateButtonVisibilityForSavedTab() {
        btnSave.visibility = android.view.View.GONE
        btnShare.visibility = android.view.View.VISIBLE
    }
    
    /** Play the previous segment */
    private fun playPreviousSegment() {
        when (viewPager.currentItem) {
            0 -> {
                if (currentSegmentIndex > 0) {
                    val previousSegment = segments[currentSegmentIndex - 1]
                    playSegment(previousSegment)
                    updateCurrentSegmentInfo(previousSegment)
                }
            }
            1 -> {
                if (currentSavedSegmentIndex > 0) {
                    val previousSavedSegment = savedSegments[currentSavedSegmentIndex - 1]
                    playSavedSegment(previousSavedSegment)
                    updateCurrentSavedSegmentInfo(previousSavedSegment)
                }
            }
        }
    }
    
    /** Play the next segment */
    private fun playNextSegment() {
        when (viewPager.currentItem) {
            0 -> {
                if (currentSegmentIndex < segments.size - 1) {
                    val nextSegment = segments[currentSegmentIndex + 1]
                    playSegment(nextSegment)
                    updateCurrentSegmentInfo(nextSegment)
                }
            }
            1 -> {
                if (currentSavedSegmentIndex < savedSegments.size - 1) {
                    val nextSavedSegment = savedSegments[currentSavedSegmentIndex + 1]
                    playSavedSegment(nextSavedSegment)
                    updateCurrentSavedSegmentInfo(nextSavedSegment)
                }
            }
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
    
    /** Save a segment to the custom saved segments directory */
    private fun saveSegment(segment: Segment, filename: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val savedFile = FileUtils.saveSegmentToSavedDirectory(this@PlaybackActivity, segment, filename)
                
                withContext(Dispatchers.Main) {
                    if (savedFile != null) {
                        // Don't modify the original segment - keep it in rotating tab
                        // The saved file is a copy that exists independently
                        
                        Toast.makeText(
                            this@PlaybackActivity,
                            getString(R.string.msg_save_success),
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Refresh saved segments list with a small delay to ensure file system is updated
                        lifecycleScope.launch {
                            delay(500) // Small delay to ensure file system is updated
                            loadSavedSegments() // Refresh saved segments list
                        }
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
                // Delete all segments from database, but saved copies remain in saved directory
                val success = SegmentManager.deleteAllSegments(this@PlaybackActivity)
                
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(
                            this@PlaybackActivity,
                            getString(R.string.msg_delete_rotating_success),
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Stop playback since all rotating segments are gone
                        stopPlayback()
                        
                        // Refresh the segments list to update the UI
                        loadSegments()
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
    
    /** Load saved segments from custom directory */
    private fun loadSavedSegments() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                AppLog.d(TAG, "Loading saved segments...")
                val savedFiles = FileUtils.getSavedSegmentFiles()
                AppLog.d(TAG, "Found ${savedFiles.size} saved segment files")
                
                val savedSegmentsList = savedFiles.map { file ->
                    SavedSegment(file)
                }
                
                withContext(Dispatchers.Main) {
                    savedSegments = savedSegmentsList
                    AppLog.d(TAG, "Updated saved segments list with ${savedSegmentsList.size} items")
                    updateSavedSegmentsList(savedSegments)
                }
            } catch (e: Exception) {
                AppLog.e(TAG, "Error loading saved segments", e)
            }
        }
    }
    
    /** Update the segments list in the UI */
    private fun updateSegmentsList(segments: List<Segment>) {
        this.segments = segments
        // Find the rotating segments fragment and update it
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is RotatingSegmentsFragment) {
                fragment.updateSegments(segments)
                break
            }
        }
        updateNavigationButtons()
        
        // Ensure fragment listeners are set up
        setupFragmentListeners()
    }
    
    /** Update the saved segments list in the UI */
    private fun updateSavedSegmentsList(savedSegments: List<SavedSegment>) {
        this.savedSegments = savedSegments
        AppLog.d(TAG, "Updating saved segments list in UI with ${savedSegments.size} items")
        
        // Find the saved segments fragment and update it
        val fragments = supportFragmentManager.fragments
        var fragmentFound = false
        for (fragment in fragments) {
            if (fragment is SavedSegmentsFragment) {
                fragment.updateSavedSegments(savedSegments)
                fragmentFound = true
                AppLog.d(TAG, "Updated SavedSegmentsFragment with ${savedSegments.size} items")
                break
            }
        }
        
        if (!fragmentFound) {
            AppLog.w(TAG, "SavedSegmentsFragment not found in fragments")
        }
        
        updateSavedNavigationButtons()
        
        // Ensure fragment listeners are set up
        setupFragmentListeners()
    }
    
    /** Play a saved segment */
    private fun playSavedSegment(savedSegment: SavedSegment) {
        AppLog.d(TAG, "playSavedSegment called with segment: ${savedSegment.filePath}")
        stopPlayback()
        
        try {
            val file = savedSegment.file
            if (!file.exists()) {
                AppLog.e(TAG, "Saved segment file does not exist: ${savedSegment.filePath}")
                return
            }
            
            AppLog.d(TAG, "Saved segment file exists, size: ${file.length()} bytes")
            
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
                setDataSource(savedSegment.filePath)
                setOnPreparedListener { player ->
                    try {
                        player.start()
                        currentSavedSegment = savedSegment
                        btnPlayPause.isEnabled = true
                        btnStop.isEnabled = true
                        btnSave.isEnabled = false // Already saved
                        btnDelete.isEnabled = true
                        seekBar.isEnabled = true
                        seekBar.max = player.duration
                        tvTotalTime.text = formatMs(player.duration.toLong())
                        tvCurrentTime.text = formatMs(0)
                        startProgressUpdater()
                        updatePlayPauseButton()
                        AppLog.d(TAG, "Started playing saved segment: ${savedSegment.filePath}")
                    } catch (e: Exception) {
                        AppLog.e(TAG, "Error starting playback after preparation", e)
                    }
                }
                setOnErrorListener { player, what, extra ->
                    AppLog.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    true
                }
                prepareAsync()
            }
            
        } catch (e: Exception) {
            AppLog.e(TAG, "Error playing saved segment", e)
        }
    }
    
    /** Update current saved segment information display */
    private fun updateCurrentSavedSegmentInfo(savedSegment: SavedSegment) {
        AppLog.d(TAG, "updateCurrentSavedSegmentInfo called with: ${savedSegment.filename}")
        currentSavedSegment = savedSegment
        tvCurrentSegment.text = savedSegment.filename
        currentSavedSegmentIndex = savedSegments.indexOf(savedSegment)
        updateSavedNavigationButtons()
    }
    
    /** Update saved navigation buttons state */
    private fun updateSavedNavigationButtons() {
        btnPrevious.isEnabled = currentSavedSegmentIndex > 0
        btnNext.isEnabled = currentSavedSegmentIndex < savedSegments.size - 1
        btnSave.isEnabled = false // Already saved
        btnShare.isEnabled = currentSavedSegment != null
        btnDelete.isEnabled = currentSavedSegment != null
    }
    
    /** Show delete saved segment confirmation dialog */
    private fun showDeleteSavedSegmentDialog(savedSegment: SavedSegment) {
        AlertDialog.Builder(this)
            .setTitle("Delete Saved Recording")
            .setMessage("Are you sure you want to delete this saved recording? This action cannot be undone.")
            .setPositiveButton(getString(R.string.btn_delete)) { _, _ ->
                deleteSavedSegment(savedSegment)
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }
    
    /** Delete a saved segment */
    private fun deleteSavedSegment(savedSegment: SavedSegment) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val success = FileUtils.deleteSavedSegment(savedSegment.file)
                
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(
                            this@PlaybackActivity,
                            "Saved recording deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // If we deleted the currently playing saved segment, stop playback
                        if (currentSavedSegment?.filePath == savedSegment.filePath) {
                            stopPlayback()
                        }
                        
                        // Refresh saved segments list
                        loadSavedSegments()
                    } else {
                        Toast.makeText(
                            this@PlaybackActivity,
                            "Error deleting saved recording",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                AppLog.e(TAG, "Error deleting saved segment", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PlaybackActivity,
                        "Error deleting saved recording",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    /** Show delete all saved segments confirmation dialog */
    private fun showDeleteAllSavedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete All Saved Recordings")
            .setMessage("Are you sure you want to delete all saved recordings? This action cannot be undone.")
            .setPositiveButton(getString(R.string.btn_delete)) { _, _ ->
                deleteAllSavedSegments()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }
    
    /** Delete all saved segments */
    private fun deleteAllSavedSegments() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                var successCount = 0
                val totalCount = savedSegments.size
                
                for (savedSegment in savedSegments) {
                    if (FileUtils.deleteSavedSegment(savedSegment.file)) {
                        successCount++
                    }
                }
                
                withContext(Dispatchers.Main) {
                    if (successCount == totalCount) {
                        Toast.makeText(
                            this@PlaybackActivity,
                            "All saved recordings deleted successfully",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Stop playback since all saved segments are gone
                        stopPlayback()
                        
                        // Refresh saved segments list
                        loadSavedSegments()
                    } else {
                        Toast.makeText(
                            this@PlaybackActivity,
                            "Error deleting some saved recordings",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                AppLog.e(TAG, "Error deleting all saved segments", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PlaybackActivity,
                        "Error deleting saved recordings",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /** Play a specific segment */
    fun playSegment(segment: Segment) {
        AppLog.d(TAG, "playSegment called with segment: ${segment.filePath}")
        stopPlayback()
        
        try {
            val file = File(segment.filePath)
            if (!file.exists()) {
                AppLog.e(TAG, "Segment file does not exist: ${segment.filePath}")
                return
            }
            
            AppLog.d(TAG, "Segment file exists, size: ${file.length()} bytes")
            
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
                setOnPreparedListener { player ->
                    try {
                        player.start()
                        currentSegment = segment
                        btnPlayPause.isEnabled = true
                        btnStop.isEnabled = true
                        btnSave.isEnabled = true
                        btnDelete.isEnabled = true
                        seekBar.isEnabled = true
                        seekBar.max = player.duration
                        tvTotalTime.text = formatMs(player.duration.toLong())
                        tvCurrentTime.text = formatMs(0)
                        startProgressUpdater()
                        updatePlayPauseButton()
                        AppLog.d(TAG, "Started playing segment: ${segment.filePath}")
                    } catch (e: Exception) {
                        AppLog.e(TAG, "Error starting playback after preparation", e)
                    }
                }
                setOnErrorListener { player, what, extra ->
                    AppLog.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    true
                }
                prepareAsync()
            }
            
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
        currentSavedSegment = null
        abandonAudioFocus()
        btnPlayPause.isEnabled = false
        btnStop.isEnabled = false
        btnSave.isEnabled = false
        btnDelete.isEnabled = false
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

    private fun shareSavedSegment(savedSegment: SavedSegment) {
        AppLog.d(TAG, "shareSavedSegment called with file: ${savedSegment.filePath}")
        val file = savedSegment.file
        AppLog.d(TAG, "File exists: ${file.exists()}, File size: ${file.length()}")
        
        if (file.exists()) {
            try {
                val fileUri = FileProvider.getUriForFile(this@PlaybackActivity, "${this@PlaybackActivity.packageName}.fileprovider", file)
                AppLog.d(TAG, "FileProvider URI: $fileUri")
                
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "audio/*"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    putExtra(Intent.EXTRA_TEXT, "Check out this saved recording!")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                AppLog.d(TAG, "Starting share activity")
                startActivity(Intent.createChooser(shareIntent, "Share Saved Recording"))
            } catch (e: Exception) {
                AppLog.e(TAG, "Error sharing saved segment", e)
                Toast.makeText(this, "Error sharing file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            AppLog.w(TAG, "Saved segment file not found: ${savedSegment.filePath}")
            Toast.makeText(this, "Saved segment file not found for sharing.", Toast.LENGTH_SHORT).show()
        }
    }
    
    companion object {
        private const val TAG = "PlaybackActivity"
    }
} 