// MC 26.2 port: see refinedstorage-common-api/build.gradle.kts.
plugins {
    id("net.neoforged.moddev")
}

neoForge {
    enable {
        neoFormVersion = property("neoform_version") as String
    }
}

base {
    archivesName.set("refinedstorage-common")
}

// Datagen output (src/generated/resources) is written by :refinedstorage-neoforge's `data` run and
// ships in both loaders' jars, same as it always did.
sourceSets.main {
    resources.srcDir("src/generated/resources")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    api(project(":refinedstorage-common-api"))
    api(project(":refinedstorage-core-api"))
    api(project(":refinedstorage-resource-api"))
    api(project(":refinedstorage-storage-api"))
    api(project(":refinedstorage-network-api"))
    api(project(":refinedstorage-network"))
    api(project(":refinedstorage-query-parser"))
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.assertj)
    testImplementation(libs.equalsverifier)
    testRuntimeOnly(libs.junit.engine)

    // Optional: JEI integration (com.refinedmods.refinedstorage.jei.common), inert unless JEI's own
    // loader actually invokes it -- see refinedstorage-fabric/build.gradle.kts for the full note.
    compileOnly("mezz.jei:jei-${property("minecraft_version")}-common-api:${property("jei_version")}")
    compileOnly("mezz.jei:jei-${property("minecraft_version")}-common:${property("jei_version")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "10000"))
}
