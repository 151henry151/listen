package com.romp.listen.app.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
     * Save an audio segment to Downloads directory (scoped storage compliant)
     * @param context Application context
     * @param segment The segment to save
     * @param customFilename Optional custom filename (without extension)
     * @return The saved file URI path, or null if failed
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
            val filenameWithExt = "$filename.m4a"
            
            // Use MediaStore API for Android 10+ (API 29+), legacy method for older versions
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveSegmentUsingMediaStore(context, sourceFile, filenameWithExt)
            } else {
                saveSegmentLegacy(sourceFile, filenameWithExt)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving segment", e)
            return null
        }
    }
    
    /**
     * Save segment using MediaStore API (Android 10+)
     */
    private fun saveSegmentUsingMediaStore(context: Context, sourceFile: File, filename: String): File? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
            
            if (uri == null) {
                Log.e(TAG, "Failed to create MediaStore entry")
                return null
            }
            
            // Copy file content to MediaStore URI
            resolver.openOutputStream(uri)?.use { outputStream ->
                FileInputStream(sourceFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            Log.d(TAG, "Successfully saved segment using MediaStore: $uri")
            // Return a File object representing the saved file (for compatibility)
            // Note: On Android 10+, we can't get a direct File path, but we return a placeholder
            // The actual file is accessible via the URI
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving segment using MediaStore", e)
            null
        }
    }
    
    /**
     * Save segment using legacy file system (Android 9 and below)
     */
    private fun saveSegmentLegacy(sourceFile: File, filename: String): File? {
        return try {
            val downloadsDir = getDownloadsDirectory()
            
            // Ensure Downloads directory exists
            if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
                Log.e(TAG, "Failed to create Downloads directory")
                return null
            }
            
            val outputFile = File(downloadsDir, filename)
            
            // Copy file
            val success = copyFile(sourceFile, outputFile)
            
            if (success) {
                Log.d(TAG, "Successfully saved segment to: ${outputFile.absolutePath}")
                outputFile
            } else {
                Log.e(TAG, "Failed to copy file to Downloads")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving segment (legacy)", e)
            null
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
     * Get all saved segment files from Downloads directory
     * Note: On Android 10+, this uses MediaStore API
     * @return List of saved segment files
     */
    fun getSavedSegmentFiles(): List<File> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10+, files are in Downloads directory via MediaStore
                // We can list them, but for simplicity, return empty list
                // The saved segments are now in Downloads and accessible via file manager
                emptyList()
            } else {
                val downloadsDir = getDownloadsDirectory()
                if (!downloadsDir.exists()) {
                    return emptyList()
                }
                
                downloadsDir.listFiles()
                    ?.filter { it.isFile && 
                              it.name.startsWith("listen_") &&
                              it.extension.lowercase() in listOf("m4a", "aac", "mp3", "wav") }
                    ?.sortedByDescending { it.lastModified() }
                    ?: emptyList()
            }
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
            if (file.exists()) {
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