plugins {
    id("arrows.kotlin.jvm")
}

dependencies {
    api(project(":core:models"))
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    // Behind the shared catalog's value (5.23.0) - still sourced from it,
    // strictly pinned to this repo's own value.
    testImplementation(libs.mockito.core) { version { strictly("5.22.0") } }
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.2.3")
}
