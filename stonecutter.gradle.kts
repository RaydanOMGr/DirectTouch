plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "0.3.1-alpha14-fabric"

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.isxander.dev/releases")
        maven("https://maven.terraformersmc.com/")
        maven("https://thedarkcolour.github.io/KotlinForForge/")
        maven("https://maven.bawnorton.com/releases")
        maven("https://maven.enjarai.dev/mirrors")
    }
}