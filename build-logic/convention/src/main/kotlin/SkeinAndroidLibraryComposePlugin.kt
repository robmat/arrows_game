import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class SkeinAndroidLibraryComposePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("arrows.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                // Compose BOM intentionally not on the shared catalog's value
                // (2026.06.01) - this repo is behind at 2026.02.01; bumping would
                // be a real, untested-here change, not a mechanical migration.
                // (Kept as a plain literal here rather than libs.androidx.compose.bom
                // + strictly() - that Kotlin DSL sugar doesn't resolve cleanly
                // inside a compiled Plugin<Project> source file, unlike in a real
                // build.gradle.kts script; confirmed by a real compile failure.)
                add("implementation", platform("androidx.compose:compose-bom:2026.02.01"))
                add("implementation", "androidx.compose.ui:ui")
                add("implementation", "androidx.compose.material3:material3")
            }
        }
    }
}
