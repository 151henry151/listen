package com.listen.app.audio;

/**
 * Handles continuous audio recording with optimal settings for speech clarity
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\u0010\u0002\n\u0002\b\u0011\u0018\u0000 $2\u00020\u0001:\u0001$B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u001a\u001a\u00020\u0014J\u0006\u0010\u001b\u001a\u00020\u0013J\b\u0010\u001c\u001a\u0004\u0018\u00010\fJ\u0006\u0010\r\u001a\u00020\u000eJ\b\u0010\u001d\u001a\u0004\u0018\u00010\fJ\u0006\u0010\u001e\u001a\u00020\u000eJ\b\u0010\u001f\u001a\u0004\u0018\u00010\fJ\u001e\u0010 \u001a\u00020\u00142\u0006\u0010!\u001a\u00020\b2\u0006\u0010\"\u001a\u00020\b2\u0006\u0010#\u001a\u00020\bR\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R4\u0010\u0011\u001a\u001c\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u0014\u0018\u00010\u0012X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u0016\"\u0004\b\u0017\u0010\u0018R\u000e\u0010\u0019\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"Lcom/listen/app/audio/AudioRecorderService;", "", "context", "Landroid/content/Context;", "storageManager", "Lcom/listen/app/storage/StorageManager;", "(Landroid/content/Context;Lcom/listen/app/storage/StorageManager;)V", "audioBitrate", "", "audioChannels", "audioSampleRate", "currentSegmentFile", "Ljava/io/File;", "isRecording", "", "mediaRecorder", "Landroid/media/MediaRecorder;", "onSegmentCompleted", "Lkotlin/Function3;", "", "", "getOnSegmentCompleted", "()Lkotlin/jvm/functions/Function3;", "setOnSegmentCompleted", "(Lkotlin/jvm/functions/Function3;)V", "segmentStartTime", "cleanup", "getCurrentRecordingDuration", "getCurrentSegmentFile", "rotateSegment", "startRecording", "stopRecording", "updateSettings", "bitrate", "sampleRate", "channels", "Companion", "app_debug"})
public final class AudioRecorderService {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final com.listen.app.storage.StorageManager storageManager = null;
    @org.jetbrains.annotations.Nullable
    private android.media.MediaRecorder mediaRecorder;
    @org.jetbrains.annotations.Nullable
    private java.io.File currentSegmentFile;
    private long segmentStartTime = 0L;
    private boolean isRecording = false;
    
    /**
     * Current audio settings
     */
    private int audioBitrate = 32000;
    private int audioSampleRate = 16000;
    private int audioChannels = 1;
    
    /**
     * Callback for when a segment is completed
     */
    @org.jetbrains.annotations.Nullable
    private kotlin.jvm.functions.Function3<? super java.io.File, ? super java.lang.Long, ? super java.lang.Long, kotlin.Unit> onSegmentCompleted;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "AudioRecorderService";
    
    /**
     * Default audio settings for optimal speech recording
     */
    public static final int DEFAULT_BITRATE = 32000;
    public static final int DEFAULT_SAMPLE_RATE = 16000;
    public static final int DEFAULT_CHANNELS = 1;
    @org.jetbrains.annotations.NotNull
    public static final com.listen.app.audio.AudioRecorderService.Companion Companion = null;
    
    public AudioRecorderService(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.listen.app.storage.StorageManager storageManager) {
        super();
    }
    
    /**
     * Callback for when a segment is completed
     */
    @org.jetbrains.annotations.Nullable
    public final kotlin.jvm.functions.Function3<java.io.File, java.lang.Long, java.lang.Long, kotlin.Unit> getOnSegmentCompleted() {
        return null;
    }
    
    /**
     * Callback for when a segment is completed
     */
    public final void setOnSegmentCompleted(@org.jetbrains.annotations.Nullable
    kotlin.jvm.functions.Function3<? super java.io.File, ? super java.lang.Long, ? super java.lang.Long, kotlin.Unit> p0) {
    }
    
    /**
     * Start recording with current settings
     */
    public final boolean startRecording() {
        return false;
    }
    
    /**
     * Stop current recording and return the completed file
     */
    @org.jetbrains.annotations.Nullable
    public final java.io.File stopRecording() {
        return null;
    }
    
    /**
     * Rotate to a new segment (stop current, start new)
     */
    @org.jetbrains.annotations.Nullable
    public final java.io.File rotateSegment() {
        return null;
    }
    
    /**
     * Update audio settings
     */
    public final void updateSettings(int bitrate, int sampleRate, int channels) {
    }
    
    /**
     * Check if currently recording
     */
    public final boolean isRecording() {
        return false;
    }
    
    /**
     * Get current recording duration
     */
    public final long getCurrentRecordingDuration() {
        return 0L;
    }
    
    /**
     * Get current segment file
     */
    @org.jetbrains.annotations.Nullable
    public final java.io.File getCurrentSegmentFile() {
        return null;
    }
    
    /**
     * Clean up resources
     */
    public final void cleanup() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/listen/app/audio/AudioRecorderService$Companion;", "", "()V", "DEFAULT_BITRATE", "", "DEFAULT_CHANNELS", "DEFAULT_SAMPLE_RATE", "TAG", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}