package com.romp.listen.app.ui.playback

import androidx.lifecycle.viewModelScope
import com.romp.listen.app.data.Segment
import com.romp.listen.app.data.repository.SegmentRepository
import com.romp.listen.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for PlaybackActivity
 */
class PlaybackViewModel(
    private val segmentRepository: SegmentRepository
) : BaseViewModel() {
    
    private val _segments = MutableStateFlow<List<Segment>>(emptyList())
    val segments: StateFlow<List<Segment>> = _segments.asStateFlow()
    
    private val _currentSegment = MutableStateFlow<Segment?>(null)
    val currentSegment: StateFlow<Segment?> = _currentSegment.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()
    
    private val _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()
    
    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()
    
    init {
        loadSegments()
    }
    
    /** Load all segments */
    private fun loadSegments() {
        launchWithErrorHandling {
            // For now, we'll use a simple approach. In a real app, you might want to use Flow
            // and observe changes in real-time
            // Note: This is a simplified approach. In a real implementation, you'd observe the Flow
            _segments.value = emptyList() // Placeholder - would need proper Flow collection
        }
    }
    
    /** Set current segment for playback */
    fun setCurrentSegment(segment: Segment) {
        _currentSegment.value = segment
        _totalDuration.value = segment.duration
        _currentTime.value = 0L
        _playbackProgress.value = 0f
    }
    
    /** Start playback */
    fun startPlayback() {
        _isPlaying.value = true
    }
    
    /** Pause playback */
    fun pausePlayback() {
        _isPlaying.value = false
    }
    
    /** Stop playback */
    fun stopPlayback() {
        _isPlaying.value = false
        _currentTime.value = 0L
        _playbackProgress.value = 0f
    }
    
    /** Update playback progress */
    fun updateProgress(currentTime: Long, totalDuration: Long) {
        _currentTime.value = currentTime
        _totalDuration.value = totalDuration
        _playbackProgress.value = if (totalDuration > 0) {
            currentTime.toFloat() / totalDuration.toFloat()
        } else {
            0f
        }
    }
    
    /** Seek to position */
    fun seekTo(position: Long) {
        _currentTime.value = position
        _playbackProgress.value = if (_totalDuration.value > 0) {
            position.toFloat() / _totalDuration.value.toFloat()
        } else {
            0f
        }
    }
    
    /** Refresh segments */
    fun refreshSegments() {
        loadSegments()
    }
    
    /** Delete segment */
    fun deleteSegment(segment: Segment) {
        launchWithErrorHandling {
            segmentRepository.deleteSegmentById(segment.id)
            loadSegments() // Reload after deletion
        }
    }
    
    /** Validate segment file */
    suspend fun validateSegmentFile(segment: Segment): Boolean {
        return segmentRepository.validateSegmentFile(segment)
    }
    
    /** Get segment by ID */
    suspend fun getSegmentById(segmentId: Long): Segment? {
        return segmentRepository.getSegmentById(segmentId)
    }
} 