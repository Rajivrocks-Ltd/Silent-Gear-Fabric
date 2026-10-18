# Silent Gear (Fabric port)

An **unofficial** port of [Silent Gear](https://github.com/SilentChaos512/Silent-Gear) and the parts of
[Silent Lib](https://github.com/SilentChaos512/SilentLib) it needs, from NeoForge to Fabric.

Status: work in progress. Phase 1 targets Minecraft 26.1.2. Phase 2 will move to 26.2.
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
| `docs/` | Port plan support files: API inventory, API mapping, porting notes, test checklist |
| `reference/` | Upstream clones for reference, git-ignored |
