dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../../release-tools/gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
