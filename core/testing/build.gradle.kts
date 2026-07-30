plugins {
    id("arrows.android.library")
}

android {
    namespace = "com.batodev.arrows.core.testing"
}

dependencies {
    api(libs.junit)
    api("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    api(project(":data"))
    api(project(":core:models"))
}
