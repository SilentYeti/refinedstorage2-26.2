// MC 26.2 port: plain NeoForge ModDevGradle, replacing refinedarchitect's `.neoforge` plugin
// (hardcoded to NeoForge 26.1.2.78 -- see settings.gradle.kts / PORTING.md).
//
// refinedstorage-core-api, -resource-api, -storage-api, -query-parser, -autocrafting-api,
// -network-api, -network, -common-api and -common are pure API-surface / loader-agnostic modules that
// are never published as standalone runtime artifacts: their classes are compiled against here and
// physically bundled into this jar (see the `jar` task below), exactly as refinedarchitect's
// `commonJava`/`commonResources` configurations used to do.
plugins {
    // Applied explicitly (ModDevGradle applies it too, but only once `neoForge.enable {}` -- the full
    // NeoForge userdev mode used below -- has resolved, which is too late for the plain `sourceSets`
    // DSL accessor used further down; NeoForm-only vanilla mode, used by :common and :common-api,
    // doesn't have this ordering issue).
    id("java")
    id("net.neoforged.moddev")
}


base {
    archivesName.set("refinedstorage-neoforge")
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
    ":refinedstorage-neoforge-api",
)
// Forces Gradle to fully configure these projects (registering their `java`-plugin extensions) before
// this script reads their sourceSets below -- under configuration-on-demand, plain `project(path)`
// access does not do this on its own and returns a not-yet-configured project.
bundledProjectPaths.forEach { evaluationDependsOn(it) }
val bundledMainSourceSets = bundledProjectPaths.map { path ->
    project(path).extensions.getByType<SourceSetContainer>().named("main")
}

dependencies {
    testCompileOnly(libs.apiguardian)
    testImplementation(project(":refinedstorage-common"))

    bundledProjectPaths.forEach { path -> compileOnly(project(path)) }

    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito)
    testRuntimeOnly(libs.slf4j.impl)
}

neoForge {
    enable {
        version = property("neoforge_version") as String
    }
    validateAccessTransformers = true
    mods {
        create("refinedstorage") {
            sourceSet(sourceSets.main.get())
            bundledMainSourceSets.forEach { sourceSet(it.get()) }
        }
    }
    runs {
        configureEach {
            gameDirectory = project.file("run")
        }
        create("client") {
            client()
        }
        create("server") {
            server()
        }
        create("data") {
            data()
            programArguments.addAll(
                "--mod", "refinedstorage",
                "--all",
                "--output", rootProject.file("refinedstorage-common/src/generated/resources").absolutePath,
                "--existing", rootProject.file("refinedstorage-common/src/main/resources").absolutePath,
                "--existing", rootProject.file("refinedstorage-neoforge/src/main/resources").absolutePath,
            )
        }
    }
}

val modMetadataProps = mapOf(
    "version" to project.version,
    "neoforge_version_range" to property("neoforge_version_range"),
)
val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    group = "build"
    from("src/main/templates/META-INF/neoforge.mods.toml") {
        into("META-INF")
    }
    into(layout.buildDirectory.dir("generated/modMetadata"))
    inputs.properties(modMetadataProps)
    expand(modMetadataProps)
}
sourceSets.main.get().resources.srcDir(generateModMetadata)

tasks.named<Jar>("jar") {
    bundledMainSourceSets.forEach { from(it.get().output) }
    exclude(".cache/**")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
