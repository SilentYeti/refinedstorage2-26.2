// MC 26.2 port: plain Fabric Loom, replacing refinedarchitect's `.fabric` plugin (hardcoded to MC
// 26.1 -- see settings.gradle.kts / PORTING.md). MC ships unobfuscated from 26.x on, so Loom resolves
// intermediary 0.0.0: no `mappings` line, and dependencies use plain `implementation`, not
// `modImplementation` (that configuration no longer exists).
//
// See refinedstorage-neoforge/build.gradle.kts for why core-api/resource-api/.../common-api/common
// are compiled against here and physically bundled into this jar rather than depended on normally.
plugins {
    id("net.fabricmc.fabric-loom")
}

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "ModMenu"
        url = uri("https://maven.terraformersmc.com/")
    }
    maven {
        name = "Cloth Config"
        url = uri("https://maven.shedaniel.me/")
    }
    mavenCentral()
}

base {
    archivesName.set("refinedstorage-fabric")
}

val bundledProjectPaths = listOf(
    ":refinedstorage-core-api",
    ":refinedstorage-resource-api",
    ":refinedstorage-storage-api",
    ":refinedstorage-query-parser",
    ":refinedstorage-autocrafting-api",
    ":refinedstorage-network-api",
    ":refinedstorage-network",
    ":refinedstorage-common-api",
    ":refinedstorage-common",
    ":refinedstorage-fabric-api",
)
// Forces Gradle to fully configure these projects (registering their `java`-plugin extensions) before
// this script reads their sourceSets below -- under configuration-on-demand, plain `project(path)`
// access does not do this on its own and returns a not-yet-configured project.
bundledProjectPaths.forEach { evaluationDependsOn(it) }
val bundledMainSourceSets = bundledProjectPaths.map { path ->
    project(path).extensions.getByType<SourceSetContainer>().named("main")
}
val commonProject = project(":refinedstorage-common")

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    api("me.shedaniel.cloth:cloth-config-fabric:${property("cloth_config_version")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    api("teamreborn:energy:${property("teamreborn_energy_version")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    api("com.terraformersmc:modmenu:${property("modmenu_version")}")
    include("me.shedaniel.cloth:cloth-config-fabric:${property("cloth_config_version")}")
    include("teamreborn:energy:${property("teamreborn_energy_version")}")

    testCompileOnly(libs.apiguardian)

    bundledProjectPaths.forEach { path -> compileOnly(project(path)) }
}

sourceSets {
    main {
        resources.srcDir(commonProject.file("src/main/resources"))
        resources.srcDir(commonProject.file("src/generated/resources"))
    }
}

tasks.named("compileJava") { dependsOn(bundledProjectPaths.map { "$it:classes" }) }

// Dev-only: groups the bundled projects' output directories with this mod's own so Fabric Loader
// treats them as one mod (otherwise it registers refinedstorage's items/blocks and then finds no
// resource/data pack behind them -- the assets and datagen output live in :refinedstorage-common).
val devModPaths = files(
    sourceSets.main.get().output.classesDirs,
    sourceSets.main.get().output.resourcesDir,
).plus(files(bundledMainSourceSets.map { it.get().output.classesDirs }))
    .files.joinToString(java.io.File.pathSeparator) { it.absolutePath }

loom {
    runs {
        configureEach {
            property("fabric.classPathGroups", devModPaths)
            runDir("run")
        }
        named("client") {
            client()
        }
        named("server") {
            server()
        }
    }
}

val fabricModMetadataProps = mapOf(
    "version" to project.version,
    "minecraft_version" to property("minecraft_version"),
    "fabric_loader_version" to property("fabric_loader_version"),
)
tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    filesMatching("assets/refinedstorage/blockstates/*.json") {
        filter { line -> line.replace("\"type\"", "\"fabric:type\"") }
    }
    inputs.properties(fabricModMetadataProps)
    filesMatching("fabric.mod.json") {
        expand(fabricModMetadataProps)
    }
    // Only ever produced by the NeoForge datagen run.
    exclude(".cache/**")
}

tasks.named<Jar>("jar") {
    bundledMainSourceSets.forEach { from(it.get().output.classesDirs) }
    exclude(".cache/**")
}
