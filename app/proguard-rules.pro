# ── KhanaBookLite ProGuard Rules ──────────────────────────────────────────────

# Keep line numbers in stack traces for debuggability (hidden source file name)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Retrofit / Gson (data models must survive obfuscation) ────────────────────
# Keep all fields in remote data classes for Gson deserialization
-keepclassmembers class com.khanabooklite.app.data.remote.** { *; }

# Retain generic type info used by Retrofit/Gson
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit internals
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp / Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# ── jBCrypt ───────────────────────────────────────────────────────────────────
-keep class org.mindrot.jbcrypt.** { *; }

# ── SQLCipher ─────────────────────────────────────────────────────────────────
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }

# ── Hilt / Dagger ────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# ── ZXing (QR code) ──────────────────────────────────────────────────────────
-keep class com.journeyapps.barcodescanner.** { *; }

# ── Facebook SDK ──────────────────────────────────────────────────────────────
-keep class com.facebook.** { *; }
-dontwarn com.facebook.**