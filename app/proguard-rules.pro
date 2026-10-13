# Keep JNI Bridge native methods and callback signatures
-keepclassmembers class com.fireemu.nativebridge.LibretroBridge {
    public static <methods>;
}
-keep class com.fireemu.nativebridge.LibretroBridge { *; }

# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Compose and TV components
-keep class androidx.compose.** { *; }
-keep class androidx.tv.** { *; }
