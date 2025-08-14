# ProGuard/R8 rules for Listen app

# Keep model classes used by Room
-keep class androidx.room.** { *; }
-keep class com.listen.app.data.** { *; }
-dontwarn androidx.room.**

# Keep WorkManager classes if used
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# Keep Kotlin metadata (helps reflection and Room)
-keep class kotlin.Metadata { *; }

# Reduce warnings noise
-dontnote kotlinx.coroutines.**
-dontwarn org.jetbrains.annotations.**