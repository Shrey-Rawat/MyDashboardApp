pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MyDashboardApp"

// Buildlogic for convention plugins
includeBuild("build-logic")

// Main modules
include(":app")
include(":core")
include(":data")
include(":sync")
include(":auth")
include(":export")
include(":billing")

// Feature modules
include(":feature-nutrition")
include(":feature-training")
include(":feature-productivity")
include(":feature-finance")
include(":feature-inventory")
include(":feature-ai")
