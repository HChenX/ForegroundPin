# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.hchen.foregroundpin.HookMain
-keep class com.hchen.foregroundpin.hook.ForegroundPin
-keep class com.hchen.foregroundpin.hook.SystemUiHangup

-keep class com.kongzue.dialogx.** { *; }
-dontwarn com.kongzue.dialogx.**
# 额外的，建议将 android.view 也列入 keep 范围：
-keep class android.view.** { *; }
# 若启用模糊效果，请增加如下配置：
-dontwarn androidx.renderscript.**
-keep public class androidx.renderscript.** { *; }