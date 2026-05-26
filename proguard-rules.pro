# HBD PROCAST ProBill - ProGuard Rules

# Keep WebView JavaScript interface intact
-keepclassmembers class com.hbdprocast.billing.MainActivity$AndroidBridge {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface

# Keep MainActivity
-keep class com.hbdprocast.billing.** { *; }

# AndroidX
-keep class androidx.core.** { *; }
-keep class androidx.appcompat.** { *; }
-keep class androidx.webkit.** { *; }

# FileProvider
-keep class androidx.core.content.FileProvider { *; }

# Suppress warnings for unused libraries
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
