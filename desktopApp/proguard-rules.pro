# ProGuard rules for the desktop (JVM) target. The Compose Gradle plugin already applies its own
# defaults (Kotlin, coroutines, Skiko, kotlinx.serialization); these are only project additions.

-optimizationpasses 5
-allowaccessmodification

# Readable stack traces; generics and nesting metadata for the reflection-based libraries below
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod

# Room locates the generated implementation via Class.forName("<database>_Impl")
-keep class * extends androidx.room.RoomDatabase { <init>(); }

# JNI: keep native method names and the classes the native side calls back into
-keepclasseswithmembernames,includedescriptorclasses class * { native <methods>; }
-keep class androidx.sqlite.** { *; }

# JNA (tray, dark mode detection, file dialogs) resolves native symbols, structure fields and
# callbacks by reflection on their names
-keep class com.sun.jna.** { *; }
-keep,includedescriptorclasses class * extends com.sun.jna.** { *; }
-dontnote com.sun.jna.**

# composenativetray and nucleus call back into their bridge classes from native code by name
# (e.g. ThemeChangeCallback.onThemeChanged), which the native-method rule above does not cover
-keep,includedescriptorclasses class com.kdroid.composetray.lib.** { *; }
-keep class io.github.kdroidfilter.nucleus.darkmodedetector.** { *; }

# dbus-java (Linux tray) introspects interfaces, annotations and generic signatures at runtime
-keep class org.freedesktop.dbus.** { *; }
-dontnote org.freedesktop.dbus.**
-dontwarn org.slf4j.**
-dontnote org.slf4j.**

# JLayer instantiates its audio device by class name and loads its *.ser tables relative to
# JavaLayerUtils' package
-keep class dev.mccue.jlayer.player.JavaSoundAudioDevice { <init>(); }
-keepnames class dev.mccue.jlayer.decoder.JavaLayerUtils

# Vico's MutableCartesianMeasuringContext calls MeasuringContext.super methods, which is only legal
# while MeasuringContext stays a direct superinterface; the shrinker otherwise drops that entry
-keep,allowobfuscation interface com.patrykandpatrick.vico.compose.common.MeasuringContext

# Skiko is pinned below the version these were compiled against (see build.gradle.kts); neither
# code path is used by the app
-dontwarn androidx.compose.desktop.ui.tooling.preview.runtime.NonInteractivePreviewFacade*
-dontwarn io.github.vinceglb.filekit.dialogs.compose.util.ImageBitmapExt_nonAndroidKt*
