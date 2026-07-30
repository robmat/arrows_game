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
    // datastore-preferences intentionally not on the shared catalog's value
    // (1.1.7) - this repo is ahead at 1.2.0.
    implementation("androidx.datastore:datastore-preferences:1.2.0")
    // core-ktx intentionally not on the shared catalog's value (1.19.0) -
    // this repo is behind at 1.17.0.
    implementation("androidx.core:core-ktx:1.17.0")
    implementation(project(":domain"))
    implementation(project(":core:resources"))
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}
