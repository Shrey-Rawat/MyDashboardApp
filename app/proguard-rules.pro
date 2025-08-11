# Enhanced ProGuard rules for Best Productivity App
# Comprehensive obfuscation and security hardening

# Enable aggressive optimizations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Obfuscation settings
-repackageclasses ''
-allowaccessmodification
-printmapping mapping.txt

# Security: Remove debug information
-keepattributes !SourceFile,!LineNumberTable
-renamesourcefileattribute ''

# Security: Encrypt string constants
-adaptclassstrings

# Remove logging in production builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
    public static int wtf(...);
}

# Remove debug and verbose logging
-assumenosideeffects class kotlin.io.ConsoleKt {
    public static void println(...);
}

# Remove printStackTrace calls
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
}

# Framework-specific keeps with minimal exposure

# Hilt - Keep only essential classes
-keep class dagger.hilt.InstallIn
-keep class dagger.hilt.android.** { *; }
-keep class javax.inject.Inject
-keep class javax.inject.Singleton
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}

# Room - Keep essential database classes only
-keep class androidx.room.RoomDatabase
-keep class androidx.room.Room
-keepclassmembers,allowobfuscation class * extends androidx.room.RoomDatabase {
    public abstract *;
}
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# SQLCipher - Essential for encrypted database
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# Android Keystore - Essential for security
-keep class android.security.keystore.** { *; }
-keep class androidx.security.crypto.** { *; }
-keepclassmembers class com.mydashboardapp.core.security.** { *; }

# Retrofit and OkHttp - Minimal keeps
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Kotlinx Serialization - Minimal keeps
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mydashboardapp.**$$serializer { *; }
-keepclassmembers class com.mydashboardapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.mydashboardapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class com.mydashboardapp.** {
    @kotlinx.serialization.SerialName <fields>;
}

# Compose - Essential keeps only
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class androidx.compose.** {
    *** Companion;
}

# Security: Protect our security-related classes from obfuscation
# but keep them minimal
-keep class com.mydashboardapp.core.security.KeystoreManager {
    public *;
}
-keep class com.mydashboardapp.ai.data.security.SecureStorage {
    public *;
}

# Keep model classes used for serialization but allow field obfuscation
-keepclassmembers,allowobfuscation class com.mydashboardapp.**.*Model* {
    <fields>;
}
-keepclassmembers,allowobfuscation class com.mydashboardapp.**.*Entity* {
    <fields>;
}
-keepclassmembers,allowobfuscation class com.mydashboardapp.**.*Response* {
    <fields>;
}
-keepclassmembers,allowobfuscation class com.mydashboardapp.**.*Request* {
    <fields>;
}

# Remove reflection usage to keep size down
-assumenosideeffects class java.lang.Class {
    java.lang.reflect.Method[] getDeclaredMethods();
    java.lang.reflect.Method[] getMethods();
    java.lang.reflect.Field[] getDeclaredFields();
    java.lang.reflect.Field[] getFields();
}

# Additional security measures
# Remove potential attack vectors
-assumenosideeffects class java.lang.System {
    public static void setProperty(...);
    public static java.lang.String getProperty(...);
}

# Prevent class name enumeration
-adaptclassstrings
-adaptresourcefilecontents **.properties,**.xml,**.json

# Extra security for sensitive operations
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Note: Resource shrinking is handled by Android's resource shrinker
# when 'isShrinkResources = true' is set in build.gradle.kts
