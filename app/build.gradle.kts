import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("jacoco")
    id("com.github.triplet.play")
    id("com.batodev.releasetools")
    id("com.github.ben-manes.versions")
    id("se.patrikerdes.use-latest-versions")
}

val keystorePropertiesFile = rootProject.file("../keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

val versionProps = Properties()
file("version.properties").inputStream().use { versionProps.load(it) }

android {
    namespace = "com.batodev.arrows"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.batodev.arrows"
        minSdk = 29
        targetSdk = 37
        versionCode = versionProps.getProperty("versionCode").toInt()
        versionName = versionProps.getProperty("versionName")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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

play {
    serviceAccountCredentials.set(rootProject.file("../play-console-api-465319-0f9c399097c5.json"))
    track.set("internal")
    defaultToAppBundles.set(true)
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
