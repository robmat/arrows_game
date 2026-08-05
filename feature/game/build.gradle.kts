plugins {
    id("arrows.android.feature")
}

android {
    namespace = "com.batodev.arrows.feature.game"
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

dependencies {
    implementation(libs.koin.android)
    implementation(libs.dionsegijn.konfetti.compose)
    implementation(libs.androidx.core.ktx) { version { strictly("1.17.0") } }
    implementation(project(":feature:home"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":core:ui"))
    implementation(project(":ads"))
    implementation(project(":core:resources"))
    testImplementation(project(":core:testing"))
    testImplementation(libs.bumble.appyx.testing.unit.common)
    testImplementation(libs.bumble.appyx.testing.junit5)
}
