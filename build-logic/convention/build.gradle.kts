plugins {
    `kotlin-dsl`
}

group = "com.batodev.arrows.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    compileOnly(libs.plugins.android.application.toDep())
    compileOnly(libs.plugins.android.library.toDep())
    compileOnly(libs.plugins.kotlin.jvm.toDep())
    compileOnly(libs.plugins.kotlin.compose.toDep())
    compileOnly(libs.plugins.ksp.toDep())
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

gradlePlugin {
    plugins {
        register("arrowsAndroidLibrary") {
            id = "arrows.android.library"
            implementationClass = "ArrowsAndroidLibraryPlugin"
        }
        register("arrowsAndroidLibraryCompose") {
            id = "arrows.android.library.compose"
            implementationClass = "ArrowsAndroidLibraryComposePlugin"
        }
        register("arrowsAndroidFeature") {
            id = "arrows.android.feature"
            implementationClass = "ArrowsAndroidFeaturePlugin"
        }
        register("arrowsKotlinJvm") {
            id = "arrows.kotlin.jvm"
            implementationClass = "ArrowsKotlinJvmPlugin"
        }
    }
}
