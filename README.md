<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
  <img width="280" alt="Refined Storage: Community Edition logo" src="https://raw.githubusercontent.com/refinedmods/refinedstorage2/develop/images/logo.png" />
  <h1 style="margin-top: 0">Refined Storage: Community Edition</h1>
  <p>An unofficial, community-driven fork of <a href="https://github.com/refinedmods/refinedstorage2">Refined Storage 2</a>,
  built and released against current Minecraft versions with a fully open build pipeline.</p>

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE.md)
</div>

## What this is

**Refined Storage: Community Edition** (RS:CE) is a fork of RefinedMods' Refined Storage 2 — the
mass storage mod that gives you a network-based, massively expandable item and fluid storage system.
Items and fluids are stored across storage devices connected to the same network, all accessible
through a single unified Grid interface. Importers, exporters, constructors, destructors, and
autocrafting patterns let you manipulate items and blocks in the world and automate complex recipes
from within the system.

It is **not** an original mod and it is **not affiliated with RefinedMods**. Same MIT license, same
`refinedstorage` mod ID, same data format, same public API modules (`core-api`, `resource-api`,
`storage-api`, `autocrafting-api`, `network-api`, `query-parser`) — this exists to keep that mod
running on the Minecraft version you're actually playing, on a build system anyone can pick up and
patch themselves.

## Why a fork, and why "Community Edition"

Upstream Refined Storage 2 is built with [`refinedarchitect`](https://github.com/refinedmods/refinedarchitect),
RefinedMods' own Gradle plugin. It's open source (MIT, same as this mod), but the Minecraft/NeoForge/
Fabric versions this mod targets are compiled directly into it as constants — there's no config knob
to point it at a different game version. Bumping that means either RefinedMods cutting a new
`refinedarchitect` release, or someone sending them a version-bump PR and waiting on a release cycle.
As of this fork, nobody upstream has done either for the version this fork targets.

RS:CE's loader modules (`refinedstorage-fabric`, `refinedstorage-neoforge`) build with plain, public
Fabric Loom and NeoForge ModDevGradle instead — the same tooling every other mod on the platform
already uses — so:

- **Version support isn't gated on someone else's release.** If Fabric/NeoForge/Minecraft ship a new
  version, anyone with this repo can bump the toolchain themselves and send a PR, same day if needed.
- **Anyone can clone, build, and patch it** with tooling they probably already know, instead of a
  bespoke internal build system built for RefinedMods' own multi-project conventions.
- **The addon ecosystem stays intact.** The API modules that addon developers compile against are
  untouched — same package structure, same public surface. An addon built for upstream RS2 needs a
  recompile against RS:CE's artifacts, not a rewrite.
- **Community contribution is the point, not an afterthought.** "Community Edition" isn't just a
  name — PRs for bugfixes, version bumps, and loader parity are the intended way this stays alive
  between upstream syncs, tracked openly in this repo.

**The honest tradeoff:** the loader modules' `build.gradle.kts` no longer uses the `refinedarchitect`
DSL, so future merges from upstream `develop` will likely need manual reconciliation on those two
files specifically (not the game logic itself, which upstream and this fork share unchanged). That's
a deliberate choice, made to ship a working port now rather than wait — see
[PORTING.md](PORTING.md) for exactly what's diverged and why.

## Relationship to upstream

RS:CE tracks upstream Refined Storage 2's `develop` branch and pulls in upstream feature and bugfix
commits regularly. It is not a hostile fork or a feature-divergent mod — gameplay, balance, and the
API contract are meant to stay identical to upstream. The only intentional, permanent divergence is
the build pipeline itself.

If you don't need a version upstream hasn't shipped yet, or you want first-party support, official
CurseForge/Modrinth pages, and RefinedMods' own roadmap, **use the [original Refined Storage
2](https://github.com/refinedmods/refinedstorage2) instead** — that's the right call for most players.
RS:CE exists for the gap between "the version I'm playing" and "the version upstream's build tooling
currently supports."

## Links

- [GitHub](https://github.com/SilentYeti/refinedstorage2-26.2)
    - [Issues](https://github.com/SilentYeti/refinedstorage2-26.2/issues)
- Upstream project: [refinedmods/refinedstorage2](https://github.com/refinedmods/refinedstorage2)
- Upstream wiki (gameplay is unchanged, still applies): [refinedmods.com/refined-storage](https://refinedmods.com/refined-storage)

## Building

Clone the repository and import the Gradle project. Requires JDK 25+ (pinned per-project in
`gradle.properties`, no global JDK switch needed).

## Porting status

See [PORTING.md](PORTING.md) for the live state of the Minecraft version port — what builds, what's
verified at runtime, and what's still open.

## Contributing

See [CONTRIBUTING.md](.github/CONTRIBUTING.md).

## Changelog

See [CHANGELOG.md](CHANGELOG.md).
