import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class ArrowsAndroidFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("arrows.android.library.compose")

            val libs = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>()
                .named("libs")

            dependencies {
                add("implementation", "androidx.compose.material:material-icons-extended")
                // lifecycle-viewmodel-compose intentionally not on the shared
                // catalog's value (2.11.0) - this repo is behind at 2.10.0.
                add("implementation", "androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
                add("implementation", "com.bumble.appyx:core:1.7.1")
                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
                // mockito-core intentionally not on the shared catalog's value
                // (5.23.0) - this repo is behind at 5.22.0.
                add("testImplementation", "org.mockito:mockito-core:5.22.0")
                add("testImplementation", "org.mockito.kotlin:mockito-kotlin:6.2.3")
                // kotlin-reflect pinned to match this repo's own Kotlin version
                // (2.3.10), not the shared alias.
                add("testImplementation", "org.jetbrains.kotlin:kotlin-reflect:2.3.10")
            }
        }
    }
}
