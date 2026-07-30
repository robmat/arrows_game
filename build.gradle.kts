// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(sharedLibs.plugins.detekt)
    alias(libs.plugins.aboutlibraries) apply false
    alias(libs.plugins.ksp) apply false
    id("jacoco")
    id("com.github.triplet.play") version "4.0.0" apply false
    id("com.github.ben-manes.versions") version "0.54.0" apply false
    id("se.patrikerdes.use-latest-versions") version "0.2.19" apply false
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("config/detekt/detekt.yml"))
    source.setFrom(
        subprojects.map { "${it.projectDir}/src" }
    )
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            if (project.findProperty("enableComposeCompilerMetrics") == "true") {
                freeCompilerArgs.addAll(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                        layout.buildDirectory.dir("compose-metrics").get().asFile.absolutePath,
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                        layout.buildDirectory.dir("compose-reports").get().asFile.absolutePath,
                )
            }
        }
    }
}
