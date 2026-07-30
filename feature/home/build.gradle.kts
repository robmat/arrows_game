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
    implementation("io.insert-koin:koin-android:4.0.0")
    api(project(":core:ui"))
    api(project(":data"))
    api(project(":ads"))
    api(project(":domain"))
    api(project(":core:resources"))
    testImplementation(project(":core:testing"))
    testImplementation("com.bumble.appyx:testing-unit-common:1.7.1")
    testImplementation("com.bumble.appyx:testing-junit5:1.7.1")
}
