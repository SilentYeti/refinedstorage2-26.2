// MC 26.2 port: compiled against vanilla Minecraft only, via ModDevGradle's NeoForm mode, replacing
// refinedarchitect's `.common` plugin (hardcoded to MC 26.1 -- see settings.gradle.kts / PORTING.md).
// Nothing here may import net.neoforged.* or net.fabricmc.*; loader-specific code lives in
// refinedstorage-fabric(-api)/refinedstorage-neoforge(-api).
plugins {
    id("net.neoforged.moddev")
}

neoForge {
    enable {
        neoFormVersion = property("neoform_version") as String
    }
}

base {
    archivesName.set("refinedstorage-common-api")
}

dependencies {
    api(libs.apiguardian)
    api(project(":refinedstorage-core-api"))
    api(project(":refinedstorage-storage-api"))
    api(project(":refinedstorage-resource-api"))
    api(project(":refinedstorage-network-api"))
    api(project(":refinedstorage-autocrafting-api"))
}
