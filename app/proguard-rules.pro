# Relinx ProGuard Configuration

# Keep application class
-keep class com.takitareq.linkbox.LinkBoxApplication { *; }

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep data models
-keep class com.takitareq.linkbox.data.** { *; }

# AdMob rules
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Keep Gson/JSON serialization classes
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# Keep Material Components
-keep class com.google.android.material.** { *; }

# Keep ViewBinding classes
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** inflate(...);
    public static *** bind(***);
}

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# General Android rules
-keepattributes SourceFile,LineNumberTable
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Remove debug logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep crash reporting info
-keepattributes SourceFile,LineNumberTable
