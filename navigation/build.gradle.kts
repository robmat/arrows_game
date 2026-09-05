plugins {
    id("arrows.android.library.compose")
    id("kotlin-parcelize")
}

android {
    namespace = "com.batodev.arrows.navigation"
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.koin.android)
    implementation(libs.bumble.appyx.core)
    implementation(project(":feature:home"))
    implementation(project(":feature:game"))
    implementation(project(":feature:generate"))
    implementation(project(":feature:settings"))
    implementation(project(":ads"))
    testImplementation(libs.junit)
    // Behind the shared catalog's value (1.11.0) - still sourced from it,
    // strictly pinned to this repo's own value.
    testImplementation(libs.kotlinx.coroutines.test) { version { strictly("1.10.2") } }
    // Behind the shared catalog's value (5.23.0) - still sourced from it,
    // strictly pinned to this repo's own value.
    testImplementation(libs.mockito.core) { version { strictly("5.22.0") } }
    testImplementation(libs.mockito.kotlin)
    // kotlin-reflect pinned to match this repo's own Kotlin version (2.4.10),
    // not the shared alias.
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:2.4.10")
    testImplementation(libs.bumble.appyx.testing.unit.common)
    testImplementation(libs.bumble.appyx.testing.junit5)
}
