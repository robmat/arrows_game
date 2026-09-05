// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt)
    // aboutlibraries isn't in the shared catalog (single-repo use).
    id("com.mikepenz.aboutlibraries.plugin.android") version "14.0.0-b02" apply false
    // KSP isn't in the shared catalog (only a handful of repos use it, each
    // pinned to a KSP release matching its own Kotlin version).
    id("com.google.devtools.ksp") version "2.3.6" apply false
    id("jacoco")
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
