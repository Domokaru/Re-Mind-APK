# Room Database ProGuard Rules
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Kotlin Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Coroutines & WorkManager
-dontwarn kotlinx.coroutines.**
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep Room Entities, DAOs, Converters, and Models
-keep class com.hallauer.remind.data.local.entity.** { *; }
-keep interface com.hallauer.remind.data.local.dao.** { *; }
-keep class com.hallauer.remind.data.local.Converters { *; }
-keep class com.hallauer.remind.data.local.RemindDatabase { *; }

# Keep Serialization DTOs and Serializable classes
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep class com.hallauer.remind.core.util.*Dto { *; }

