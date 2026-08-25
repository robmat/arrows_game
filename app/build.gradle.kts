import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.compose")
    id("jacoco")
    id("com.batodev.releasetools")
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
                "proguard-rules.pro",
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

tasks.withType<Test> {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

jacoco {
    toolVersion = "0.8.13"
}

tasks.register<JacocoReport>("testDebugUnitTestCoverage") {
    dependsOn("testDebugUnitTest")
    group = "Reporting"
    description = "Generate Jacoco coverage reports for the debug build."

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val excludes =
        listOf(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*",
            "**/data/models/*",
        )

    val javaClasses =
        fileTree(
            "${project.layout.buildDirectory.get().asFile}/intermediates/javac/debug/compileDebugJavaWithJavac/classes",
        ) {
            exclude(excludes)
        }
    val kotlinClasses =
        fileTree(
            "${project.layout.buildDirectory.get().asFile}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes",
        ) {
            exclude(excludes)
        }

    classDirectories.setFrom(files(javaClasses, kotlinClasses))

    sourceDirectories.setFrom(
        files(
            "$projectDir/src/main/java",
            "$projectDir/src/main/kotlin",
        ),
    )

    executionData.setFrom(
        fileTree("${project.layout.buildDirectory.get().asFile}/outputs/unit_test_code_coverage/debugUnitTest") {
            include("*.exec")
        },
    )
}

configurations.all {
    resolutionStrategy {
        force(libs.androidx.annotation.experimental)
    }
}

dependencies {
    // Behind the shared catalog's versions - still sourced from it, strictly
    // pinned to this repo's own values rather than bumped as a side effect.
    implementation(libs.androidx.core.ktx) { version { strictly("1.17.0") } }
    implementation(libs.androidx.lifecycle.runtime.ktx) { version { strictly("2.10.0") } }
    implementation(libs.androidx.activity.compose) { version { strictly("1.12.4") } }
    // Dedicated alias (not the generic composeBom + strictly()) - strictly()
    // doesn't resolve wrapped in platform(), confirmed by a real compile
    // failure; a plain catalog reference wrapped in platform() works fine.
    implementation(platform(libs.arrows.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation(libs.bumble.appyx.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(project(":navigation"))
    implementation(project(":feature:home"))
    implementation(project(":core:ui"))
    implementation(project(":data"))
    implementation(project(":ads"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.arrows.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    // GameLevel/Snake/Direction (core:models), SolvabilityChecker (domain) and
    // GAME_AREA_TEST_TAG (feature:game) aren't otherwise guaranteed to be on the
    // androidTest compile classpath - the app module only depends on them transitively
    // through implementation() edges in other modules (e.g. :navigation -> :feature:game).
    androidTestImplementation(project(":core:models"))
    androidTestImplementation(project(":domain"))
    androidTestImplementation(project(":feature:game"))
    debugImplementation(libs.bumble.appyx.testing.ui.activity)
    androidTestImplementation(libs.bumble.appyx.testing.ui)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
