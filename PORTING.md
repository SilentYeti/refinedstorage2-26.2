# Porting to Minecraft 26.2

This fork's goal: get RefinedStorage 2 building and running on Minecraft 26.2, for both
Fabric and NeoForge. Live state, not a design doc -- update it as things move.

## The blocker this fork exists to route around

Upstream's own build plugin, `com.refinedmods.refinedarchitect` (closed-source, hosted at
`https://maven.creeperhost.net`), hardcodes Minecraft 26.1 in its `.common`/`.fabric`/`.neoforge`
Gradle extensions. Confirmed by decompiling `refinedarchitect-plugin-1.7.1.jar` (the latest published
version as of 2026-09-23): `FabricExtension.class` has the literal dependency string
`com.mojang:minecraft:26.1.2` baked into bytecode, and `NeoForgeExtension.class` has NeoForge
`26.1.2.78`. `refinedarchitect-versioning-1.7.1.toml` still pins `cloth-config-fabric = "26.1.154"`.
There is no newer published version to bump to -- this isn't a config problem, the plugin itself needs
a release RefinedMods hasn't shipped yet.

**The fix**: bypass those three plugin extensions for the six modules that need Minecraft on their
classpath, and use plain Fabric Loom / NeoForge ModDevGradle instead, targeting 26.2 directly. The
`.root` and `.base` extensions are untouched -- they're MC-version-agnostic (project grouping, Sonar,
javadoc/testing/publishing conventions) and still used by the root project and the eight pure-Java
modules (core-api, resource-api, storage-api, query-parser, autocrafting-api, network-api, network,
network-test), none of which ever import `net.minecraft.*`.

## Module -> toolchain map

| Module | Was | Now |
|---|---|---|
| root, core-api, resource-api, storage-api, query-parser, autocrafting-api, network-api, network, network-test | refinedarchitect `.root`/`.base` | unchanged |
| common-api, common | refinedarchitect `.common` | `net.neoforged.moddev`, NeoForm-only vanilla mode (`neoFormVersion=26.2-2`) -- compiles against Minecraft only, no NeoForge on the classpath |
| fabric-api, fabric | refinedarchitect `.fabric` | plain Fabric Loom 1.18.2 |
| neoforge-api, neoforge | refinedarchitect `.neoforge` | plain NeoForge ModDevGradle 2.0.147, full userdev mode (`version=26.2.0.88`) |

Versions pinned in root `gradle.properties` (checked against Fabric meta / NeoForged maven on
2026-09-23, not guessed): `minecraft_version=26.2`, `neoform_version=26.2-2`,
`neoforge_version=26.2.0.88`, `fabric_loader_version=0.19.5`, `fabric_api_version=0.161.0+26.2`,
`cloth_config_version=26.2.155`, `modmenu_version=20.0.2`, `teamreborn_energy_version=5.0.0`
(MC-version-agnostic API, unchanged from upstream's pin).

`org.gradle.java.home` is pinned to JDK 25 in this project's own `gradle.properties` (the
refinedarchitect plugin requires JVM 25+) -- per project memory `gradle-jdk-per-project`, never add
that pin to `~/.gradle/gradle.properties` globally.

Local precedent for this exact toolchain shape: `~/Codebases/AE2-Refabricated`
(`ae2-262/{common,neoforge,fabric}`). Different mod, same pattern -- NeoForm-mode `:common`,
ModDevGradle `:neoforge`, Loom `:fabric`.

## Two build-file gotchas worth knowing before touching these files again

1. **`sourceSets` is unavailable at script-top-level in a ModDevGradle *full NeoForge* project**
   (`neoForge.enable { version = ... }`), until the plugin has resolved that version -- referencing it
   eagerly throws `Extension of type 'SourceSetContainer' does not exist`. NeoForm-only vanilla mode
   (`:common`, `:common-api`) doesn't have this problem; `id("java")` applied explicitly alongside
   `id("net.neoforged.moddev")` in the `:neoforge`/`:neoforge-api` plugins block is required.
2. **Cross-project `project(path).extensions...` needs `evaluationDependsOn(path)` first.** Under
   `org.gradle.configureondemand=true` (set repo-wide), plain `project(path)` access does not force
   that other project to configure first and can hand back an empty/partial project view (no
   `SourceSetContainer` yet) depending on evaluation order. `:fabric` and `:neoforge` call
   `evaluationDependsOn` on every bundled module path before reading their sourceSets.

## What's bundled into the loader jars, and why

`core-api`, `resource-api`, `storage-api`, `query-parser`, `autocrafting-api`, `network-api`,
`network`, `common-api`, `common`, and the matching `fabric-api`/`neoforge-api` module are never
published as standalone runtime artifacts -- upstream's refinedarchitect `commonJava`/`commonResources`
configurations physically merged their compiled classes into the final `fabric`/`neoforge` jar. The
replacement (`:fabric`/`:neoforge` build.gradle.kts) does the same by hand: `compileOnly` against each
module's project, then `from(sourceSet.output)` in the `jar` task. `:common`'s resources (assets +
`src/generated/resources` datagen output) are pulled into both loader jars' `processResources` the same
way upstream did.

## Status as of 2026-09-23

**Builds clean:** `:refinedstorage-common` (774 files), `:refinedstorage-common-api`,
`:refinedstorage-neoforge` (160 files), `:refinedstorage-neoforge-api`, and all eight untouched
pure-Java modules. `:refinedstorage-fabric` / `:refinedstorage-fabric-api` in progress (see below).

**Not yet done:** a runtime smoke test (client boots to main menu, world loads, mod's blocks/items
are present) on either loader. "Compiles" has been verified; "runs" has not. Do that next, on both
loaders, before calling this port usable.

**Deliberately deferred this pass** (compiles-and-boots is the bar, not full feature parity):
- Publishing/Maven config, Sonar coverage exclusions, mutation testing for the six re-toolchained
  modules (upstream's refinedarchitect convenience helpers for these were dropped along with the
  plugin; not needed for a local/dev build).
- `refinedstorage-network-test` and the unit test suites in `common`/`neoforge` compile but haven't
  been run (`./gradlew test`) -- only `compileJava`/`compileTestJava` were exercised so far.
- Full runtime parity check against the 26.1 upstream build (recipes, tags, loot tables, rendering)
  wasn't attempted -- only what javac's error list surfaced.

### Minecraft 26.1 -> 26.2 API changes found and fixed (all in `common` and `neoforge`, mechanical
once identified -- useful if this needs redoing against a future MC bump)

- `Minecraft.screen` / `Minecraft.setScreen(Screen)` / `Minecraft.getToastManager()` moved onto
  `Minecraft.gui` (a new public final `Gui` field): `.gui.screen()`, `.gui.setScreen(x)`,
  `.gui.toastManager()`.
- `SystemToast.multiline(Minecraft, SystemToastId, Component, Component)` removed; replaced by
  `SystemToast.addOrUpdate(ToastManager, SystemToastId, Component, Component)` (no more manually
  building then adding a `SystemToast` instance).
- `EntityType.ARMOR_STAND` and friends (the static constant holders) moved off `EntityType` itself
  onto a new sibling class `net.minecraft.world.entity.EntityTypes` (plural). `EntityType<T>` the
  generic type is unchanged.
- `I18n.exists(String)` removed. Replacement: `Language.getInstance().has(String)`
  (`net.minecraft.locale.Language`).
- `ChatFormatting.getByName(String)` removed -- the whole enum lost its name/color/id metadata methods,
  down to just the `§`-code and `stripFormatting`/`getByCode`. Case-exact enum names (like this
  codebase's own `SyntaxHighlighterColors`, which already stores `"WHITE"`/`"AQUA"`/etc.) go through
  `ChatFormatting.valueOf(name)` in a try/catch instead.
- `net.minecraft.advancements.criterion.{InventoryChangeTrigger,ItemPredicate}` moved to
  `net.minecraft.advancements.triggers.InventoryChangeTrigger` /
  `net.minecraft.advancements.predicates.ItemPredicate`. Signatures unchanged, pure package move.
- `net.minecraft.data.tags.IntrinsicHolderTagsProvider` removed outright (vanilla added
  `BlockItemTagsProvider`/`BlockItemTagAppender` as its own replacement for the same use case, but
  this codebase's `BlockTagsProvider` didn't need the "intrinsic" auto item-tag mirroring feature, so
  it now extends plain `TagsProvider<Block>` and converts instances to `ResourceKey<T>` by hand via
  `x.builtInRegistryHolder().key()` before calling `tag(...).add(...)`).
- `TagAppender<T>.add(T)` never existed on `TagsProvider` to begin with in 26.2 -- both `add`/`addAll`
  take `ResourceKey<T>`, not raw instances (same `builtInRegistryHolder().key()` conversion).
- New render pipeline API (`com.mojang.blaze3d.pipeline.RenderPipeline`):
  `RenderPipelines.MATRICES_PROJECTION_SNIPPET` was removed (closest replacement:
  `MATRICES_FOG_SNIPPET`, same bind groups plus an unused fog uniform); `.withVertexFormat(VertexFormat,
  VertexFormat.Mode)` split into `.withVertexBinding(int bindingIndex, VertexFormat)` +
  `.withPrimitiveTopology(PrimitiveTopology)` (draw mode moved out of `VertexFormat.Mode`, which no
  longer exists, into a new top-level `com.mojang.blaze3d.PrimitiveTopology` enum);
  `RenderSetup.RenderSetupBuilder.bufferSize(int)` removed with no replacement -- buffer sizing is
  handled internally now, `RenderType.create` only takes `(String, RenderSetup)`.

To re-derive any of the above from scratch: NeoForge publishes a `-sources.jar` for its own classes
(`net.neoforged:neoforge:<version>:sources` in the Gradle cache), and ModDevGradle's NeoForm run
(`createMinecraftArtifacts`) leaves the full decompiled-and-patched vanilla source tree under
`~/.gradle/caches/neoformruntime/intermediate_results/mergeWithSources_*_output.jar` -- unzip it and
grep, rather than guessing at API shapes.
