package com.listen.app.data;

/**
 * Data Access Object for Segment entities
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u000e\bg\u0018\u00002\u00020\u0001J\u0011\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0004J\u0019\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\bJ\u001f\u0010\t\u001a\u00020\u00032\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rJ\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000bH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u000b0\u0011H\'J\u001f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00070\u000b2\u0006\u0010\u0013\u001a\u00020\u0014H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0015J\u001b\u0010\u0016\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u0017\u001a\u00020\fH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0018J\u0011\u0010\u0019\u001a\u00020\u0014H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0004J$\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u000b0\u00112\u0006\u0010\u001b\u001a\u00020\f2\u0006\u0010\u001c\u001a\u00020\fH\'J\u001f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00070\u000b2\u0006\u0010\u001e\u001a\u00020\fH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0018J\u0013\u0010\u001f\u001a\u0004\u0018\u00010\fH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0004J\u0019\u0010 \u001a\u00020\f2\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\bJ\u0019\u0010!\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\b\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\""}, d2 = {"Lcom/listen/app/data/SegmentDao;", "", "deleteAllSegments", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteSegment", "segment", "Lcom/listen/app/data/Segment;", "(Lcom/listen/app/data/Segment;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteSegmentsByIds", "segmentIds", "", "", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllFilePaths", "", "getAllSegments", "Lkotlinx/coroutines/flow/Flow;", "getOldestSegments", "limit", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getSegmentById", "segmentId", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getSegmentCount", "getSegmentsInRange", "startTime", "endTime", "getSegmentsOlderThan", "timestamp", "getTotalStorageUsage", "insertSegment", "updateSegment", "app_debug"})
@androidx.room.Dao
public abstract interface SegmentDao {
    
    /**
     * Get all segments ordered by start time (newest first)
     */
    @androidx.room.Query(value = "SELECT * FROM segments ORDER BY startTime DESC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.listen.app.data.Segment>> getAllSegments();
    
    /**
     * Get segments within a time range
     */
    @androidx.room.Query(value = "SELECT * FROM segments WHERE startTime >= :startTime AND endTime <= :endTime ORDER BY startTime DESC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.listen.app.data.Segment>> getSegmentsInRange(long startTime, long endTime);
    
    /**
     * Get segments older than the given timestamp
     */
    @androidx.room.Query(value = "SELECT * FROM segments WHERE endTime < :timestamp ORDER BY startTime ASC")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getSegmentsOlderThan(long timestamp, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.listen.app.data.Segment>> $completion);
    
    /**
     * Get the oldest segments (for cleanup)
     */
    @androidx.room.Query(value = "SELECT * FROM segments ORDER BY startTime ASC LIMIT :limit")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getOldestSegments(int limit, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.listen.app.data.Segment>> $completion);
    
    /**
     * Get all file paths (for orphaned file cleanup)
     */
    @androidx.room.Query(value = "SELECT filePath FROM segments")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getAllFilePaths(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion);
    
    /**
     * Get total storage usage
     */
    @androidx.room.Query(value = "SELECT SUM(fileSize) FROM segments")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getTotalStorageUsage(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    /**
     * Get segment count
     */
    @androidx.room.Query(value = "SELECT COUNT(*) FROM segments")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getSegmentCount(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    /**
     * Insert a new segment
     */
    @androidx.room.Insert
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertSegment(@org.jetbrains.annotations.NotNull
    com.listen.app.data.Segment segment, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    /**
     * Update an existing segment
     */
    @androidx.room.Update
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object updateSegment(@org.jetbrains.annotations.NotNull
    com.listen.app.data.Segment segment, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Delete a segment
     */
    @androidx.room.Delete
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteSegment(@org.jetbrains.annotations.NotNull
    com.listen.app.data.Segment segment, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Delete segments by IDs
     */
    @androidx.room.Query(value = "DELETE FROM segments WHERE id IN (:segmentIds)")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteSegmentsByIds(@org.jetbrains.annotations.NotNull
    java.util.List<java.lang.Long> segmentIds, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Delete all segments
     */
    @androidx.room.Query(value = "DELETE FROM segments")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteAllSegments(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Get segment by ID
     */
    @androidx.room.Query(value = "SELECT * FROM segments WHERE id = :segmentId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getSegmentById(long segmentId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.listen.app.data.Segment> $completion);
}