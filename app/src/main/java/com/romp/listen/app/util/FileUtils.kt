package com.romp.listen.app.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for file operations including saving audio segments
 */
object FileUtils {
    
    private const val TAG = "FileUtils"
    private const val SAVED_SEGMENTS_DIR = "listen-saved-segments"
    
    /**
     * Get the Downloads directory path
     */
    fun getDownloadsDirectory(): File {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    }
    
    /**
     * Get the custom saved segments directory path
     */
    fun getSavedSegmentsDirectory(): File {
        return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), SAVED_SEGMENTS_DIR)
    }
    
    /**
     * Generate a default filename for a segment based on its timestamp
     */
    fun generateDefaultFilename(segment: com.romp.listen.app.data.Segment): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        val timestamp = segment.startTime
        val date = Date(timestamp)
        return "listen_${dateFormat.format(date)}"
    }
    
    /**
     * Save an audio segment to the custom saved segments directory
     * @param context Application context
     * @param segment The segment to save
     * @param customFilename Optional custom filename (without extension)
     * @return The saved file, or null if failed
     */
    fun saveSegmentToSavedDirectory(
        context: Context,
        segment: com.romp.listen.app.data.Segment,
        customFilename: String? = null
    ): File? {
        try {
            val sourceFile = File(segment.filePath)
            if (!sourceFile.exists()) {
                Log.e(TAG, "Source file does not exist: ${segment.filePath}")
                return null
            }
            
            // Generate filename
            val filename = customFilename ?: generateDefaultFilename(segment)
            val savedSegmentsDir = getSavedSegmentsDirectory()
            
            // Ensure saved segments directory exists
            if (!savedSegmentsDir.exists() && !savedSegmentsDir.mkdirs()) {
                Log.e(TAG, "Failed to create saved segments directory")
                return null
            }
            
            // Convert AAC to M4A for better compatibility
            val outputFile = File(savedSegmentsDir, "$filename.m4a")
            
            // For now, we'll do a simple copy since AAC files are already compatible
            // In a future enhancement, we could add proper format conversion
            val success = copyFile(sourceFile, outputFile)
            
            if (success) {
                Log.d(TAG, "Successfully saved segment to: ${outputFile.absolutePath}")
                return outputFile
            } else {
                Log.e(TAG, "Failed to copy file to saved segments directory")
                return null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving segment", e)
            return null
        }
    }
    
    /**
     * Save an audio segment to the Downloads directory (legacy method)
     * @param context Application context
     * @param segment The segment to save
     * @param customFilename Optional custom filename (without extension)
     * @return The saved file, or null if failed
     */
    fun saveSegmentToDownloads(
        context: Context,
        segment: com.romp.listen.app.data.Segment,
        customFilename: String? = null
    ): File? {
        try {
            val sourceFile = File(segment.filePath)
            if (!sourceFile.exists()) {
                Log.e(TAG, "Source file does not exist: ${segment.filePath}")
                return null
            }
            
            // Generate filename
            val filename = customFilename ?: generateDefaultFilename(segment)
            val downloadsDir = getDownloadsDirectory()
            
            // Ensure Downloads directory exists
            if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
                Log.e(TAG, "Failed to create Downloads directory")
                return null
            }
            
            // Convert AAC to M4A for better compatibility
            val outputFile = File(downloadsDir, "$filename.m4a")
            
            // For now, we'll do a simple copy since AAC files are already compatible
            // In a future enhancement, we could add proper format conversion
            val success = copyFile(sourceFile, outputFile)
            
            if (success) {
                Log.d(TAG, "Successfully saved segment to: ${outputFile.absolutePath}")
                return outputFile
            } else {
                Log.e(TAG, "Failed to copy file to Downloads")
                return null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving segment", e)
            return null
        }
    }
    
    /**
     * Get all saved segment files from the custom directory
     * @return List of saved segment files
     */
    fun getSavedSegmentFiles(): List<File> {
        return try {
            val savedSegmentsDir = getSavedSegmentsDirectory()
            if (!savedSegmentsDir.exists()) {
                return emptyList()
            }
            
            savedSegmentsDir.listFiles()
                ?.filter { it.isFile && it.extension.lowercase() in listOf("m4a", "aac", "mp3", "wav") }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting saved segment files", e)
            emptyList()
        }
    }
    
    /**
     * Delete a saved segment file
     * @param file The file to delete
     * @return true if successful, false otherwise
     */
    fun deleteSavedSegment(file: File): Boolean {
        return try {
            if (file.exists() && file.parentFile?.absolutePath?.contains(SAVED_SEGMENTS_DIR) == true) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting saved segment", e)
            false
        }
    }
    
    /**
     * Copy a file from source to destination
     */
    private fun copyFile(source: File, destination: File): Boolean {
        return try {
            FileInputStream(source).use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Error copying file", e)
            false
        }
    }
    
    /**
     * Validate a filename
     */
    fun isValidFilename(filename: String): Boolean {
        return filename.isNotBlank() && 
               filename.length <= 255 && 
               !filename.contains(File.separator) &&
               !filename.contains(File.pathSeparator) &&
               filename.all { it.isLetterOrDigit() || it in "._- " }
    }
    
    /**
     * Get file size in a human-readable format
     */
    fun getFileSizeString(file: File): String {
        val bytes = file.length()
        return when {
            bytes >= 1024 * 1024 * 1024 -> {
                val gb = bytes / (1024.0 * 1024.0 * 1024.0)
                String.format("%.1f GB", gb)
            }
            bytes >= 1024 * 1024 -> {
                val mb = bytes / (1024.0 * 1024.0)
                String.format("%.1f MB", mb)
            }
            bytes >= 1024 -> {
                val kb = bytes / 1024.0
                String.format("%.1f KB", kb)
            }
            else -> "$bytes B"
        }
    }
} 