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
    implementation(libs.koin.android)
    api(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // Ahead of the shared catalog's value (1.1.7) - still sourced from it,
    // strictly pinned to this repo's own value.
    implementation(libs.androidx.datastore.preferences) { version { strictly("1.2.0") } }
    // Behind the shared catalog's value (1.19.0) - still sourced from it,
    // strictly pinned to this repo's own value.
    implementation(libs.androidx.core.ktx) { version { strictly("1.17.0") } }
    implementation(project(":domain"))
    implementation(project(":core:resources"))
    testImplementation(libs.junit)
    // Behind the shared catalog's value (1.11.0) - still sourced from it,
    // strictly pinned to this repo's own value.
    testImplementation(libs.kotlinx.coroutines.test) { version { strictly("1.10.2") } }
}
