plugins {
    id("arrows.android.library")
}

android {
    namespace = "com.batodev.arrows.core.testing"
}

dependencies {
    api(libs.junit)
    // Behind the shared catalog's value (1.11.0) - still sourced from it,
    // strictly pinned to this repo's own value.
    api(libs.kotlinx.coroutines.test) { version { strictly("1.10.2") } }
    api(project(":data"))
    api(project(":core:models"))
}
