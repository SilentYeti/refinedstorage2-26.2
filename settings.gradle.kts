import org.gradle.api.initialization.resolve.RepositoriesMode

// MC 26.2 port: the upstream `com.refinedmods.refinedarchitect` build plugin (open source, MIT,
// https://github.com/refinedmods/refinedarchitect, hosted below) hardcodes Minecraft 26.1 for its
// `.common`/`.fabric`/`.neoforge` extensions (confirmed by
// decompiling refinedarchitect-plugin-1.7.1.jar -- `com.mojang:minecraft:26.1.2` and NeoForge
// `26.1.2.78` are baked into FabricExtension/NeoForgeExtension bytecode, and 1.7.1 is the latest
// published version). Those three modding plugins are bypassed below in favor of plain Fabric Loom /
// NeoForge ModDevGradle targeting 26.2. `.root` and `.base` are untouched -- they're MC-version-agnostic
// (project grouping, Sonar, javadoc/testing/publishing conventions) and still used by the eight pure-Java
// modules (core-api, resource-api, storage-api, query-parser, autocrafting-api, network-api, network,
// network-test) plus the root project itself. See PORTING.md.
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_PROJECT
    repositories {
        maven {
            name = "Refined Architect"
            url = uri("https://maven.creeperhost.net")
            content {
                includeGroupAndSubgroups("com.refinedmods.refinedarchitect")
            }
        }
        // JEI recipe-transfer/ghost-ingredient integration (optional, see refinedstorage-common's
        // build.gradle.kts). Declared here, not per-project: under PREFER_PROJECT, a project-level
        // `repositories {}` block replaces rather than extends what net.neoforged.moddev.repositories
        // injects for refinedstorage-common, which breaks its NeoForm tooling resolution.
        maven {
            name = "JEI"
            url = uri("https://maven.blamejared.com/")
        }
        // REI recipe-transfer/ghost-ingredient integration, NeoForge side (optional, see
        // refinedstorage-neoforge's build.gradle.kts). Same PREFER_PROJECT reasoning as JEI above --
        // refinedstorage-neoforge has no project-level repositories block of its own to extend, so this
        // has to live here rather than break net.neoforged.moddev.repositories' injected repos.
        maven {
            name = "REI"
            url = uri("https://maven.shedaniel.me/")
        }
        maven {
            name = "Architectury"
            url = uri("https://maven.architectury.dev/")
        }
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            val refinedarchitectVersion: String by settings
            from("com.refinedmods.refinedarchitect:refinedarchitect-versioning:${refinedarchitectVersion}")
        }
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "Refined Architect"
            url = uri("https://maven.creeperhost.net")
            content {
                includeGroupAndSubgroups("com.refinedmods.refinedarchitect")
            }
        }
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
    }
    val refinedarchitectVersion: String by settings
    plugins {
        id("com.refinedmods.refinedarchitect.root").version(refinedarchitectVersion)
        id("com.refinedmods.refinedarchitect.base").version(refinedarchitectVersion)
        id("net.neoforged.moddev").version("2.0.147")
        id("net.neoforged.moddev.repositories").version("2.0.147")
        id("net.fabricmc.fabric-loom").version("1.18.2")
    }
}

plugins {
    // Registers the NeoForged maven repos (neoform/neoforge artifacts) project-wide.
    id("net.neoforged.moddev.repositories")
}

rootProject.name = "refinedstorage"
include("refinedstorage-core-api")
include("refinedstorage-resource-api")
include("refinedstorage-storage-api")
include("refinedstorage-query-parser")
include("refinedstorage-autocrafting-api")
include("refinedstorage-network-api")
include("refinedstorage-network")
include("refinedstorage-common-api")
include("refinedstorage-common")
include("refinedstorage-fabric")
include("refinedstorage-fabric-api")
include("refinedstorage-neoforge")
include("refinedstorage-neoforge-api")
include("refinedstorage-network-test")
