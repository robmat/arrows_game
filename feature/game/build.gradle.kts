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
    implementation("io.insert-koin:koin-android:4.0.0")
    implementation("nl.dionsegijn:konfetti-compose:2.0.5")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation(project(":feature:home"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":core:ui"))
    implementation(project(":ads"))
    implementation(project(":core:resources"))
    testImplementation(project(":core:testing"))
    testImplementation("com.bumble.appyx:testing-unit-common:1.7.1")
    testImplementation("com.bumble.appyx:testing-junit5:1.7.1")
}
