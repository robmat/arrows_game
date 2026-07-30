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
    implementation("io.insert-koin:koin-android:4.0.0")
    implementation("com.bumble.appyx:core:1.7.1")
    implementation(project(":feature:home"))
    implementation(project(":feature:game"))
    implementation(project(":feature:generate"))
    implementation(project(":feature:settings"))
    implementation(project(":ads"))
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("org.mockito:mockito-core:5.22.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.2.3")
    // kotlin-reflect pinned to match this repo's own Kotlin version (2.3.10),
    // not the shared alias.
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:2.3.10")
    testImplementation("com.bumble.appyx:testing-unit-common:1.7.1")
    testImplementation("com.bumble.appyx:testing-junit5:1.7.1")
}
