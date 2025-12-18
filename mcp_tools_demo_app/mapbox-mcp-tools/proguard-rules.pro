# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep all public API classes and methods
-keep public class com.mapbox.mcp.MapboxMapTools {
    public *;
}

-keep public class com.mapbox.mcp.models.** {
    public *;
}

# Keep serialization classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Mapbox SDK classes
-keep class com.mapbox.** { *; }
-dontwarn com.mapbox.**
