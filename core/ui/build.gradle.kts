plugins {
    id("arrows.android.library.compose")
}

android {
    namespace = "com.batodev.arrows.core.ui"
}

dependencies {
    implementation("androidx.compose.material:material-icons-extended")
    // lifecycle-viewmodel-compose intentionally not on the shared catalog's
    // value (2.11.0) - this repo is behind at 2.10.0.
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation(project(":core:resources"))
    implementation(project(":core:models"))
}
