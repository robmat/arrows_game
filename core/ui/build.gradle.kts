plugins {
    id("arrows.android.library.compose")
}

android {
    namespace = "com.batodev.arrows.core.ui"
}

dependencies {
    implementation("androidx.compose.material:material-icons-extended")
    // Behind the shared catalog's value (2.11.0) - still sourced from it,
    // strictly pinned to this repo's own value.
    implementation(libs.androidx.lifecycle.viewmodel.compose) { version { strictly("2.10.0") } }
    implementation(project(":core:resources"))
    implementation(project(":core:models"))
}
