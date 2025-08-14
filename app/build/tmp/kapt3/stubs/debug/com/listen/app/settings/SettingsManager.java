package com.listen.app.settings;

/**
 * Manages app settings and configuration
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010\t\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u0000 .2\u00020\u0001:\u0001.B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010+\u001a\u00020\u0017J\u0006\u0010,\u001a\u00020-R$\u0010\u0007\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR$\u0010\f\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\r\u0010\t\"\u0004\b\u000e\u0010\u000bR$\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0005\u001a\u00020\u000f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R$\u0010\u0015\u001a\u00020\u000f2\u0006\u0010\u0005\u001a\u00020\u000f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0015\u0010\u0012\"\u0004\b\u0016\u0010\u0014R$\u0010\u0018\u001a\u00020\u00172\u0006\u0010\u0005\u001a\u00020\u00178F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001cR$\u0010\u001d\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u001e\u0010\t\"\u0004\b\u001f\u0010\u000bR\u000e\u0010 \u001a\u00020!X\u0082\u0004\u00a2\u0006\u0002\n\u0000R$\u0010\"\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b#\u0010\t\"\u0004\b$\u0010\u000bR$\u0010%\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b&\u0010\t\"\u0004\b\'\u0010\u000bR$\u0010(\u001a\u00020\u000f2\u0006\u0010\u0005\u001a\u00020\u000f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b)\u0010\u0012\"\u0004\b*\u0010\u0014\u00a8\u0006/"}, d2 = {"Lcom/listen/app/settings/SettingsManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "value", "", "audioBitrate", "getAudioBitrate", "()I", "setAudioBitrate", "(I)V", "audioSampleRate", "getAudioSampleRate", "setAudioSampleRate", "", "autoStartOnBoot", "getAutoStartOnBoot", "()Z", "setAutoStartOnBoot", "(Z)V", "isServiceEnabled", "setServiceEnabled", "", "lastServiceStartTime", "getLastServiceStartTime", "()J", "setLastServiceStartTime", "(J)V", "maxStorageMB", "getMaxStorageMB", "setMaxStorageMB", "prefs", "Landroid/content/SharedPreferences;", "retentionPeriodMinutes", "getRetentionPeriodMinutes", "setRetentionPeriodMinutes", "segmentDurationSeconds", "getSegmentDurationSeconds", "setSegmentDurationSeconds", "showNotification", "getShowNotification", "setShowNotification", "calculateStorageUsage", "getFormattedStorageUsage", "", "Companion", "app_debug"})
public final class SettingsManager {
    @org.jetbrains.annotations.NotNull
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String PREFS_NAME = "listen_settings";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_SERVICE_ENABLED = "service_enabled";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_SEGMENT_DURATION = "segment_duration";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_RETENTION_PERIOD = "retention_period";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_AUDIO_BITRATE = "audio_bitrate";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_AUDIO_SAMPLE_RATE = "audio_sample_rate";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_MAX_STORAGE = "max_storage";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_SHOW_NOTIFICATION = "show_notification";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_AUTO_START_BOOT = "auto_start_boot";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_LAST_SERVICE_START = "last_service_start";
    public static final int DEFAULT_SEGMENT_DURATION = 60;
    public static final int DEFAULT_RETENTION_PERIOD = 10;
    public static final int DEFAULT_AUDIO_BITRATE = 32;
    public static final int DEFAULT_AUDIO_SAMPLE_RATE = 16000;
    public static final int DEFAULT_MAX_STORAGE = 100;
    @org.jetbrains.annotations.NotNull
    public static final com.listen.app.settings.SettingsManager.Companion Companion = null;
    
    public SettingsManager(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    public final boolean isServiceEnabled() {
        return false;
    }
    
    public final void setServiceEnabled(boolean value) {
    }
    
    public final int getSegmentDurationSeconds() {
        return 0;
    }
    
    public final void setSegmentDurationSeconds(int value) {
    }
    
    public final int getRetentionPeriodMinutes() {
        return 0;
    }
    
    public final void setRetentionPeriodMinutes(int value) {
    }
    
    public final int getAudioBitrate() {
        return 0;
    }
    
    public final void setAudioBitrate(int value) {
    }
    
    public final int getAudioSampleRate() {
        return 0;
    }
    
    public final void setAudioSampleRate(int value) {
    }
    
    public final int getMaxStorageMB() {
        return 0;
    }
    
    public final void setMaxStorageMB(int value) {
    }
    
    public final boolean getShowNotification() {
        return false;
    }
    
    public final void setShowNotification(boolean value) {
    }
    
    public final boolean getAutoStartOnBoot() {
        return false;
    }
    
    public final void setAutoStartOnBoot(boolean value) {
    }
    
    public final long getLastServiceStartTime() {
        return 0L;
    }
    
    public final void setLastServiceStartTime(long value) {
    }
    
    /**
     * Calculate total storage usage for current settings
     */
    public final long calculateStorageUsage() {
        return 0L;
    }
    
    /**
     * Get formatted storage usage string
     */
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFormattedStorageUsage() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\n\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\nX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\nX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\nX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\nX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\nX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\nX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\nX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\nX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\nX\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/listen/app/settings/SettingsManager$Companion;", "", "()V", "DEFAULT_AUDIO_BITRATE", "", "DEFAULT_AUDIO_SAMPLE_RATE", "DEFAULT_MAX_STORAGE", "DEFAULT_RETENTION_PERIOD", "DEFAULT_SEGMENT_DURATION", "KEY_AUDIO_BITRATE", "", "KEY_AUDIO_SAMPLE_RATE", "KEY_AUTO_START_BOOT", "KEY_LAST_SERVICE_START", "KEY_MAX_STORAGE", "KEY_RETENTION_PERIOD", "KEY_SEGMENT_DURATION", "KEY_SERVICE_ENABLED", "KEY_SHOW_NOTIFICATION", "PREFS_NAME", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}