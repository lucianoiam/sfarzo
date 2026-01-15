rootProject.name = "Sfarzo"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Path to juce_cmp_ui relative to this settings file
// This will be populated by CMake at configure time via FetchContent
val juceCmpUiPath = file("../build/_deps/juce-cmp-src/juce_cmp_ui")
if (juceCmpUiPath.exists()) {
    includeBuild(juceCmpUiPath)
}

include(":composeApp")
