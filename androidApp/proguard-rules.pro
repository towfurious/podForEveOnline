# Keep Kotlin metadata for reflection (Koin, serialization)
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# SQLDelight generated code
-keep class com.podforeve.tracker.db.** { *; }

# Koin
-keep class org.koin.** { *; }

# Kotlinx serialization
-keepattributes *Annotation*
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-dontwarn kotlinx.serialization.**
