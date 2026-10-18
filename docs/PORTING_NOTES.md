# Porting notes

Running log of decisions, pinned upstream commits, and known gaps.

## Pinned upstream sources

Every diff in this port is against these commits. They live in `reference/` (git-ignored, read-only).

| Repo | Branch / tag | Commit | Version |
|---|---|---|---|
| SilentChaos512/Silent-Gear | `26.1.x` (same as `master`) | `cb9926f4a3215a2f6016e4b465d1029ce06251e4` | 4.2.3, MC 26.1.2, NeoForge 26.1.2.95 |
| SilentChaos512/SilentLib | `26.1.x` | `bea6a1f629f3b79da84d63f55f9464f158036760` | 11.2.0 |
| SilentChaos512/silent-utils | `master` | `0dc2b339aeafd6503e280a911813ef291f115957` | not used (see below) |
| FabricMC/fabric | tag `0.155.3+26.1.2` | `1abe2820db1f3304fe06785fee88497bf593aeb2` | API reference |
| NeoForge sources | maven `neoforge-26.1.2.95-sources.jar` | | in `reference/neoforge-src`, used to see what each hook does |

## Versions used

| Item | Version |
|---|---|
| Minecraft | 26.1.2 |
| Java | 25 (Oracle JDK 25.0.4.1 on the dev machine) |
| Gradle | 9.8.1 (Loom 1.19.1 needs at least 9.8.0) |
| Fabric Loom | 1.19.1, plugin id `net.fabricmc.fabric-loom` |
| Fabric Loader | 0.19.5 |
| Fabric API | 0.155.3+26.1.2 |
| Forge Config API Port | 26.1.5 (`fuzs.forgeconfigapiport:forgeconfigapiport-fabric`) |
| Trinkets Updated | 4.0.1+26.1 (`eu.pb4:trinkets`, maven.nucleoid.xyz), stable, not beta |
| mixbox | 2.0.0, jar-in-jar inside Silent Lib |
| JEI Fabric (later) | 29.47.0.110 for 26.1.2 |
| Mod Menu | 18.0.2, dev runtime only |

## Decisions

### 2026-10-18 (setup, M0, M1)

- **Upstream version is 4.2.3, not 4.2.1.** The plan was written for 4.2.1, but the 26.1.x branch
  has moved to 4.2.3 (released 2026-08-26). The plan's intent is "latest upstream", so we port 4.2.3.
  The reference instance should use Silent Gear 4.2.3 and Silent Lib 11.2.0 from Modrinth.
- **Mod ids** stay `silentgear` and `silentlib`. Display names "Silent Gear (Fabric)" and "Silent Lib (Fabric)".
- **Single loader** (Fabric only), two Gradle subprojects: `silent-lib-fabric`, `silent-gear-fabric`.
- **No split source sets.** The Fabric template uses `splitEnvironmentSourceSets()`, but upstream mixes
  client and common code in one source tree. Keeping one source set keeps the package layout identical
  to upstream, so diffs stay readable. Client-only classes are only loaded from `SilentGearClient`.
- **silent-utils is out of scope.** Nothing on the 26.1.x branches imports it.
- **mixbox ships in Silent Lib only.** Upstream shades it into both jars, but only Silent Lib's
  `ColorUtils` uses it.
- **Silent Lib has no config**, so it does not depend on Forge Config API Port.
- **Forge Config API Port keeps `net.neoforged.*` package names** (`ModConfigSpec`, `ModConfig`,
  `ConfigurationScreen`). Those imports are allowed. All other `net.neoforged` imports must go.
- **Trinkets is a soft dependency.** It is on the dev classpath but listed under `suggests`. Any code
  touching it must check `FabricLoader.getInstance().isModLoaded("trinkets")`.
- **Datagen stays out of the build until M10.** We ship upstream's `src/generated/resources` as is.
- **Shims to limit churn** (planned, built in M2/M4): `DeferredRegister`-like helpers, a `Tags` class with
  the same `c:` tag ids as NeoForge's `Tags`, and an `ItemAbility` class with the constants Silent Gear uses.
  These keep most upstream files unchanged except for imports.
- **JAVA_HOME** was set at user level to `C:\Program Files\Java\jdk-25.0.4.1`. The `java` on PATH is still
  Java 8 (Oracle `java8path`), which does not matter for Gradle.

### 2026-10-18 (M0 test)

- M0 passed with `runClient`: both mods load, all four init messages appear, no errors from our mods.
  The `401` and Realms errors in the dev log come from the offline dev account and are expected.
- **Mod Menu added as a dev-only runtime mod** (`localRuntime`, version 18.0.2). Fabric has no built-in
  mod list. Mod Menu is not a dependency of the released jars.
- **Launch model:** milestone tests use `runClient`. The Modrinth App is used for the NeoForge reference
  instance and for the full M11 pass of the built jars. See TEST_CHECKLIST.md "How to launch".

### 2026-10-18 (M2, Silent Lib subset)

- Ported the 27 runtime classes listed in NEOFORGE_API_INVENTORY section 6. 17 are byte-for-byte
  copies of upstream, 10 changed (`SilentLib`, `ClientTicks`, `ServerTicks`, `Greetings`,
  `InitialSpawnItems`, `PlayerUtils`, `TeleportUtils`, `NameUtils`, `ExtendedShapelessRecipe`,
  `DimensionFilterPlacement`), 2 new (`SilentLibClient`, `ShapelessIngredientMatcher`).
  The 8 datagen-only classes wait for M10.
- **Registered objects are plain vanilla fields, not holders.** `SilentLib.LOOT_CONTAINER` is a
  `DataComponentType` and `DIMENSION_FILTER_PLACEMENT` a `PlacementModifierType`, registered with
  `Registry.register` in static initializers. Fabric loads the entrypoint class during mod init, while
  registries are still open. Reason: NeoForge lets `stack.get(holder)` take a holder, vanilla does not.
  Silent Gear has 89 such call sites, so in M4 its data components should follow the same pattern.
  Other registries (items, blocks, ...) are mostly used through `.get()` (about 360 sites), so for those
  M4 will add a small `DeferredRegister` / `DeferredHolder` stand-in with the same method names.
- **Event helpers register in `init()` methods** instead of their constructors. `ServerTicks`, `Greetings`
  and `InitialSpawnItems` are initialized from `SilentLib.onInitialize()`, `ClientTicks` from
  `SilentLibClient`. This keeps Fabric's client tick API from being touched on a dedicated server.
- **`ClientTicks.partialTicks`**: NeoForge's `RenderFrameEvent` has no Fabric equivalent. The value is read
  from vanilla's `DeltaTracker`, the same source NeoForge used. No mixin. Silent Gear only reads `ticksInGame`.
- **Player persisted data** (`PlayerUtils.getPersistedDataSubcompound`, used for starter items) is a Fabric
  data attachment `silentlib:persisted_data` (persistent, copied on death). It replaces NeoForge's
  `getPersistentData()` + `PlayerPersisted` compound. Data saved by the NeoForge version is not migrated.
- **Shapeless matching with custom ingredients** uses a new `ShapelessIngredientMatcher` (own code,
  bipartite matching) instead of NeoForge's `RecipeMatcher`, which is LGPL. `Ingredient.isSimple()`
  became `!Ingredient.requiresTesting()` (Fabric).
- `TeleportUtils` gets the server from the entity's level instead of `ServerLifecycleHooks`.
- `NameUtils.fromFluid(FluidStack)` now takes Fabric's `FluidVariant`. Silent Gear does not call it.
- **jsr305 annotations** (`javax.annotation.Nullable` etc.) added as `compileOnly` to both subprojects.
  NeoForge pulls them in transitively.
- **Silent Lib access transformer not translated.** Its 3 entries widen fields on vanilla `ShapedRecipe` /
  `ShapelessRecipe`, but neither Silent Lib recipe class extends those any more. Add a class tweaker only
  if M7 needs it.
- **`render_type` in block model JSON** is a NeoForge key. Vanilla 26.1 picks the chunk layer from texture
  transparency (`ChunkSectionLayer.byTransparency`), so the key is redundant. Files left unchanged.
  Check cutout blocks (crops, flowers) visually in M4.
- `pack.mcmeta` not copied: Fabric generates mod resource pack metadata.

## Findings that change the plan

- **Gear rendering is data-driven in 26.1.** Upstream has 303 item model definition files plus 3 custom
  item tint sources (`material`, `gear_part`, `paint_color`) and one special renderer for the trident.
  There is no custom baked model code. Fabric API opens `ItemTintSources.ID_MAPPER` and
  `SpecialModelRenderers.ID_MAPPER`, so M9 is much lower risk than the plan assumed.
- **Capabilities are not used.** All block entity item handler registrations are commented out upstream.
- **Villager trades are not used.** `SgVillages` trade code is commented out upstream.
- **No configuration-phase networking.** Only 10 play-phase payloads.
- **488 `neoforge:ingredient_type` keys** in 218 JSON files. Fabric's custom ingredient key is `fabric:type`.
  This is a mechanical rename in M3.
- **`GetEnchantmentLevelEvent` needs a mixin.** Fabric's `EnchantmentEvents` does not cover level lookups.

## Open questions

- **`neoforge:swim_speed` attribute** (used by the `swift_swim` trait). NeoForge adds this attribute,
  Fabric and vanilla do not. Options: (a) register a `silentgear:swim_speed` attribute and apply it with a
  small mixin, (b) map it to vanilla `minecraft:water_movement_efficiency`, which behaves differently.
  Recommendation: (a), decide in M8.
- **`neoforge:needs_netherite_tool` block tag.** Kept under the same id through the `Tags` shim. Check in M8
  whether vanilla 26.1 tool tiers already cover this.
- **Curios slot data** in `data/silentgear/curios` needs a Trinkets equivalent in M8.
- **Silent Lib distribution.** Ship as a separate jar (like upstream) or nest it in the Silent Gear jar.
  Default: separate jar, decide at release.

## Mixins added

None yet.

## Known gaps

None yet.
