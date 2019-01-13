# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class com.barocredit.barobaro.Activities.MainActivity$AndroidBridge {
   public *;
}

-keepnames class * implements android.webkit.JavascriptInterface
-keepclassmembers class * implements android.webkit.JavascriptInterface {
    public *;
}

-keepattributes InnerClasses
-dontoptimize


# Keep serializable classes and necessary members for serializable classes
# Copied from the ProGuard manual at http://proguard.sourceforge.net.
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Firebase Realtime database
-keepattributes Signature

# Firebase Authentication
-keepattributes *Annotation*

# dont warnning
-dontwarn com.google.common.util.**
-dontwarn org.apache.commons.**
-dontwarn android.net.http.**
-dontwarn com.android.internal.http.**
-dontwarn com.google.api.client.googleapis.**
-dontwarn com.google.common.**
-dontwarn com.nprotect.**
-dontwarn com.softforum.**
-dontwarn org.apache.http.**
-dontwarn com.google.android.gms.**
-dontwarn com.facebook.internal.**
-dontwarn com.google.firebase.**
-dontwarn com.softforum.xecure.**


-keep class com.google.common.util.**
-keep class org.apache.commons.**
-keep class android.net.http.**
-keep class com.android.internal.http.**
-keep class com.google.api.client.googleapis.**
-keep class com.google.common.**
-keep class com.nprotect.**
-keep class com.softforum.**
-keep class org.apache.http.**
-keep class com.google.android.gms.**
-keep class com.facebook.internal.**
-keep class com.google.firebase.**
-keep class com.softforum.xecure.**

#### XecureAppShield
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService


-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

##############################################
# softforum class
#
# WebView의 addJavascriptInterface() 메소드 사용시 등록되는 Object class의 메소드는 유지
# Class.forName과 같이 동적로드를 사용하는 코드에서 로드되는 class는 유지
#
##############################################
-keepnames class com.softforum.xas.** { *; }
-keepnames class com.softforum.xas.XecureAppShield { *; }

-keepclassmembers class com.softforum.xas.XecureAppShield {
	private String soUpdateUrl;
	private String strToken;
	private String nSID;
	public *;
}


-keep class com.softforum.xecure.core.CoreWrapper {*;}
-keepnames class com.softforum.xecure.core.CoreWrapper { *; }
-keepclassmembers class com.softforum.xecure.core.CoreWrapper {
	*;
}

#-keep class com.softforum.xecure.util.XUtil {*;}
#-keepnames class com.softforum.xecure.util.XUtil { *; }
#-keepclassmembers class com.softforum.xecure.util.XUtil {
#	*;
#}
