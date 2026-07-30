plugins {
    id("arrows.kotlin.jvm")
}

dependencies {
    api(project(":core:models"))
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    // mockito-core intentionally not on the shared catalog's value (5.23.0) -
    // this repo is behind at 5.22.0.
    testImplementation("org.mockito:mockito-core:5.22.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.2.3")
}
