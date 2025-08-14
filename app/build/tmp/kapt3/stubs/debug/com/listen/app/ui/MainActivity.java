package com.listen.app.ui;

/**
 * Main activity with dashboard and service controls
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000b\u0018\u0000 \u001f2\u00020\u0001:\u0001\u001fB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0010\u001a\u00020\u0011H\u0002J\b\u0010\u0012\u001a\u00020\u0011H\u0002J\u0012\u0010\u0013\u001a\u00020\u00112\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0014J\b\u0010\u0016\u001a\u00020\u0011H\u0014J\u0006\u0010\u0017\u001a\u00020\u0011J\u0006\u0010\u0018\u001a\u00020\u0011J\b\u0010\u0019\u001a\u00020\u0011H\u0002J\b\u0010\u001a\u001a\u00020\u0011H\u0002J\b\u0010\u001b\u001a\u00020\u0011H\u0002J\u0006\u0010\u001c\u001a\u00020\u0011J\u0006\u0010\u001d\u001a\u00020\u0011J\b\u0010\u001e\u001a\u00020\u0011H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0007\u001a\u0010\u0012\f\u0012\n \n*\u0004\u0018\u00010\t0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u000b\u001a\u0010\u0012\f\u0012\n \n*\u0004\u0018\u00010\t0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Lcom/listen/app/ui/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/listen/app/databinding/ActivityMainBinding;", "database", "Lcom/listen/app/data/ListenDatabase;", "requestNotificationPermissionLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "", "kotlin.jvm.PlatformType", "requestPermissionLauncher", "settings", "Lcom/listen/app/settings/SettingsManager;", "storageManager", "Lcom/listen/app/storage/StorageManager;", "checkAndStartService", "", "checkPermissionsAndService", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onResume", "openPlayback", "openSettings", "requestNotificationPermission", "setupUI", "showPermissionRationale", "startService", "stopService", "updateUI", "Companion", "app_debug"})
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.listen.app.databinding.ActivityMainBinding binding;
    private com.listen.app.settings.SettingsManager settings;
    private com.listen.app.data.ListenDatabase database;
    private com.listen.app.storage.StorageManager storageManager;
    @org.jetbrains.annotations.NotNull
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> requestPermissionLauncher = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> requestNotificationPermissionLauncher = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "MainActivity";
    @org.jetbrains.annotations.NotNull
    public static final com.listen.app.ui.MainActivity.Companion Companion = null;
    
    public MainActivity() {
        super();
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override
    protected void onResume() {
    }
    
    /**
     * Set up the user interface
     */
    private final void setupUI() {
    }
    
    /**
     * Check permissions and service status
     */
    private final void checkPermissionsAndService() {
    }
    
    /**
     * Request notification permission for Android 13+
     */
    private final void requestNotificationPermission() {
    }
    
    /**
     * Show permission rationale dialog
     */
    private final void showPermissionRationale() {
    }
    
    /**
     * Check if service should be started and start it
     */
    private final void checkAndStartService() {
    }
    
    /**
     * Update the UI with current status
     */
    private final void updateUI() {
    }
    
    /**
     * Start the recording service
     */
    public final void startService() {
    }
    
    /**
     * Stop the recording service
     */
    public final void stopService() {
    }
    
    /**
     * Open playback activity
     */
    public final void openPlayback() {
    }
    
    /**
     * Open settings activity
     */
    public final void openSettings() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/listen/app/ui/MainActivity$Companion;", "", "()V", "TAG", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}