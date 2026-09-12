pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()

        // Modstitch
        maven("https://maven.isxander.dev/releases/")

        // Loom platform
        maven("https://maven.fabricmc.net/")

        // MDG platform
        maven("https://maven.neoforged.net/releases/")

        // Stonecutter
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")

        // Modstitch
        maven("https://maven.isxander.dev/releases")
    }
}

plugins {
    id("com.android.application") version "9.1.0" apply false
    id("dev.kikugie.stonecutter") version "0.9+"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        /**
         * @param mcVersion The base minecraft version.
         * @param loaders A list of loaders to target, supports "fabric" (1.14+), "neoforge"(1.20.6+), "vanilla"(any) or "forge"(<=1.20.1)
         */
        fun mc(mcVersion: String, name: String = mcVersion, loaders: Iterable<String>) =
            loaders.forEach { version("$name-$it", mcVersion) }

        // Configure your targets here!
        mc("0.3.1-alpha14", loaders = listOf("fabric", "neoforge"))
        mc("0.2.1-beta15", loaders = listOf("fabric", "forge", "neoforge"))

        // This is the default target.
        // https://stonecutter.kikugie.dev/stonecutter/guide/setup#settings-settings-gradle-kts
        vcsVersion = "0.3.1-alpha14-fabric"
    }
}

rootProject.name = "DirectTouch"
include(":android")