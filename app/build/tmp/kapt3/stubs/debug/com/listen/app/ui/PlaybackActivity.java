package com.listen.app.ui;

/**
 * Activity for playing back recorded audio segments
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010 \n\u0002\b\u0002\u0018\u0000 \u001f2\u00020\u0001:\u0001\u001fB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\t\u001a\u00020\nJ\u0006\u0010\u000b\u001a\u00020\nJ\u0006\u0010\f\u001a\u00020\rJ\b\u0010\u000e\u001a\u00020\u000fH\u0002J\u0012\u0010\u0010\u001a\u00020\u000f2\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u0014J\b\u0010\u0013\u001a\u00020\u000fH\u0014J\u0006\u0010\u0014\u001a\u00020\u000fJ\u000e\u0010\u0015\u001a\u00020\u000f2\u0006\u0010\u0016\u001a\u00020\u0004J\u0006\u0010\u0017\u001a\u00020\u000fJ\u000e\u0010\u0018\u001a\u00020\u000f2\u0006\u0010\u0019\u001a\u00020\nJ\b\u0010\u001a\u001a\u00020\u000fH\u0002J\u0006\u0010\u001b\u001a\u00020\u000fJ\u0016\u0010\u001c\u001a\u00020\u000f2\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00040\u001eH\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Lcom/listen/app/ui/PlaybackActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "currentSegment", "Lcom/listen/app/data/Segment;", "database", "Lcom/listen/app/data/ListenDatabase;", "mediaPlayer", "Landroid/media/MediaPlayer;", "getCurrentPosition", "", "getDuration", "isPlaying", "", "loadSegments", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "pausePlayback", "playSegment", "segment", "resumePlayback", "seekTo", "position", "setupUI", "stopPlayback", "updateSegmentsList", "segments", "", "Companion", "app_debug"})
public final class PlaybackActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.listen.app.data.ListenDatabase database;
    @org.jetbrains.annotations.Nullable
    private android.media.MediaPlayer mediaPlayer;
    @org.jetbrains.annotations.Nullable
    private com.listen.app.data.Segment currentSegment;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "PlaybackActivity";
    @org.jetbrains.annotations.NotNull
    public static final com.listen.app.ui.PlaybackActivity.Companion Companion = null;
    
    public PlaybackActivity() {
        super();
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override
    protected void onDestroy() {
    }
    
    /**
     * Set up the user interface
     */
    private final void setupUI() {
    }
    
    /**
     * Load segments from database
     */
    private final void loadSegments() {
    }
    
    /**
     * Update the segments list in the UI
     */
    private final void updateSegmentsList(java.util.List<com.listen.app.data.Segment> segments) {
    }
    
    /**
     * Play a specific segment
     */
    public final void playSegment(@org.jetbrains.annotations.NotNull
    com.listen.app.data.Segment segment) {
    }
    
    /**
     * Pause playback
     */
    public final void pausePlayback() {
    }
    
    /**
     * Resume playback
     */
    public final void resumePlayback() {
    }
    
    /**
     * Stop playback
     */
    public final void stopPlayback() {
    }
    
    /**
     * Get current playback position
     */
    public final int getCurrentPosition() {
        return 0;
    }
    
    /**
     * Get total duration of current segment
     */
    public final int getDuration() {
        return 0;
    }
    
    /**
     * Seek to specific position
     */
    public final void seekTo(int position) {
    }
    
    /**
     * Check if currently playing
     */
    public final boolean isPlaying() {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/listen/app/ui/PlaybackActivity$Companion;", "", "()V", "TAG", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}