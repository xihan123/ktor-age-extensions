pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://s01.oss.sonatype.org/content/repositories/releases")
    }

    plugins {
        kotlin("jvm").version(extra["kotlin.version"] as String)
        kotlin("plugin.serialization").version(extra["kotlin.version"] as String)
        id("io.ktor.plugin").version(extra["ktor.version"] as String)
        id("com.github.johnrengelman.shadow").version("7.1.2")
    }
}

rootProject.name = "ktor-age-extensions"