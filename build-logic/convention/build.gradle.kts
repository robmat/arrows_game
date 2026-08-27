plugins {
    `kotlin-dsl`
}

group = "com.batodev.arrows.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.plugins.android.application.toDep())
    compileOnly(libs.plugins.android.library.toDep())
    // Kotlin intentionally not on the shared alias - this repo is ahead of the
    // fleet-wide 2.2.10 at 2.3.10; folding it in would be a real, untested-here
    // Kotlin change, not a mechanical catalog migration. KSP isn't in the shared
    // catalog at all (only a handful of repos use it, each pinned to a KSP
    // release matching its own Kotlin version).
    compileOnly("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.3.10")
    compileOnly("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:2.3.10")
    compileOnly("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.3.6")
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

gradlePlugin {
    plugins {
        register("arrowsAndroidLibrary") {
            id = "arrows.android.library"
            implementationClass = "SkeinAndroidLibraryPlugin"
        }
        register("arrowsAndroidLibraryCompose") {
            id = "arrows.android.library.compose"
            implementationClass = "SkeinAndroidLibraryComposePlugin"
        }
        register("arrowsAndroidFeature") {
            id = "arrows.android.feature"
            implementationClass = "SkeinAndroidFeaturePlugin"
        }
        register("arrowsKotlinJvm") {
            id = "arrows.kotlin.jvm"
            implementationClass = "SkeinKotlinJvmPlugin"
        }
    }
}
