package com.listen.app.data;

/**
 * Represents an audio segment in the database
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0018\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0005\b\u0087\b\u0018\u0000 \'2\u00020\u0001:\u0001\'BA\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\u0006\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0003H\u00c6\u0003JO\u0010\u001c\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u001d\u001a\u00020\u001e2\b\u0010\u001f\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\u0006\u0010 \u001a\u00020\u0005J\u0006\u0010!\u001a\u00020\u0005J\t\u0010\"\u001a\u00020#H\u00d6\u0001J\u000e\u0010$\u001a\u00020\u001e2\u0006\u0010%\u001a\u00020\u0003J\t\u0010&\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\rR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\rR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\rR\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\r\u00a8\u0006("}, d2 = {"Lcom/listen/app/data/Segment;", "", "id", "", "filePath", "", "startTime", "endTime", "duration", "fileSize", "createdAt", "(JLjava/lang/String;JJJJJ)V", "getCreatedAt", "()J", "getDuration", "getEndTime", "getFilePath", "()Ljava/lang/String;", "getFileSize", "getId", "getStartTime", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "getFormattedDuration", "getFormattedStartTime", "hashCode", "", "isOlderThan", "timestamp", "toString", "Companion", "app_debug"})
@androidx.room.Entity(tableName = "segments")
public final class Segment {
    @androidx.room.PrimaryKey(autoGenerate = true)
    private final long id = 0L;
    
    /**
     * File path to the audio segment
     */
    @org.jetbrains.annotations.NotNull
    private final java.lang.String filePath = null;
    
    /**
     * Start timestamp of the segment
     */
    private final long startTime = 0L;
    
    /**
     * End timestamp of the segment
     */
    private final long endTime = 0L;
    
    /**
     * Duration of the segment in milliseconds
     */
    private final long duration = 0L;
    
    /**
     * File size in bytes
     */
    private final long fileSize = 0L;
    
    /**
     * Creation timestamp
     */
    private final long createdAt = 0L;
    @org.jetbrains.annotations.NotNull
    public static final com.listen.app.data.Segment.Companion Companion = null;
    
    public Segment(long id, @org.jetbrains.annotations.NotNull
    java.lang.String filePath, long startTime, long endTime, long duration, long fileSize, long createdAt) {
        super();
    }
    
    public final long getId() {
        return 0L;
    }
    
    /**
     * File path to the audio segment
     */
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFilePath() {
        return null;
    }
    
    /**
     * Start timestamp of the segment
     */
    public final long getStartTime() {
        return 0L;
    }
    
    /**
     * End timestamp of the segment
     */
    public final long getEndTime() {
        return 0L;
    }
    
    /**
     * Duration of the segment in milliseconds
     */
    public final long getDuration() {
        return 0L;
    }
    
    /**
     * File size in bytes
     */
    public final long getFileSize() {
        return 0L;
    }
    
    /**
     * Creation timestamp
     */
    public final long getCreatedAt() {
        return 0L;
    }
    
    /**
     * Check if this segment is older than the given timestamp
     */
    public final boolean isOlderThan(long timestamp) {
        return false;
    }
    
    /**
     * Get the formatted start time for display
     */
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFormattedStartTime() {
        return null;
    }
    
    /**
     * Get the formatted duration for display
     */
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFormattedDuration() {
        return null;
    }
    
    public final long component1() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    public final long component3() {
        return 0L;
    }
    
    public final long component4() {
        return 0L;
    }
    
    public final long component5() {
        return 0L;
    }
    
    public final long component6() {
        return 0L;
    }
    
    public final long component7() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.listen.app.data.Segment copy(long id, @org.jetbrains.annotations.NotNull
    java.lang.String filePath, long startTime, long endTime, long duration, long fileSize, long createdAt) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002\u00a8\u0006\u0007"}, d2 = {"Lcom/listen/app/data/Segment$Companion;", "", "()V", "formatTimestamp", "", "timestamp", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        private final java.lang.String formatTimestamp(long timestamp) {
            return null;
        }
    }
}