pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("com\\.zebra.*")
                includeGroupByRegex("org\\.jetbrains.*")
            }
        }
        plugins {
            id("com.google.dagger.hilt.android") version "2.48"  // Используйте одну и ту же версию
        }
        mavenCentral()
        gradlePluginPortal()
        jcenter()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        jcenter()
        maven { url = uri("https://zebratech.jfrog.io/artifactory/EMDK-Android/") } // Использование uri()
        maven { url = uri("https://jitpack.io") } // Использование uri()
    }
}

rootProject.name = "sorage_mobile"
include(":scanner_module")
include(":app")
 