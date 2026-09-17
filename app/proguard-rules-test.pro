# Keeps that only the instrumentation harness needs. Applied to the releaseTest build
# type alone, so the release APK users get stays fully shrunk and obfuscated.
#
# The androidTest APK is not minified and carries no Kotlin stdlib or androidx runtime of
# its own - it loads those out of the app APK. R8 legitimately strips whatever the app
# itself never calls, and the instrumentation then dies with ClassNotFoundException
# before a single test runs (seen: androidx.tracing.Trace, kotlin.LazyKt).

-keep class androidx.tracing.** { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class androidx.test.** { *; }
-dontwarn androidx.test.**

# Espresso drives the app's own classes by name - Kotlin `object` singletons especially,
# whose INSTANCE field R8 renames and then optimises away entirely, so every test dies in
# setUp with "NoSuchFieldError: No field INSTANCE of type Lf10;". Keeping app code
# addressable here costs nothing in the shipped APK, and library code is still fully
# minified in this variant - so reflective-access regressions like the WorkManager one
# still surface exactly as they would in release.
-keep class com.batodev.arrows.** { *; }

# More classes the unminified androidTest APK resolves out of the app APK, each found by
# a failing run rather than guessed:
#   javax.inject.Provider        - NoClassDefFoundError before any test body executes
#   j$.**                        - L8 shrinks the desugared library to what the app uses,
#                                  so the test APK's DesugarCollections.synchronizedMap
#                                  reference goes unresolved
#   Activity.onBackPressed()     - tests call it directly; app code never does, so R8
#                                  drops it from the androidx superclass
-keep class javax.inject.** { *; }
-dontwarn javax.inject.**
-keep class j$.** { *; }
-dontwarn j$.**
-keepclassmembers class * extends android.app.Activity {
    public void onBackPressed();
}

# The onBackPressed() the tests drive is OnBackPressedDispatcher's, not the Activity's -
# mapping.txt resolved the "No virtual method onBackPressed()V in class Lj40;" failure to
# androidx.activity.OnBackPressedDispatcher. App code only ever goes through the dispatcher's
# callback path, so R8 drops the entry point itself.
-keepclassmembers class androidx.activity.OnBackPressedDispatcher {
    public void onBackPressed();
}

# androidx.test resolves Guava's ListenableFuture out of the app APK.
-keep class com.google.common.util.concurrent.** { *; }
-dontwarn com.google.common.util.concurrent.**

# AndroidComposeTestRule.<init> resolves InfiniteAnimationPolicy (and friends) out of the
# app APK; nothing in app code references them, so R8 strips them and every Compose test
# fails in the rule's constructor.
-keep class androidx.compose.ui.platform.** { *; }
-dontwarn androidx.compose.ui.platform.**

# TestMonotonicFrameClock reaches MonotonicFrameClock$DefaultImpls, the interface's
# default-implementation bridge, which nothing in app code calls directly.
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.runtime.**

# Keep androidx wholesale for the instrumentation. The negated form that excluded
# androidx.work was silently ineffective in R8 - compose.runtime classes were still being
# renamed - so this keeps all of androidx. The WorkDatabase_Impl keep that fixes the
# crash-on-open lives in the shipped proguard-rules.pro, which is what actually matters.
-keep class androidx.** { *; }
-dontwarn androidx.**

# Test helpers reset app state through Koin, so the instrumentation resolves Koin's
# component interfaces out of the app APK.
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Namespace-derived keep above misses repos whose Kotlin package differs from the Gradle
# namespace (pics_of_pretty_girls: namespace sunset_girls, package sunsetgirls), which
# reintroduced the "No field INSTANCE" failures. These are the catch-all for app code
# across the fleet, including the two vendored upstream packages.
-keep class com.batodev.** { *; }
-keep class name.boyle.chris.** { *; }
-keep class com.dozingcatsoftware.** { *; }

# Below minSdk 24, D8 desugars Kotlin/Java default interface methods into synthetic
# <Interface>$-CC companions holding static $default$ bridges. R8 renames those synthetics,
# but the unminified test APK still calls them by name - e.g. snake's
# "No static method $default$getKey(androidx.compose.runtime.MonotonicFrameClock)".
-keep class **$-CC { *; }
-keepclassmembers class * { public static *** $default$*(...); }

# Hilt entry points are resolved by the instrumentation (EntryPointAccessors), not by app
# code, so R8 strips them and the test dies with
# "NoClassDefFoundError: dagger.hilt.android.EntryPointAccessors".
-keep class dagger.** { *; }
-keep class javax.annotation.** { *; }
-dontwarn dagger.**
