# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.dialog.app.data.model.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
