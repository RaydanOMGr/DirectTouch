import dev.kikugie.stonecutter.data.ParsedVersion
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

plugins {
    id("dev.isxander.modstitch.base") version "0.8.5"
}

fun prop(name: String, consumer: (prop: String) -> Unit) {
    (findProperty(name) as? String?)
        ?.let(consumer)
}

val minecraft = property("deps.minecraft") as String
val isIdeaSync = System.getProperty("idea.sync.active").toBoolean()

val version = "1.0.0"

// Stonecutter constants for mod loaders.
// See https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants
var constraint: String = name.substringAfterLast("-")
val tcVersion: String = findProperty("deps.touchcontroller") as? String? ?: "${sc.current.version}+$constraint"
stonecutter {
    constants.match(
        constraint,
        "fabric",
        "neoforge",
        "forge"
    )

    constants.put("is_zero_two", tcVersion.startsWith("0.2"))
}

val touchController by configurations.creating
val extractedTouchController = layout.buildDirectory.dir("jij")

val cleanTouchController = tasks.register("cleanTouchController", Delete::class) {
    description = "Deletes all extracted jar in jars"
    group = "touchcontroller"

    delete(extractedTouchController)
}

val extractTouchController = tasks.register("extractTouchController") {
    description = "Extracts touch controller jar in jars"
    group = "touchcontroller"

    val input = touchController

    inputs.files(input)
    outputs.dir(extractedTouchController)

    doLast {
        extractTouchControllerJiJ(input.files, minecraft.replace('.', '-'))
    }
}

modstitch {
    minecraftVersion = minecraft

    // Alternatively use stonecutter.eval if you have a lot of versions to target.
    // https://stonecutter.kikugie.dev/stonecutter/guide/setup#checking-versions

    val parsedVersion = ParsedVersion(minecraft)
    javaVersion = when {
        parsedVersion >= "26.1" -> 25
        parsedVersion >= "1.20.5" -> 21
        parsedVersion >= "1.18" -> 17
        parsedVersion >= "1.17" -> 16
        else -> 8
    }
    println("Java version: ${javaVersion.get()}")

    // If parchment doesnt exist for a version yet you can safely
    // omit the "deps.parchment" property from your versioned gradle.properties
    parchment {
        prop("deps.parchment") { mappingsVersion = it }
    }

    // This metadata is used to fill out the information inside
    // the metadata files found in the templates folder.
    metadata {
        modId = "directtouch"
        modName = "DirectTouch"
        modVersion = "$version+$minecraft-$constraint"
        modGroup = "me.andreasmelone"
        modAuthor = "AndreasMelone"
        modDescription = "A fast, reliable and launcher-independent proxy implementation for the TouchController mod!"
        modLicense = "LGPL-3.0"

        fun <K : Any, V : Any> MapProperty<K, V>.populate(block: MapProperty<K, V>.() -> Unit) {
            block()
        }

        replacementProperties.populate {
            // You can put any other replacement properties/metadata here that
            // modstitch doesn't initially support. Some examples below.
            put("mod_issue_tracker", "https://github.com/RaydanOMGr/DirectTouch/issues")
            put("mod_repo", "https://github.com/RaydanOMGr/DirectTouch")
            put("minecraft_version", minecraft)
            put("java_version", "" + javaVersion.get())
            if(isModDevGradleLegacy) {
                put("forge_version", (property("deps.forge") as String).split("-", limit = 2)[1])
            } else {
                put("forge_version", "0.0")
            }
        }
    }

    // Fabric Loom (Fabric)
    loom {
        // It's not recommended to store the Fabric Loader version in properties.
        // Make sure its up to date.
        fabricLoaderVersion = "0.19.3"

        // Configure loom like normal in this block.
        configureLoom {
            runConfigs.all {
                ideConfigGenerated(true)
            }
        }
    }

    // ModDevGradle (NeoForge, Forge, Forgelike)
    moddevgradle {
        prop("deps.forge") { forgeVersion = it }
        prop("deps.neoform") { neoFormVersion = it }
        prop("deps.neoforge") { neoForgeVersion = it }
        prop("deps.mcp") { mcpVersion = it }

        // Configures client and server runs for MDG, it is not done by default
        defaultRuns()

        // This block configures the `neoforge` extension that MDG exposes by default,
        // you can configure MDG like normal from here
//        configureNeoforge {
//            runs.all {
//                disableIdeRun()
//            }
//        }
    }

    mixin {
        // You do not need to specify mixins in any mods.json/toml file if this is set to
        // true, it will automatically be generated.
        addMixinsToModManifest = true

        configs.register("directtouch")

        // Most of the time you wont ever need loader specific mixins.
        // If you do, simply make the mixin file and add it like so for the respective loader:
        // if (isLoom) configs.register("examplemod-fabric")
        // if (isModDevGradleRegular) configs.register("examplemod-neoforge")
        // if (isModDevGradleLegacy) configs.register("examplemod-forge")
    }
}

tasks.named("compileJava") {
    dependsOn(touchController)
}

tasks.register("moveLibs", Copy::class) {
    group = "build"
    dependsOn(":android:build")

    from(project(":android").layout.buildDirectory.dir("intermediates/stripped_native_libs/release/stripReleaseDebugSymbols/out/lib"))
    into(layout.buildDirectory.dir("resources/main/natives"))
}

tasks.register("moveDex", Copy::class) {
    group = "build"
    dependsOn(":android:build")

    from(project(":android").layout.buildDirectory.dir("intermediates/dex/release/mergeDexRelease"))
    into(layout.buildDirectory.dir("resources/main/dex"))
}

tasks.named("processResources") {
    dependsOn("moveLibs")
    dependsOn("moveDex")
}

tasks.named("jar", Jar::class) {
    archiveVersion = "$version-tc${sc.current.version}"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.4.20")
    touchController(modstitchModImplementation("maven.modrinth:touchcontroller:$tcVersion")!!)

    modstitchModImplementation(extractedTouchController.get().asFileTree)
}

fun extractTouchControllerJiJ(input: Set<File>, version: String) {
    input.forEach { jar ->
        ZipInputStream(FileInputStream(jar)).use { stream ->
            var entry: ZipEntry?

            while (stream.nextEntry.also { entry = it } != null) {
                if(!entry!!.name.endsWith(".jar")) continue
                val fileName = entry.name.substringAfterLast('/')

                fun isExcluded(prefix: String): Boolean {
                    if(fileName.startsWith(prefix)) {
                        if(fileName[prefix.length].isDigit()) {
                            if(!fileName.startsWith("$prefix$version")) return true
                        }
                    }
                    return false
                }

                if(isExcluded("touchcontroller-") || isExcluded("combine-") || isExcluded("combine-neoforge-")) continue

                println("Entry name: ${entry.name}")
                val addedJar = extractedTouchController.get()
                    .file("${jar.name}/${entry.name}")
                    .asFile

                addedJar.parentFile.mkdirs()
                addedJar.outputStream().use { out ->
                    stream.transferTo(out)
                    println("Transfered to $addedJar")
                }
            }
        }
    }
}