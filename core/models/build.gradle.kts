plugins {
    id("arrows.kotlin.jvm")
}

dependencies {
    // Behind the shared catalog's value (1.11.0) - still sourced from it,
    // strictly pinned to this repo's own value.
    api(libs.kotlinx.coroutines.core) { version { strictly("1.10.2") } }
    testImplementation(libs.junit)
}
