pluginManagement {
    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
    }

    plugins {
        val kotlinVersion = extra["kotlin_version"] as String

        kotlin("jvm").version(kotlinVersion)
        id("org.jetbrains.kotlin.plugin.serialization").version(kotlinVersion)
    }
}

rootProject.name = "ktor-age-extensions"

