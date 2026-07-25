plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("jacoco")
}

android {
    namespace = "com.batodev.arrows"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.batodev.arrows"
        minSdk = 29
        targetSdk = 36
        versionCode = 9
        versionName = "1.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["admobAppId"] = "ca-app-pub-9667420067790140~5728073317"
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

tasks.withType<Test> {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

jacoco {
    toolVersion = libs.versions.jacocoVersion.get()
}

tasks.register<JacocoReport>("testDebugUnitTestCoverage") {
    dependsOn("testDebugUnitTest")
    group = "Reporting"
    description = "Generate Jacoco coverage reports for the debug build."

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val excludes = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/data/models/*"
    )

    val javaClasses = fileTree("${project.layout.buildDirectory.get().asFile}/intermediates/javac/debug/compileDebugJavaWithJavac/classes") {
        exclude(excludes)
    }
    val kotlinClasses = fileTree("${project.layout.buildDirectory.get().asFile}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") {
        exclude(excludes)
    }

    classDirectories.setFrom(files(javaClasses, kotlinClasses))

    sourceDirectories.setFrom(files(
        "$projectDir/src/main/java",
        "$projectDir/src/main/kotlin"
    ))

    executionData.setFrom(fileTree("${project.layout.buildDirectory.get().asFile}/outputs/unit_test_code_coverage/debugUnitTest") {
        include("*.exec")
    })
}

configurations.all {
    resolutionStrategy {
        force(libs.androidx.annotation.experimental)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.appyx.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(project(":navigation"))
    implementation(project(":feature:home"))
    implementation(project(":core:ui"))
    implementation(project(":data"))
    implementation(project(":ads"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    // GameLevel/Snake/Direction (core:models), SolvabilityChecker (domain) and
    // GAME_AREA_TEST_TAG (feature:game) aren't otherwise guaranteed to be on the
    // androidTest compile classpath - the app module only depends on them transitively
    // through implementation() edges in other modules (e.g. :navigation -> :feature:game).
    androidTestImplementation(project(":core:models"))
    androidTestImplementation(project(":domain"))
    androidTestImplementation(project(":feature:game"))
    debugImplementation(libs.appyx.testing.ui.activity)
    androidTestImplementation(libs.appyx.testing.ui)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
