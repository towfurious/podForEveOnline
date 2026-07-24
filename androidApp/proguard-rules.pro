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

# Tink (used internally by androidx.security.crypto / EncryptedSharedPreferences) references
# errorprone's compile-time-only annotations (@CanIgnoreReturnValue, @Immutable, etc.) — nothing
# calls into them at runtime, R8 just can't resolve them since they're not on the classpath.
# First surfaced 2026-07-23, the first time this project ever ran a release (R8-minified) build.
-dontwarn com.google.errorprone.annotations.**
