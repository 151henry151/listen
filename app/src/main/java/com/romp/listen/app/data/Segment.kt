package com.romp.listen.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an audio segment in the database
 */
@Entity(tableName = "segments")
data class Segment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** File path to the audio segment */
    val filePath: String,
    
    /** Start timestamp of the segment */
    val startTime: Long,
    
    /** End timestamp of the segment */
    val endTime: Long,
    
    /** Duration of the segment in milliseconds */
    val duration: Long,
    
    /** File size in bytes */
    val fileSize: Long,

    /** Whether this segment is a phone call recording */
    val isPhoneCall: Boolean = false,

    /** Direction of call when isPhoneCall is true: "INCOMING" or "OUTGOING" */
    val callDirection: String? = null,

    /** Caller ID (incoming) or dialed number (outgoing) when available */
    val phoneNumber: String? = null,
    
    /** Whether this segment has been saved to Downloads */
    val isSavedToDownloads: Boolean = false,
    
    /** Creation timestamp */
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Check if this segment is older than the given timestamp */
    fun isOlderThan(timestamp: Long): Boolean {
        return endTime < timestamp
    }
    
    /** Get the formatted start time for display */
    fun getFormattedStartTime(): String {
        return formatTimestamp(startTime)
    }
    
    /** Get the formatted duration for display */
    fun getFormattedDuration(): String {
        val minutes = duration / 60000
        val seconds = (duration % 60000) / 1000
        return String.format("%d:%02d", minutes, seconds)
    }
    
    companion object {
        private fun formatTimestamp(timestamp: Long): String {
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            return format.format(date)
        }
    }
} 