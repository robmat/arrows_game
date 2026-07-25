pluginManagement {
    includeBuild("build-logic")
    includeBuild("../release-tools")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Arrows"
include(":app")
include(":core:models")
include(":core:resources")
include(":core:testing")
include(":core:ui")
include(":domain")
include(":data")
include(":ads")
include(":feature:home")
include(":feature:game")
include(":feature:generate")
include(":feature:settings")
include(":navigation")
