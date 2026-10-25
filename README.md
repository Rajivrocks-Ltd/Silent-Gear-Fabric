# Silent Gear (Fabric port)

**Work in progress**. This branch is the port to Minecraft 26.2 and is not released. For a working version, use branch 26.1.2 or the latest release.

An **unofficial** port of [Silent Gear](https://github.com/SilentChaos512/Silent-Gear) and the parts of
[Silent Lib](https://github.com/SilentChaos512/SilentLib) it needs, from NeoForge to Fabric.

Status: the port of Silent Gear 4.2.3 for Minecraft 26.1.2 is complete (branch `26.1.2`). The move to
Minecraft 26.2 is in progress (branch `26.2`).
This port is not made or supported by SilentChaos512. Please report port bugs here, not upstream.

## Credits

- **Silent Gear** and **Silent Lib** are made by **SilentChaos512** and released under the MIT license.
  All game design, code, art, and data come from the upstream projects.
- Fabric port by Rajiv.
- Uses [mixbox](https://github.com/scrtwpns/mixbox) for color mixing.

## License

MIT, same as upstream. See [LICENSE](LICENSE).

## Building

Needs JDK 25.

```
.\gradlew.bat build
```

Jars end up in `silent-lib-fabric/build/libs` and `silent-gear-fabric/build/libs`.

To start a dev client with both mods loaded:

```
.\gradlew.bat :silent-gear-fabric:runClient
```

## Branches

One branch per Minecraft version, like upstream: `26.1.2` and `26.2`. `main` points to the newest version.
Release tags name the mod version and the Minecraft version, for example `4.2.3+26.1.2`.

## Tests

Automated gametests run on a headless server:

```
.\gradlew.bat :silent-gear-fabric:runGametest
```

## Publishing

Release jars (with sources and javadoc) are published to this repository's GitHub Packages, like upstream:

```
.\gradlew.bat publish
```

This needs a GitHub personal access token with the `write:packages` permission in `~/.gradle/gradle.properties`
(never in this repository):

```
gpr.username=<GitHub user name>
gpr.token=<token>
```

Packages: `net.silentchaos512:silent-lib-26.1.2-fabric` and `net.silentchaos512:silent-gear-26.1.2-fabric`. The
version stays the mod version (for example 4.2.3), so publish each version once.

## Runtime dependencies

- Fabric Loader 0.19.5 or newer
- Fabric API
- Forge Config API Port
- Silent Lib (Fabric), built from this repo
- Optional: Trinkets Updated

## Layout

| Folder | Content |
|---|---|
| `silent-lib-fabric/` | Ported Silent Lib subset |
| `silent-gear-fabric/` | The Silent Gear port |
| `docs/` | Port plan, API inventory, API mapping, porting notes, test checklist |
| `reference/` | Upstream clones for reference, git-ignored |
