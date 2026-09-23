// MC 26.2 port: plain NeoForge ModDevGradle, replacing refinedarchitect's `.neoforge` plugin
// (hardcoded to NeoForge 26.1.2.78 -- see settings.gradle.kts / PORTING.md).
plugins {
    id("net.neoforged.moddev")
}

neoForge {
    enable {
        version = property("neoforge_version") as String
    }
}

dependencies {
    api(libs.apiguardian)
    api(project(":refinedstorage-common-api"))
}

base {
    archivesName.set("refinedstorage-neoforge-api")
}
