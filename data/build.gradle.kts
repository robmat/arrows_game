plugins {
    id("arrows.android.library")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.batodev.arrows.data"
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("io.insert-koin:koin-android:4.0.0")
    api("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    // Ahead of the shared catalog's value (1.1.7) - still sourced from it,
    // strictly pinned to this repo's own value.
    implementation(libs.androidx.datastore.preferences) { version { strictly("1.2.0") } }
    // Behind the shared catalog's value (1.19.0) - still sourced from it,
    // strictly pinned to this repo's own value.
    implementation(libs.androidx.core.ktx) { version { strictly("1.17.0") } }
    implementation(project(":domain"))
    implementation(project(":core:resources"))
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}
