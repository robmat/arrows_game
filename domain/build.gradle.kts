plugins {
    id("arrows.kotlin.jvm")
}

dependencies {
    api(project(":core:models"))
    testImplementation(libs.junit)
    // Behind the shared catalog's value (1.11.0) - still sourced from it,
    // strictly pinned to this repo's own value.
    testImplementation(libs.kotlinx.coroutines.test) { version { strictly("1.10.2") } }
    // Behind the shared catalog's value (5.23.0) - still sourced from it,
    // strictly pinned to this repo's own value.
    testImplementation(libs.mockito.core) { version { strictly("5.22.0") } }
    testImplementation(libs.mockito.kotlin)
}
