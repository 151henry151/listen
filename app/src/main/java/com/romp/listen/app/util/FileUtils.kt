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
    private const val SAVED_SEGMENTS_SUBDIR = "Listen"

    /**
     * Get the app-specific directory for saved segments.
     * Uses getExternalFilesDir which requires no special permissions and works
     * reliably on all Android versions including Android 15.
     */
    fun getSavedSegmentsDirectory(context: Context): File {
        val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            ?: context.getExternalFilesDir(null)
            ?: context.filesDir
        return File(baseDir, SAVED_SEGMENTS_SUBDIR).apply {
            if (!exists() && !mkdirs()) {
                Log.e(TAG, "Failed to create saved segments directory: $absolutePath")
            }
        }
    }

    /**
     * Get the Downloads directory path (legacy, for reference)
     */
    fun getDownloadsDirectory(): File {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    }

    /**
     * Get the custom saved segments directory path (legacy Documents path)
     */
    fun getSavedSegmentsDirectoryLegacy(): File {
        return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "listen-saved-segments")
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
     * Save an audio segment to app-specific storage (reliable on all Android versions including 15).
     * Files are stored in getExternalFilesDir(Music)/Listen/ and can be shared via the Share button.
     *
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
            if (!sourceFile.canRead()) {
                Log.e(TAG, "Source file is not readable: ${segment.filePath}")
                return null
            }

            val savedDir = getSavedSegmentsDirectory(context)
            if (!savedDir.exists() && !savedDir.mkdirs()) {
                Log.e(TAG, "Failed to create saved segments directory: ${savedDir.absolutePath}")
                return null
            }
            if (!savedDir.canWrite()) {
                Log.e(TAG, "Saved segments directory is not writable: ${savedDir.absolutePath}")
                return null
            }

            val filename = customFilename ?: generateDefaultFilename(segment)
            val filenameWithExt = if (filename.endsWith(".m4a", ignoreCase = true)) filename else "$filename.m4a"
            val outputFile = File(savedDir, filenameWithExt)

            val success = copyFile(sourceFile, outputFile)
            if (success) {
                Log.d(TAG, "Successfully saved segment to: ${outputFile.absolutePath}")
                return outputFile
            }
            Log.e(TAG, "Failed to copy file to saved directory")
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error saving segment: ${e.message}", e)
            return null
        }
    }

    /**
     * Save an audio segment to the Downloads directory (legacy method, for backup/reference)
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

            val filename = customFilename ?: generateDefaultFilename(segment)
            val downloadsDir = getDownloadsDirectory()

            if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
                Log.e(TAG, "Failed to create Downloads directory")
                return null
            }

            val outputFile = File(downloadsDir, "$filename.m4a")
            val success = copyFile(sourceFile, outputFile)

            if (success) {
                Log.d(TAG, "Successfully saved segment to: ${outputFile.absolutePath}")
                return outputFile
            }
            Log.e(TAG, "Failed to copy file to Downloads")
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error saving segment", e)
            return null
        }
    }

    /**
     * Get all saved segment files from app-specific storage.
     * Works on all Android versions.
     */
    fun getSavedSegmentFiles(context: Context): List<File> {
        return try {
            val savedDir = getSavedSegmentsDirectory(context)
            if (!savedDir.exists()) {
                emptyList()
            } else {
                savedDir.listFiles()
                    ?.filter { it.isFile && it.extension.lowercase() in listOf("m4a", "aac", "mp3", "wav") }
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