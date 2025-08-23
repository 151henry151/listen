package com.romp.listen.app.data

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Represents a saved audio segment
 */
data class SavedSegment(
    val file: File,
    val originalSegment: com.romp.listen.app.data.Segment? = null
) {
    /** Get the filename */
    val filename: String = file.name
    
    /** Get the file path */
    val filePath: String = file.absolutePath
    
    /** Get the file size in bytes */
    val fileSize: Long = file.length()
    
    /** Get the last modified timestamp */
    val lastModified: Long = file.lastModified()
    
    /** Get the formatted last modified date */
    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val date = Date(lastModified)
        return dateFormat.format(date)
    }
    
    /** Get the formatted file size */
    fun getFormattedSize(): String {
        return when {
            fileSize < 1024 -> "${fileSize} B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            else -> "${fileSize / (1024 * 1024)} MB"
        }
    }
    
    /** Get the formatted duration (if available from original segment) */
    fun getFormattedDuration(): String {
        return originalSegment?.getFormattedDuration() ?: "Unknown"
    }
    
    /** Check if this is a phone call recording */
    val isPhoneCall: Boolean = originalSegment?.isPhoneCall ?: false
    
    /** Get call direction if available */
    val callDirection: String? = originalSegment?.callDirection
    
    /** Get phone number if available */
    val phoneNumber: String? = originalSegment?.phoneNumber
} 