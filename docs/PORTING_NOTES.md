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

### 2026-10-18 (M3, resources)

- Copied upstream `src/main/resources` (assets, data) and `src/generated/resources` into
  `silent-gear-fabric/src/main/resources` and `silent-gear-fabric/src/generated/resources`.
  No path exists in both folders. 3,285 upstream files, 15 left out (below), our `fabric.mod.json` added.
- **Not copied** (NeoForge-only, the originals stay in `reference/`):

  | Upstream file(s) | Becomes | Milestone |
  |---|---|---|
  | `META-INF/neoforge.mods.toml`, `META-INF/accesstransformer.cfg` (empty) | `fabric.mod.json` | done in M0 |
  | `silentgear.mixins.json` (lists no mixins) | a Fabric mixin config, created with the first mixin | when needed |
  | `data/silentgear/neoforge/biome_modifier/{overworld,nether,end}_ores.json` | `BiomeModifications.addFeature` in code | M4 |
  | `data/silentgear/loot_modifiers/add_mob_drops/*.json` (5, `neoforge:add_table`) | `LootTableEvents.MODIFY` in code | M8 |
  | `data/silentgear/loot_modifiers/{bonus_drops_trait,magmatic_smelting}.json` | code, see API_MAPPING "Loot and world" | M8 |
  | `data/neoforge/data_maps/item/{furnace_fuels,compostables}.json` | `FuelValueEvents`, `CompostableRegistry` in code | M4 |

  Biome modifier contents, for M4: overworld `underground_ores` gets `silentgear:bort_ore`,
  `silentgear:wild_flax`, `silentgear:wild_fluffy` in `#minecraft:is_overworld`. Check the nether and
  end files in `reference/` for their lists when porting.
- **Patched:**
  - `"neoforge:ingredient_type"` renamed to `"fabric:type"` in 218 files (488 uses). Fabric's custom
    ingredient codec reads the type key and the ingredient's fields inline, exactly like NeoForge, so
    nothing else changes. Ingredient types used, to register in M7: `silentgear:blueprint` (78),
    `silentgear:custom_compound` (3), `silentgear:material` (248), `silentgear:optional_tag` (52),
    `silentgear:part_type` (107).
  - `recipe/sapling/netherwood.json`: NeoForge `mod_loaded` condition replaced with
    `"fabric:load_conditions": [{"condition": "fabric:all_mods_loaded", "values": ["bonsaitrees2"]}]`.
- **Kept as is:** `data/neoforge/tags/block/needs_netherite_tool.json` (tags load for any namespace),
  `swift_swim.json` with `neoforge:swim_speed` (open question below), the 14 `render_type` model keys
  (redundant, see M2), and data for other mods (`curios`, `patchouli_books`, `weapon_attributes`, `mmorpg`).
- **Tag check:** 53 `c:` tags used by Silent Gear are defined by neither Fabric API nor Silent Gear. All are
  metals from other mods (tin, silver, steel, ...), and every use is an `optional_tag` ingredient or an
  optional tag entry. NeoForge does not define them either, so behavior is the same.
- **Offline JSON check:** all 2,291 JSON files parse in strict mode, no NeoForge keys left except `swim_speed`.
- **TEMPORARY: `data/**` is excluded from the build** (`silent-gear-fabric/build.gradle`). The data files
  refer to blocks and items registered in M4. Without them, the worldgen files stop worlds from loading and
  vanilla tags that list our blocks get dropped. **Remove the exclude in M4.**
- **Expected client log errors until M9:** 86 of the 303 item model files use Silent Gear's custom tint
  sources (`silentgear:gear_part` 71 uses, `silentgear:material` 51, `silentgear:paint_color` 1). They fail to
  load until the tint sources are registered in M9.
- `optional_data_packs/` (upstream repo root) is a pack players install by hand, not loaded by code. Not ported.
- M3 test passed. Only expected log entries: 86 item model errors (logged once per resource reload) and
  vanilla texture warnings for 6 Silent Gear block models. The block models use `minecraft:block/orientable`
  as parent but draw their own elements, so the parent's texture slots stay empty. This comes from vanilla
  model loading and happens on NeoForge too. Upstream data is left unchanged.

### 2026-10-18 (M4, registries and basic content)

- **All upstream Java is in the build** except: datagen (`data/`, M10), JEI (`compat/jei`, M8), Curios
  (`compat/curios`, replaced by Trinkets in M8), the 2 global loot modifiers (`loot/modifier`, M8) and the
  3 datagen base classes `api/data/**/*ProviderBase` (M10). The last two are excluded in
  `silent-gear-fabric/build.gradle`.
- Of 483 upstream Java files, **362 are unchanged** and 121 changed, mostly import lines. 4 new port-only
  files: `SilentGearClient`, `network/SgNetworkClient`, `setup/SgBiomeModifiers`, `setup/SgDataMaps`.
- **Approach: Silent Lib stand-ins with NeoForge's names.** Instead of rewriting upstream code, Silent Lib
  provides independent classes with the same class and method names as the NeoForge APIs Silent Gear uses,
  so ported files mostly change only their imports. Full list in API_MAPPING.md, "Silent Lib stand-ins".
  Highlights:
  - `registry.DeferredRegister/DeferredHolder/DeferredItem/DeferredBlock`. Entries are created by
    `register()`, called from `SideProxy` in NeoForge's registry order (attributes, data components,
    vanilla registries in vanilla order, then Silent Gear's registries alphabetically).
  - `DeferredRegister.DataComponents.registerComponentType` returns the plain `DataComponentType`, so the
    89 `stack.get(SgDataComponents.X)` call sites are unchanged.
  - `port.event.*`: NeoForge event classes (same getters) and a simple `NeoForge.EVENT_BUS`. Upstream
    handler bodies are unchanged; each class with handlers got a `register()` method listing them
    (replacing `@EventBusSubscriber`). Silent Gear's own add-on events use the same bus.
  - **Interface injection** (Loom class tweaker `transitive-inject-interface` + mixin): Silent Lib adds
    `IItemExtension`, `IItemPropertiesExtension`, `IBlockExtension`, `IBlockStateExtension`,
    `IBlockEntityExtension` and `IPlayerExtension` to the vanilla classes, with vanilla-equivalent defaults.
    The 20 gear item classes compile unchanged, including their `@Override`s and `super` calls.
  - `port.items.*` (item handlers), `port.menu.*` (menus with extra data, via Fabric `ExtendedMenuType`
    sending the raw bytes), `port.network.IPayloadContext`, `port.crafting.ICustomIngredient/IngredientType`,
    `port.client.*` (registration events forwarding to Fabric/vanilla, `IClientItemExtensions`).
- **Networking ported now** (planned for M6): payloads were needed to compile. The three data sync payloads
  use Fabric `registerLarge` (64 MB), because NeoForge splits big payloads automatically and Fabric only does
  it for large payloads.
- **Configs register before content.** NeoForge loads configs after all mod constructors but before it
  creates registry entries, so entry factories may read config values. `SilentGear.onInitialize()` keeps that
  order.
- **Ore generation, fuels, compostables** are code now (`SgBiomeModifiers`, `SgDataMaps`), same values and
  biome tags as the upstream JSON. Compostables also register as villager-compostable, like
  `"can_villager_compost": true`.
- **Netherwood strip** registered with Fabric `StrippableBlockRegistry` (upstream overrode a NeoForge block hook).
- **Recipe sync to clients:** upstream asks NeoForge for full recipes of chosen types; on Fabric every Silent
  Gear recipe serializer is synced (`RecipeSynchronization`), which covers the same recipes.
- **Tag `remove` lists** (3 generated tag files: incorrect_for_copper/amethyst/lapis_lazuli_tools) renamed to
  Fabric's `fabric:remove`. NeoForge's unprefixed `"remove"` was missed by the M3 scan, which looked only for
  `neoforge:` keys. Vanilla ignores `"remove"`, which would have left wrong blocks in those mining tiers.
- `silentgear.classtweaker` opens what NeoForge's access transformer opens and upstream needs:
  `Block.popExperience`, `BlockEntityType` constructor and `BlockEntitySupplier`, `Ingredient.values`,
  `AbstractContainerScreen.hoveredSlot`.
- Small one-to-one replacements at call sites, each marked `Port note:` in the code: `ItemTags/BlockTags.create(id)`,
  `ItemInstance.has(...)`, `RecipeType.simple`, `canDrownInFluidType(WATER)`, `KeyMapping.getKey()`,
  `Ingredient.isCustom()/getValues()`, `new Ingredient(custom)`, `FlowerPotBlock` constructor, `FenceGateBlock`
  constructor (`WoodType.CRIMSON` has the same sounds), `CreativeModeTab.builder()`, `ShapedRecipePattern`
  max size, `getEnchantmentLevel`, `TagAppender.remove(TagKey)` (Fabric datagen `removeTag`), environment checks.
- Dropped, no Fabric equivalent: NeoForge `KeyConflictContext.GUI` (Silent Gear keys may show as conflicting
  in the controls screen), `PlayerDestroyItemEvent` post in the fishing rod, the magnet's
  `PreventRemoteMovement` persistent-data check, pool name on injected chest loot.
- Item tint sources are registered already (planned for M9), so gear item models can load.
- **Registry tags are not bound during Fabric mod init.** Looking up a tag's contents then throws
  "Tags not bound". `WindBlastTraitEffect` did this in a static field (`BLOCKS_WIND_CHARGE_EXPLOSIONS`),
  which crashed the first M4 launch. It is now built on first use. A scan found no other static tag lookups.

### 2026-10-18 (M5 reference comparison)

- NeoForge reference instance (Modrinth App, NeoForge 26.1.2.112, Silent Gear 4.2.3, Silent Lib 11.2.0) and the
  Fabric port load identical data: 77 traits, 131 materials (123 standard), 48 parts, 2116 recipes,
  1892 advancements, 5 chest loot injections, and the client receives 77/131/48 on join.
- The reference log confirms the trident model warning and the block model warnings are upstream.
- Upstream's `recipe/sapling/netherwood.json` uses the key `conditions`, which NeoForge 26 does not read
  (it expects `neoforge:conditions`), so NeoForge logs a parse error for it. The Fabric port skips it cleanly.
- Test data pack for M5/M6/M11: `test-packs/sg_port_test`. The dev server runs in `silent-gear-fabric/run-server`.

### 2026-10-19 (M7, crafting, and JEI)

- M7 crafting hooks wired (see "Wired in M7" below). First test run crashed at startup: `CraftingMenuMixin`
  targeted `assemble(CraftingInput)`, but the compiled call is `assemble(RecipeInput)` (generic erasure). Mixin
  targets are now checked against `javap` bytecode, not only the decompiled source.
- **JEI moved forward from M8** at the user's request (finding recipes without it is slow). JEI Fabric
  29.47.0.111 is a dev runtime mod (`localRuntime`), the plugin compiles against `jei-26.1.2-common-api` and
  `jei-26.1.2-fabric-api` (`compileOnly`), and `fabric.mod.json` suggests `jei`. Players install JEI themselves.
- Silent Gear's JEI plugin (`compat/jei`, 6 classes) is back in the build. It has no NeoForge code; it uses JEI's
  cross-loader API. Changes: JEI Fabric finds plugins through the `jei_mod_plugin` entrypoint in `fabric.mod.json`
  (the `@JeiPlugin` annotation stays, Fabric ignores it), and 3 uses of NeoForge's `new Ingredient(custom)`
  became `toVanilla()`. It shows recipes from `ClientRecipeHelper` (recipe sync, M4).
- `reference/minecraft-src` holds Minecraft sources decompiled with Loom `genSources`, for writing mixins.

### 2026-10-19 (M8, gameplay hooks)

Every hook was placed where NeoForge's own patch puts it. To find those places, the patched Minecraft jar
from the Modrinth NeoForge install (`meta/libraries/net/neoforged/minecraft-client-patched/26.1.2.112`) was
disassembled with `javap`; the NeoForge sources jar does not contain the patches. Every mixin target was
checked against the bytecode of the Loom merged jar.

- **Durability** (`ItemStackMixin`): `getMaxDamage`, `setDamageValue` and `hurtAndBreak` call the item's
  `getMaxDamage`, `setDamage` and `damageItem`. `getDamage` and `isDamaged` are not wired: no Silent Gear
  item overrides them and the defaults read the same component as vanilla.
- **Living and player events** (`LivingEntityMixin`, `PlayerMixin`, `BlockBehaviourMixin`): incoming damage,
  damage before health drops, fall, experience drop, player tick, break speed.
- **Block drops** (`BlockDropsMixin`, `BlockDropsHook`): the 6-argument `Block.dropResources` (a breaker
  and a tool) collects the drops and experience, posts `BlockDropsEvent`, then spawns what is left. Drops
  from explosions and pistons stay vanilla, as in NeoForge.
- **Global loot modifiers.** Fabric has no loot modifier registry. Silent Lib adds a small one
  (`port.loot.GlobalLootModifiers`), applied by `LootTableMixin` at the same point as NeoForge: once per table
  rolled on its own, after stacks are split, never for tables rolled from inside another table. Fabric's
  `LootTableEvents.MODIFY_DROPS` was tried first and dropped: it also fires for inner tables, which would give
  bonus drops a second roll on the added mob drop tables. Silent Gear registers `bonus_drops_trait` and
  `magmatic_smelting` there (`SgLootModifiers`); the 5 `add_mob_drops` files became extra pools added with
  `LootTableEvents.MODIFY`. NeoForge orders modifiers by priority only, and all of these have the default.
- **Fuel** (`FuelValuesMixin`): `burnDuration` posts `FurnaceFuelBurnTimeEvent` with vanilla's value;
  `isFuel` also accepts stacks the event gives a burn time to.
- **Enchantment levels** (`EnchantmentHelperMixin`, `EnchantmentsPredicateMixin`): same five places NeoForge
  posts `GetEnchantmentLevelEvent`. `CommonHooks.resolveLookup` finds the enchantment registry like NeoForge's
  (running server first, then the client level); the server is tracked by `FabricEventBridge`.
- **Anvil** (`AnvilMenuMixin`): `AnvilUpdateEvent` after `createResult`, on every path.
- **Item abilities.** `IItemExtension.canPerformAction` now gives the same answers as NeoForge's overrides in
  `AxeItem`, `ShearsItem` and the other vanilla tools (by `instanceof`), so gear items that call `super` get
  them too. Vanilla checks for exactly `Items.SHEARS` or `Items.FISHING_ROD` in places where NeoForge asks
  `canPerformAction`; those are mixins now: shearing mobs (`ShearableMobsMixin`, see below), leashes and
  equipment (`EntityShearsMixin`, `LeashFenceKnotEntityMixin`), honeycomb, pumpkins, tripwire, and the fishing
  hook (without it, a gear fishing rod's hook is removed right after casting). Strip, till and path need no
  mixin: gear axes and shovels call the netherite tool's `useOn` after their own broken check. Sweep needs
  none either: gear swords, katanas and machetes are in `minecraft:swords`, which is what vanilla checks.
- **Mob shearing.** NeoForge removes the shears branch from sheep, mooshrooms, snow golems, bogged and copper
  golems and shears them from `ShearsItem.interactLivingEntity`. Here the vanilla branch stays and checks
  `canPerformAction(shears_harvest)`, so the result is the same for vanilla and gear shears.
- **Piglins and powder snow** (`PiglinAiMixin`, `PowderSnowBlockMixin`): call `makesPiglinsNeutral` and
  `canWalkOnPowderedSnow`. `getEquipmentSlot` and `getDefaultAttributeModifiers` are not wired: in 26.1 the
  `EQUIPPABLE` component picks the slot, and the only override of the second one returns the default.
- **Deferred holders in saved data** (`RegistryMixin`). Silent Gear armor puts `SgAttributes.MAGIC_ARMOR` (a
  `DeferredHolder`) into its attribute modifiers. Vanilla's registry codec only accepts a `Holder.Reference`, so
  saving, hashing or syncing that stack failed with "Unregistered holder" and crashed the game (found in M7
  testing: taking lined armor out of the crafting grid). NeoForge patches `safeCastToReference` to call
  `getDelegate()` first; the mixin does the same. The network stream codec reads `holder.value()` and needed no
  change.
- **Trinkets replaces Curios** (`compat/trinkets`). Same slots as upstream's Curios data: ring in
  `hand/ring`, necklace in `chest/necklace`, elytra in `chest/back`, and a new `hand/bracelet` slot (Trinkets
  has no bracelet slot; it uses the glove icon). Trinkets gives no slots to players by default, so
  `data/trinkets/entities/silentgear.json` assigns them, like upstream's Curios entities file. Callbacks
  (`GearTrinketCallback`) do what upstream's `GearCurio` did: tick, trait attribute modifiers (ids get the
  slot as a suffix, as Trinkets does), keep on death, equip from use, gold equip sound. The elytra gets no
  tick: Trinkets already lets glider items fly from a trinket slot and damages them, so upstream's damage
  tick would double it. The Brilliant trait on a trinket makes piglins neutral through Silent Gear's own
  `PiglinAiMixin`, because Trinkets only checks the `piglin_safe_armor` tag. The "requires Curios" tooltip now
  checks for Trinkets, and the en_us text says "Requires Trinkets to equip" (other languages still say Curios).
  Upstream's `data/curios` and `data/silentgear/curios` files stay in the jar; they do nothing on Fabric.
- **Swim speed** (`AttributeMapMixin`, `LivingEntitySwimMixin`, in Silent Gear): vanilla builds the default
  attribute sets during bootstrap, before mods register attributes, so `silentgear:swim_speed` cannot be added
  to `createLivingAttributes` like NeoForge's. Instead an entity's attribute map creates it the first time it
  is asked for it (equipment modifiers, loading, syncing). It is applied at NeoForge's two points: water
  movement speed and the upward push when jumping in water.
- **Elytra** needs nothing: the gear elytra sets vanilla's `GLIDER` component, and flight damage goes through
  the durability hook.
- Silent Gear has its own `silentgear.mixins.json` now (upstream ships an empty one).

### Hooks still to wire (stand-ins exist, nothing fires them yet)

None left after M9. Each stand-in's javadoc names the hook that calls it.

Already fired on Fabric: `BreakBlockEvent`, `LivingDeathEvent`, `LivingDamageEvent.Post`,
`PlayerLoggedInEvent`, `OnDatapackSyncEvent`, `LevelTickEvent`, `ItemTooltipEvent`, `InputEvent.Key` (in
screens), attack clicks.

Wired in M7: `PlayerEvent.ItemCraftedEvent` (`ResultSlotMixin`), stack-aware crafting remainder (Fabric API
already calls `FabricItem.getCraftingRemainder(ItemStack)`; `ItemMixin` forwards it to
`IItemExtension.getCraftingRemainder(ItemInstance)`), `isCombineRepairable` (`RepairItemRecipeMixin`),
`CommonHooks` crafting player (`CraftingMenuMixin`, `ResultSlotMixin`), `onDataPacket`
(`ClientPacketListenerMixin`).

Wired in M8: see the M8 decisions above. All events that Silent Gear listens to are now posted.

Wired in M9: see the M9 decisions below.

### 2026-10-25 (M9, client rendering)

- **Most of M9 was already done.** Gear item models are data-driven (vanilla model types plus the three tint
  sources registered in M4), the broken look is chosen in code through the item model component, and entity,
  block entity, screen, key and config screen registrations were ported in M4 and M7. Upstream registers no
  custom tooltip components, GUI layers or standalone models (its trident standalone model is a `FIXME`).
- **Worn armor and elytra tint** (`client.EquipmentLayerRendererMixin`). NeoForge asks
  `IClientItemExtensions.getArmorLayerTintColor(stack, layer, index, dyeColor)` for each equipment layer.
  Silent Gear returns the material color for layer 0 and no tint for the others, also on layers that the
  equipment asset does not mark dyeable (crimson steel armor, the elytra wings use vanilla's elytra asset).
  Vanilla would leave those untinted. The stand-in's default now returns vanilla's `getColorForLayer`, as
  NeoForge's does; `silentlib.classtweaker` makes that private method accessible.
- **First-person hand pose** (`client.ItemInHandRendererMixin`). NeoForge asks `applyForgeHandTransform`
  before vanilla's use, spin attack and swing transforms in `renderArmWithItem` and skips them when it returns
  true. Silent Gear's trident uses it for its charge animation. A mixin cannot jump like NeoForge's patch, so
  it injects at the start of that branch and, when the item handles the pose, renders the item, pops the pose
  and returns, which is what vanilla does next.
- **Re-equip animation: Fabric API, no mixin.** `FabricItem.allowComponentsUpdateAnimation` is called from
  `ItemInHandRenderer.tick` when a hand's stack changed but is the same item; `ItemMixin` forwards it to
  `shouldCauseReequipAnimation(old, new, false)`. NeoForge also asks on item and slot changes, but Silent Gear's
  override only compares item and components, so the result is the same. Vanilla 26.1 already skips the
  animation when only ignored components such as damage change, on both loaders.
- **Trident model unchanged.** Upstream's 3D held trident renderer (`GearTridentSpecialRenderer`) is never
  registered upstream, so the port keeps the M7 "Port fix" layered model. Adding the 3D model would be new
  behavior, not porting.
- **Client log, last run:** only the known upstream warnings (block model texture slots, `phantom_light`), the
  shield item model has no `particle` texture (same file as upstream), and JEI reports 16 duplicate items in the
  main creative tab (the tab code matches upstream). The NeoForge reference log (2026-10-25) shows the same
  block, `phantom_light` and shield warnings, plus `Missing textures in model silentgear:item/trident`, the
  upstream bug the M7 trident Port fix repaired. The JEI line was in a cut-off part of the reference log.
- **M9 test (2026-10-25):** game starts with the new client mixins, trident animation and throw work, mining
  does not bob, a diamond armor set looks right. The tester has low vision, so detailed visual comparisons with
  the reference (material colors, broken models, armor and elytra tint) are left for player reports after
  release; tooltips, screens and keys looked fine but not every case was checked.

### 2026-10-25 (M10, data generation)

- **Result: the ported data generators reproduce the copied upstream generated resources exactly.** 2,172 files,
  same JSON content, no file missing or extra; the only byte difference was line endings. This includes every
  Fabric-specific change made to those files since M3 (`fabric:type` ingredient keys, `silentgear:swim_speed`).
  So `runDatagen` now writes straight into `src/generated/resources`, which is the source of those files from
  now on: change the generators, not the JSON. `.gitattributes` keeps that folder LF, as datagen writes it, so a
  run that changes nothing shows nothing in git. The datagen hash cache (`.cache/`) is git-ignored and left out
  of the jar.
- **Run:** `.\gradlew.bat :silent-gear-fabric:runDatagen` (headless, about 15 seconds). Loom's
  `fabricApi.configureDataGeneration` with `client = true` (upstream generates models and sounds in the same run),
  output `src/generated/resources`, `addToResources = false` (that folder is already a resource folder).
- **Ported:** Silent Gear's `data/` package (copied from upstream, it was not part of the M4 copy) and the three
  `api/data/*ProviderBase` classes; Silent Lib's `data/client`, `data/recipe`, `data/tag` helpers. Entry point:
  `DataGenerators` implements `DataGeneratorEntrypoint` (`fabric-datagen` in `fabric.mod.json`).
- **Not ported:** `ModDataMapProvider` (furnace fuels, compostables) and `ModLootModifierProvider` (global loot
  modifiers): their output was left out in M3 because Fabric does it in code (fuel and compost registries,
  `SgLootModifiers`). Silent Lib's `LibWorldGenProvider`: upstream's world gen datagen is commented out.
- **API change in `api/data`:** `MaterialsProviderBase`, `PartsProviderBase` and `TraitsProviderBase` take a
  `PackOutput` instead of a `DataGenerator` (upstream reads NeoForge's `DataGenerator.getPackOutput()`; Fabric
  gives each provider its output). Add-ons that extend them need the same one-line change.
- **Silent Lib stand-ins:** `port.data.BlockTagsProvider` / `ItemTagsProvider` (NeoForge's names and
  constructors on vanilla's `IntrinsicHolderTagsProvider`, so upstream's `tag(...)` calls stay), and
  `port.data.ExternalTags`: vanilla refuses tag references to tags its provider does not define, NeoForge accepts
  tags that exist in other packs; this parent lookup accepts other namespaces and still checks the mod's own.
  `port.Tags` gained the 75 NeoForge tag constants the generators use, with ids read from NeoForge's compiled
  `Tags` class (16 dye tags are built in a loop there: `c:dyes/<color>`).
- **Other NeoForge patches replaced:** see the Datagen table in API_MAPPING. Notable: vanilla's block loot check
  covers every block in the game, so `ModBlockLootTables.generate(BiConsumer)` runs vanilla's loop over
  `getKnownBlocks()` like NeoForge's patch; vanilla's `EquipmentAssetProvider` only writes vanilla's assets, so
  `ModEquipmentAssetsProvider` is a plain provider with vanilla's `run` logic; NeoForge's `createItem` parses a
  namespace, so the saw base template builds its model id directly. Vanilla's loot table validation is on (upstream
  turns it off through a NeoForge hook) and passes.
- **Class tweaker additions** (`silentgear.classtweaker`, what NeoForge's access transformer opens):
  `ItemModelGenerators.BLANK_LAYER`, `ModelTemplates.createItem`, `BlockModelGenerators.ROTATION_HORIZONTAL_FACING`,
  `BlockLootSubProvider.map` and `enabledFeatures`. No mixins.

### Automated gametests (2026-10-25, user approved)

- `silent-gear-fabric/src/gametest` is a dev-only test mod (`silentgear-gametest`) for Fabric's gametest API.
  It is its own source set, so it is not in the release jar. `build.gradle` adds a `gametest` run; run it with
  `.\gradlew.bat :silent-gear-fabric:runGametest`. It is not hooked into `build` (Loom's
  `fabricApi.configureTests` would add it to `check`), so a failing test never blocks a normal build.
- The tests cover the M8 checklist through the real hooks: mining, anvil, loot tables, fall damage, item use on
  blocks and mobs, Trinkets slots. Report: `build/gametest/junit.xml`.
- `TestPlayer` is a fake survival `ServerPlayer`. Two vanilla details made a plain fake player useless:
  changing to the game mode a player already has does nothing, so the gametest server's creative abilities
  stayed (fixed by setting the abilities directly), and a `ServerPlayer` ignores all damage until its client
  sends `ServerboundPlayerLoadedPacket` (the test sends it).
- Tests must allow for random traits: iron (Malleable) and the wood rod (Flexible) sometimes skip durability
  loss, so the durability tests mine several blocks. Stone gives Ancient 2, not 1, on a three-material head.

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

- **`neoforge:swim_speed` attribute: decided 2026-10-18** (user chose option a). Silent Gear registers its own
  `silentgear:swim_speed` (`SgAttributes.SWIM_SPEED`, base 1.0, range 0 to 1024, synced, like NeoForge's),
  `swift_swim.json` points at it, and en_us.json has its name "Swim Speed". Applied while swimming since M8.
  Found because the trait failed to load and crashed world creation (upstream treats a bad trait file as fatal).
- **`neoforge:needs_netherite_tool` block tag: closed in M8.** Silent Gear only reads it (harvest tier 4 for
  ore block items and AOE tools). NeoForge ships it empty, and on Fabric a tag nobody defines is empty too,
  so the result is the same unless another mod fills it.
- **Curios slot data: closed in M8.** Replaced by `data/trinkets` slot, entity and tag files (see M8).
- **Silent Lib distribution.** Ship as a separate jar (like upstream) or nest it in the Silent Gear jar.
  Default: separate jar, decide at release.

## Mixins added

Silent Lib (`silentlib.mixins.json`) unless marked Silent Gear (`silentgear.mixins.json`). Added in M4 unless
marked otherwise. Targets were checked with `javap` (generic erasure changes some descriptors, for example
`ItemStack.is(Item)` compiles to `is(Object)`).

| Mixin | Target | What it does |
|---|---|---|
| `ItemMixin` | `Item` | Adds `IItemExtension`. Injects at the end of the constructor to keep the no-combine-repair flag. Forwards Fabric's stack-aware crafting remainder (M7) and `allowComponentsUpdateAnimation` (M9). |
| `ItemPropertiesMixin` | `Item.Properties` | Adds `IItemPropertiesExtension.setNoCombineRepair()` and stores its flag. |
| `BlockMixin` | `Block` | Adds `IBlockExtension`. No code. |
| `BlockStateBaseMixin` | `BlockBehaviour.BlockStateBase` | Adds `IBlockStateExtension`. No code. |
| `BlockEntityMixin` | `BlockEntity` | Adds `IBlockEntityExtension`. No code. |
| `PlayerMixin` | `Player` | Adds `IPlayerExtension` (`openMenu` with extra data). No code. |
| `RepairItemRecipeMixin` (M7) | `RepairItemRecipe.canCombine` | `@ModifyReturnValue`: also requires `isCombineRepairable` on both items. |
| `CraftingMenuMixin` (M7) | `CraftingMenu.slotChangedCraftingGrid` | `@WrapOperation` on `assemble`: sets the crafting player for the call. |
| `ResultSlotMixin` (M7) | `ResultSlot` | Posts `ItemCraftedEvent` after `onCraftedBy`; sets the crafting player around `getRemainingItems`. |
| `client.ClientPacketListenerMixin` (M7, client) | `ClientPacketListener` handleBlockEntityData lambda | `@WrapOperation`: calls `onDataPacket` instead of `loadWithComponents`. |
| `client.EquipmentLayerRendererMixin` (M9, client) | `EquipmentLayerRenderer.renderLayers` (11 args) | `@WrapOperation` on `getColorForLayer`: asks `IClientItemExtensions.getArmorLayerTintColor` with the layer index. |
| `client.ItemInHandRendererMixin` (M9, client) | `ItemInHandRenderer.renderArmWithItem` | `@Inject` at the second `isUsingItem` call: `applyForgeHandTransform`, then renders and returns if it applied. |
| `ItemStackMixin` (M8) | `ItemStack` | `getMaxDamage`, `setDamageValue`, `hurtAndBreak` call the item's durability hooks. |
| `LivingEntityMixin` (M8) | `LivingEntity` | Posts `LivingIncomingDamageEvent` (`hurtServer`), `LivingDamageEvent.Pre` (`actuallyHurt`), `LivingFallEvent` (`causeFallDamage`), `LivingExperienceDropEvent` (`dropExperience`). |
| `PlayerMixin` (M8 additions) | `Player` | Posts `PlayerTickEvent.Pre/Post` and `LivingDamageEvent.Pre` (players have their own `actuallyHurt`). |
| `BlockBehaviourMixin` (M8) | `BlockBehaviour.getDestroyProgress` | `@WrapOperation` on `Player.getDestroySpeed`: posts `PlayerEvent.BreakSpeed` with the block position. |
| `BlockDropsMixin` (M8) | `Block.dropResources` (6 args), `Block.popExperience` | Sends drops through `BlockDropsHook`, which posts `BlockDropsEvent`. |
| `LootTableMixin` (M8) | `LootTable.getRandomItems` (3 `Consumer` variants) | Applies `GlobalLootModifiers` once per table rolled on its own. |
| `FuelValuesMixin` (M8) | `FuelValues.burnDuration`, `isFuel` | Posts `FurnaceFuelBurnTimeEvent`; accepts stacks the event makes fuel. |
| `EnchantmentHelperMixin` (M8) | `EnchantmentHelper` | Posts `GetEnchantmentLevelEvent` in `getItemEnchantmentLevel`, both `runIterationOnItem`, `hasTag`. |
| `EnchantmentsPredicateMixin` (M8) | `EnchantmentsPredicate.Enchantments` | Adds NeoForge's `matches(DataComponentGetter)` override, so loot table enchantment checks see the event. |
| `AnvilMenuMixin` (M8) | `AnvilMenu.createResult` | Posts `AnvilUpdateEvent` at every return and applies its result. |
| `ShearableMobsMixin` (M8) | `mobInteract` of `Sheep`, `MushroomCow`, `SnowGolem`, `Bogged`, `CopperGolem` | `is(Items.SHEARS)` becomes `canPerformAction(shears_harvest)`. |
| `EntityShearsMixin` (M8) | `Entity.interact` | Shears checks become `shears_harvest` (leashes) and `shears_remove_armor` (equipment). |
| `LeashFenceKnotEntityMixin` (M8) | `LeashFenceKnotEntity.interact` | Shears check becomes `shears_harvest`. |
| `BeehiveBlockMixin` (M8) | `BeehiveBlock.useItemOn` | Shears check becomes `shears_harvest`. |
| `PumpkinBlockMixin` (M8) | `PumpkinBlock.useItemOn` | Shears check becomes `shears_carve`. |
| `TripWireBlockMixin` (M8) | `TripWireBlock.playerWillDestroy` | Shears check becomes `shears_disarm`. |
| `FishingHookMixin` (M8) | `FishingHook.shouldStopFishing` | Fishing rod check becomes `fishing_rod_cast`. |
| `PiglinAiMixin` (M8) | `PiglinAi.isWearingSafeArmor` | Tag check becomes `makesPiglinsNeutral`. |
| `PowderSnowBlockMixin` (M8) | `PowderSnowBlock.canEntityWalkOnPowderSnow` | Leather boots check becomes `canWalkOnPowderedSnow`. |
| `RegistryMixin` (M8) | `Registry.safeCastToReference` | Unwraps a `DeferredHolder` to its registry holder before the `Holder.Reference` check, as NeoForge does. |
| Silent Gear `PiglinAiMixin` (M8) | `PiglinAi.isWearingSafeArmor` | Also true for a gear trinket with Brilliant (Trinkets only). |
| Silent Gear `AttributeMapMixin` (M8) | `AttributeMap.getInstance` | Creates `silentgear:swim_speed` for any living entity on first use. |
| Silent Gear `LivingEntitySwimMixin` (M8) | `LivingEntity.travelInWater`, `jumpInLiquid` | Multiplies water speed and the swim-up push by swim speed. |

## Known gaps

None yet.

## Upstream issues fixed in the port (deliberate changes, user approved 2026-10-18)

Marked `Port fix:` in the code. These bugs are in upstream itself, so they also show on NeoForge.

- **Invisible screen labels.** `ChargerContainerScreen` (title and "Structure Level") and `GraderScreen`
  (title, and the grade of the last attempt) used colors without an alpha byte (`0x404040`, `0xFFFFFF`). In
  26.x text colors are ARGB and vanilla skips text with alpha 0. Now `0xFF404040` and `0xFFFFFFFF`.
- **Trident item had no texture.** Upstream's `models/item/trident.json` is still in the pre-1.21.4 format
  (parent `builtin/entity`, removed from vanilla, and a texture path that is a folder), and the code for its
  custom renderer is commented out (`FIXME`). The model is now a layered handheld model like the other
  tools, using the existing `textures/item/trident/` layers (rod, main, highlight) in the tint order of
  `items/trident.json`. It shows as a flat icon in hand; upstream's intended 3D held model needs the
  special renderer (milestone M9). `trident_throwing.json` is unused in 26.x and left as is.
- **Snow Walker did nothing** (approved 2026-10-25). `GearArmorItem.canWalkOnPowderedSnow` checked
  `getEquipmentSlot(stack) == FEET`, but that returns null for armor on NeoForge too (26.1 keeps the slot in the
  `EQUIPPABLE` component), so gear boots always sank in powder snow. It now checks `armorType.getSlot()`.
  Gametest `snowWalkerBootsWalkOnPowderSnow` is required again.
- **A broken pickaxe mined at full speed** (approved 2026-10-25). Broken gear axes, shovels, maces and machetes
  slow to 0.25 through `GearHelper.getDestroySpeed`, but the pickaxe used vanilla's tool component, which
  `GearTool.onRecalculatePost` leaves in place when the tool breaks (measured: broken iron pickaxe 6.0, same as
  working). `GearPickaxeItem.getDestroySpeed` now returns the broken speed when broken and is unchanged
  otherwise. This also covers the hammer, paxel and prospector hammer, which extend the pickaxe. Sickles, hoes
  and mattocks were not changed. Gametest `brokenPickaxeMinesSlowly`.
- **The break sound played twice** (found while testing the pickaxe fix, 2026-10-25). `GearHelper.damageItem`
  decided "this use breaks the tool" from the damage *before* traits. When Malleable or Flexible then cancelled
  that point, the break sound played but the tool stayed whole, so the next block still mined at full speed and
  the sound played again when it really broke. It now uses the damage after traits. Gametest
  `breakCallbackRunsOnceWhenReallyBroken`.
- **Trait tooltip lines showed twice** (approved 2026-10-25). `GearItem.buildAttributes` added every trait's
  attribute modifiers, and `GearData.setGearAttributeModifiers` added them again, so each was stored twice with the
  same id (only one applied in play) and the tooltip showed lines like "-60% Gravity" twice, on every gear item
  with an attribute trait. The default `GearItem.buildAttributes` now adds nothing; `GearData` still adds the
  trait modifiers once for every gear item. Gametest `attributeModifiersAreNotDuplicated`.
- **Magic armor showed as "magic_armor" in tooltips** (approved 2026-10-25). `SgAttributes.MAGIC_ARMOR` uses
  the bare string `magic_armor` as its translation key, and no upstream lang file defines it. en_us.json now
  has `"magic_armor": "Magic Armor"`. Lang data only, no code change; other languages still show the raw key.

## Upstream issues (same on NeoForge, not changed by the port)

- **Block model warnings** for `alloy_forge`, `salvager`, `gear_smithing_table`, `refabricator`,
  `material_grader` (unused parent texture slots) and `phantom_light` (no blockstate file, it is invisible).

## Fixes after the first M4 runs

- Startup crash: `WindBlastTraitEffect` looked up a tag during init (see M4 notes).
- World creation crash: `swift_swim` trait used `neoforge:swim_speed` (see open questions, decided).
- Material Grader, Paint Mixer and Salvager menus would not open: upstream opens them with vanilla's
  `openMenu(provider)`. NeoForge then sends empty extra data; Fabric refuses to open an extended menu that way.
  `GraderBlock`, `SalvagerBlock` and `ModContainerBlock` now call `openMenu(provider, buf -> {})`.
