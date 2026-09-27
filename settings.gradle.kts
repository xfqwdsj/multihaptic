// The settings file is the entry point of every Gradle build.
// Its primary purpose is to define the subprojects.
// It is also used for some aspects of project-wide configuration, like managing plugins, dependencies, etc.
// https://docs.gradle.org/current/userguide/settings_file_basics.html

pluginManagement {
    // Use the version catalog to manage dependencies and plugins.
    // The version catalog is defined in `gradle/libs.versions.toml`.
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}

include(":samples")

include(":multihaptic-android-dsl")
include(":multihaptic-apple-corehaptics-dsl")
include(":multihaptic-core")
include(":multihaptic-compose")

rootProject.name = "multihaptic"
