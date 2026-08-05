plugins {
    id("arrows.android.feature")
}

android {
    namespace = "com.batodev.arrows.feature.home"
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.koin.android)
    api(project(":core:ui"))
    api(project(":data"))
    api(project(":ads"))
    api(project(":domain"))
    api(project(":core:resources"))
    testImplementation(project(":core:testing"))
    testImplementation(libs.bumble.appyx.testing.unit.common)
    testImplementation(libs.bumble.appyx.testing.junit5)
}
