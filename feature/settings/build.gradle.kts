plugins {
    id("arrows.android.feature")
    id("com.mikepenz.aboutlibraries.plugin.android")
}

android {
    namespace = "com.batodev.arrows.feature.settings"
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        release {
            buildConfigField("Boolean", "DRAW_DEBUG_STUFF", "false")
        }
        debug {
            buildConfigField("Boolean", "DRAW_DEBUG_STUFF", "false")
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

aboutLibraries {
    // Plugin will auto-generate aboutlibraries.json
}

dependencies {
    implementation("io.insert-koin:koin-android:4.0.0")
    implementation("com.mikepenz:aboutlibraries-compose-m3:14.0.0-b02")
    implementation(libs.google.play.review.ktx)
    implementation(project(":feature:home"))
    implementation(project(":core:ui"))
    implementation(project(":ads"))
    implementation(project(":core:resources"))
}
