// MC 26.2 port: plain Fabric Loom, replacing refinedarchitect's `.fabric` plugin (hardcoded to MC
// 26.1 -- see settings.gradle.kts / PORTING.md). MC ships unobfuscated from 26.x on, so Loom resolves
// intermediary 0.0.0: no `mappings` line, and dependencies use plain `implementation`, not
// `modImplementation` (that configuration no longer exists).
plugins {
    id("net.fabricmc.fabric-loom")
}

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    compileOnly(project(":refinedstorage-common-api"))
    api(libs.apiguardian)
}

base {
    archivesName.set("refinedstorage-fabric-api")
}
