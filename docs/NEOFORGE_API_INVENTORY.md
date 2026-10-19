# NeoForge API Inventory (milestone M1)

This file lists everything in upstream Silent Gear and Silent Lib that depends on NeoForge.
It defines the scope of the port. Scanned on 2026-10-18 against the commits in
[PORTING_NOTES.md](PORTING_NOTES.md).

How it was made: scripted scans of `reference/`, plus a comparison against the NeoForge
26.1.2.95 sources (`reference/neoforge-src`) and the vanilla 26.1.2 jar, so that overrides of
NeoForge-only methods are found even when they have no import.

## 1. Summary

| | Silent Gear 4.2.3 | Silent Lib 11.2.0 |
|---|---|---|
| Java files | 528 (47,895 lines) | 63 (4,995 lines) |
| Files with `net.neoforged` imports | 124 | 12 |
| Distinct NeoForge imports | 89 | 25 |
| `@SubscribeEvent` / `addListener` handlers | about 60 | 9 |
| Network payloads | 10 (5 to client, 5 to server), play phase only | 0 |
| Custom registries | 8 | 0 |
| Resource files with `neoforge:` keys | 228 JSON files, plus 6 files under `neoforge` folders | 0 |
| Access transformer entries | 0 (file exists, empty) | 3 |
| Mixins | 0 (config exists, empty) | 0 |

**silent-utils is not used.** Neither Silent Gear nor Silent Lib imports `net.silentchaos512.utils`
on the 26.1.x branches. It is out of scope.

### NeoForge usage per Silent Gear package

| Package | Files | Lines | Files importing NeoForge | Milestone |
|---|---|---|---|---|
| setup | 29 | 3,341 | 26 | M4 |
| item | 63 | 4,721 | 18 | M4, M8 |
| crafting | 42 | 3,058 | 14 | M7 |
| data (datagen) | 36 | 8,027 | 13 | M10 |
| gear | 61 | 6,042 | 11 | M5, M8 |
| block | 61 | 4,657 | 8 | M4, M7 |
| api | 81 | 6,187 | 7 | M5 |
| client | 60 | 2,918 | 6 | M9 |
| event | 5 | 600 | 4 | M8 |
| util | 16 | 2,704 | 4 | M8 |
| network | 19 | 579 | 3 | M6 |
| command | 9 | 1,666 | 2 | M5 |
| compat | 9 | 913 | 2 | M8 |
| loot | 9 | 243 | 2 | M8 |
| core | 13 | 889 | 1 | M5 |
| root (`SilentGear`, `SideProxy`, `Config`, `IProxy`) | 4 | | 3 | M4 |
| advancements, entity, world, mixin | 11 | | 0 | copy as is |

Datagen (8k lines) can stay out of the build until M10. Until then we use the upstream
`src/generated/resources` output.

## 2. NeoForge imports grouped by category

Counts are "files that import it" in Silent Gear (SG) and Silent Lib (SL).
A full import to file table is in the appendix.

### 2.1 Mod entry, lifecycle, environment
| Import | SG | SL | Notes |
|---|---|---|---|
| `fml.common.Mod` | 1 | 1 | `SilentGear`, `SilentLib` |
| `bus.api.IEventBus` | 3 | 1 | constructor injection |
| `fml.ModContainer`, `neoforgespi.language.IModInfo` | 3, 1 | 1 | version lookup, config registration |
| `fml.ModList` | 7 | 1 | `isLoaded("curios")`, `isLoaded("sgearmetalworks")`, version lookup |
| `fml.loading.FMLEnvironment`, `api.distmarker.Dist` | 3, 8 | 1, 1 | side checks, `@EventBusSubscriber(value = Dist.CLIENT)` |
| `fml.event.lifecycle.*` (common setup, client setup, IMC) | 2 | 0 | `SideProxy`, `SgBlocks` |
| `neoforge.event.server.ServerStartedEvent`, `ServerStoppingEvent` | 1, 1 | 0 | logs counts, keeps server reference |
| `neoforge.server.ServerLifecycleHooks` | 0 | 1 | `TeleportUtils` |

### 2.2 Registration
| Import | SG | SL | Notes |
|---|---|---|---|
| `registries.DeferredRegister` | 26 | 1 | about 30 registrars in `SideProxy` |
| `registries.DeferredHolder` | 20 | 1 | |
| `registries.DeferredItem`, `DeferredBlock` | 5, 4 | 0 | `SgItems`, `SgBlocks` |
| `registries.RegistryBuilder`, `NewRegistryEvent` | 1, 1 | 0 | `SgRegistries`, 8 custom registries (see 4) |
| `registries.NeoForgeRegistries` | 2 | 1 | ingredient types, biome modifier serializers |
| `common.extensions.IMenuTypeExtension`, `network.IContainerFactory` | 1, 1 | 0 | menus that read extra data from a buffer |
| `common.util.Lazy` | 4 | 0 | memoized supplier |

### 2.3 Game events (NeoForge.EVENT_BUS)
| Import | SG | SL |
|---|---|---|
| `common.NeoForge` | 6 | 6 |
| `bus.api.SubscribeEvent`, `fml.common.EventBusSubscriber`, `bus.api.EventPriority` | 24, 23, 2 | 0 |
| `event.entity.player.PlayerEvent` (BreakSpeed, ItemCrafted, PlayerLoggedIn) | 6 | 2 |
| `event.level.block.BreakBlockEvent`, `event.level.BlockDropsEvent` | 5, 1 | 0 |
| `event.entity.living.*` (incoming damage, damage pre/post, death, fall, XP drop) | 1 | 0 |
| `event.tick.PlayerTickEvent`, `LevelTickEvent`, `ServerTickEvent` | 1, 1 | 1 |
| `event.AnvilUpdateEvent` | 1 | 0 |
| `event.furnace.FurnaceFuelBurnTimeEvent` | 1 | 0 |
| `event.enchanting.GetEnchantmentLevelEvent` | 1 | 0 |
| `event.LootTableLoadEvent` | 1 | 0 |
| `event.RegisterCommandsEvent` | 1 | 1 |
| `event.AddServerReloadListenersEvent` | 1 | 0 |
| `event.OnDatapackSyncEvent` | 2 | 0 |
| `event.entity.player.ItemTooltipEvent` | 3 | 0 |
| `bus.api.Event` (Silent Gear's own add-on events) | 3 | 0 |

### 2.4 Items, tools, tags
| Import | SG | Notes |
|---|---|---|
| `common.ItemAbility`, `common.ItemAbilities` | 9, 2 | tool actions, see 3.3 |
| `common.Tags` | 10 (+1 SL) | about 40 distinct `c:` tags, plus `Tags.Blocks.NEEDS_NETHERITE_TOOL` |
| `common.CommonHooks` | 8 | only `CommonHooks.getCraftingPlayer()` |
| `common.NeoForgeMod` | 2 | `SWIM_SPEED` attribute, `WATER_TYPE` fluid type |
| `registries.datamaps.builtin.*` (FurnaceFuel, Compostable) | 1 | datagen only |

### 2.5 Crafting
| Import | SG | SL | Notes |
|---|---|---|---|
| `common.crafting.ICustomIngredient`, `IngredientType` | 7, 7 | 2 | 6 custom ingredient classes (see 4) |

### 2.6 Inventories and transfer
| Import | SG | SL | Notes |
|---|---|---|---|
| `items.IItemHandler`, `ItemStackHandler`, `ComponentItemHandler`, `SlotItemHandler` | 3, 1, 1, 1 | 0 | |
| `transfer.ResourceHandler`, `transfer.item.*` | 3 | 0 | |
| `capabilities.RegisterCapabilitiesEvent` | 2 | 0 | **all block entity capability registrations are commented out upstream** |
| `fluids.FluidStack` | 0 | 1 | `NameUtils` |

### 2.7 Networking
| Import | SG | Notes |
|---|---|---|
| `network.event.RegisterPayloadHandlersEvent` | 1 | `SgNetwork` |
| `network.handling.IPayloadContext` | 3 | |
| `network.PacketDistributor`, `client.network.ClientPacketDistributor` | 3, 4 | |
| `network.codec.NeoForgeStreamCodecs` | 3 | |

### 2.8 Config
| Import | SG | Notes |
|---|---|---|
| `common.ModConfigSpec`, `fml.config.ModConfig`, `fml.event.config.ModConfigEvent` | 1, 1, 1 | `Config` (COMMON and CLIENT specs) |
| `client.gui.ConfigurationScreen`, `IConfigScreenFactory` | 1, 1 | in-game config screen |

Silent Lib has no config.

### 2.9 Loot and world
| Import | SG | SL |
|---|---|---|
| `common.loot.IGlobalLootModifier`, `LootModifier`, `AddTableLootModifier`, `LootTableIdCondition` | 4, 2, 1, 1 | 0 |
| `common.world.BiomeModifier(s)` | 0 | 1 (datagen only) |

### 2.10 Client
| Import | SG | SL |
|---|---|---|
| `client.event.RegisterColorHandlersEvent` (ItemTintSources) | 1 | 0 |
| `client.event.EntityRenderersEvent` | 2 | 0 |
| `client.event.ModelEvent` (RegisterStandalone) | 1 | 0 |
| `client.extensions.common.IClientItemExtensions`, `RegisterClientExtensionsEvent` | 2, 1 | 0 |
| `client.event.RegisterMenuScreensEvent` | 1 | 0 |
| `client.event.RegisterKeyMappingsEvent`, `client.settings.KeyConflictContext`, `client.event.InputEvent` | 1, 1, 2 | 0 |
| `client.event.RecipesReceivedEvent` | 1 | 0 |
| `client.event.ClientTickEvent`, `RenderFrameEvent`, `RenderGuiLayerEvent`, `client.gui.VanillaGuiLayers` | 0 | 2, 1, 1, 1 |

### 2.11 Datagen (M10)
| Import | SG | SL |
|---|---|---|
| `data.event.GatherDataEvent` | 4 | 0 |
| `common.data.BlockTagsProvider`, `ItemTagsProvider`, `SoundDefinitionsProvider`, `GlobalLootModifierProvider`, `DataMapProvider`, `DatapackBuiltinEntriesProvider` | 2, 0, 1, 1, 1, 0 | 0, 1, 0, 0, 0, 1 |

## 3. Overrides and calls of NeoForge-only methods (no import needed)

NeoForge adds methods to vanilla classes through `IItemExtension`, `IBlockExtension` and similar
interfaces. Code that overrides or calls them compiles on NeoForge only.

### 3.1 Overrides
| Extension method | Count | Classes |
|---|---|---|
| `IItemExtension.getMaxDamage(ItemStack)` | 15 | all `Gear*Item` classes, `BasicGearItem`, `MainPartItem`, `GearCurioItem` |
| `IItemExtension.setDamage(ItemStack, int)` | 13 | `BasicGearItem`, `GearArmorItem`, `GearAxeItem`, `GearBowItem`, `GearCrossbowItem`, `GearElytraItem`, `GearFishingRodItem`, `GearHoeItem`, `GearMaceItem`, `GearShearsItem`, `GearShieldItem`, `GearShovelItem`, `GearTridentItem` |
| `IItemExtension.damageItem(...)` | 12 | same as above minus `GearShieldItem` |
| `IItemExtension.shouldCauseReequipAnimation(...)` | 11 | `BasicGearItem`, `GearArrowItem`, `GearAxeItem`, `GearBowItem`, `GearCrossbowItem`, `GearFishingRodItem`, `GearHoeItem`, `GearMaceItem`, `GearShearsItem`, `GearShovelItem`, `GearTridentItem` |
| `IItemExtension.canPerformAction(..., ItemAbility)` | 6 | `GearAxeItem`, `GearHammerItem`, `GearHoeItem`, `GearPaxelItem`, `GearProspectorHammerItem`, `GearShearsItem` |
| `IItemExtension.getCraftingRemainder(ItemInstance)` | 3 | `ModKitItem`, `AbstractBlueprintItem`, `BlueprintBookItem` |
| `IItemExtension.makesPiglinsNeutral(...)` | 3 | `GearArmorItem`, `GearElytraItem`, `CurioGearItemCapability` |
| `IItemExtension.canWalkOnPowderedSnow(...)` | 1 | `GearArmorItem` |
| `IItemExtension.getEquipmentSlot(ItemStack)` | 1 | `GearElytraItem` |
| `IItemExtension.getDefaultAttributeModifiers(...)` | 1 | `GearCurioItem` |
| `IBlockEntityExtension.onDataPacket(...)` | 4 | `AlloyMakerBlockEntity`, `GraderBlockEntity`, `PaintMixerBlockEntity`, `StoneAnvilBlockEntity` |
| `IBlockExtension.getToolModifiedState(...)` | 1 | `WoodBlock` (log stripping) |
| `IClientItemExtensions.applyForgeHandTransform(...)` | 1 | `SgClientItemExtensions` |
| `IClientItemExtensions.getArmorLayerTintColor(...)` | 1 | `SgEntities` |

No `IBlockExtension` overrides other than `getToolModifiedState`.

### 3.2 Calls
| Call | Where |
|---|---|
| `ItemStack.getEnchantmentLevel(Holder)` | `GearEvents` (silk touch check) |
| `LivingEntity.canDrownInFluidType(NeoForgeMod.WATER_TYPE)` | `ExtraDamageTraitEffect` (AQUATIC) |
| `BlockState.canHarvestBlock(...)`, `onDestroyedByPlayer(...)`, `getExpDrop(...)` | `IAoeTool` (area mining) |
| `Block.onDestroyedByPlayer(...)`, `BlockState.getExpDrop(...)` | `GearSickleItem` |
| `Holder.getDelegate()` | `CodecUtils` |
| `GearType.canPerformAction(ItemAbility)` | Silent Gear's own method, but typed on `ItemAbility` |

### 3.3 Item abilities used
`AXE_STRIP`, `AXE_SCRAPE`, `AXE_WAX_OFF`, `HOE_TILL`, `SWORD_SWEEP`, and the default sets
`DEFAULT_AXE_ACTIONS`, `DEFAULT_HOE_ACTIONS`, `DEFAULT_SHOVEL_ACTIONS`, `DEFAULT_SHEARS_ACTIONS`,
`DEFAULT_FISHING_ROD_ACTIONS`, `DEFAULT_TRIDENT_ACTIONS`.

## 4. Custom registries, ingredients, events

**Custom registries** (`setup/SgRegistries`, all built with `RegistryBuilder`):
`GEAR_TYPE`, `PART_TYPE`, `GEAR_PROPERTY`, `TRAIT_CONDITION`, `TRAIT_EFFECT_TYPE`,
`MATERIAL_SERIALIZER`, `MATERIAL_MODIFIER_TYPE`, `PART_SERIALIZER`.

**Data loaded by reload listeners** (not datapack registries): traits, materials, parts,
registered in `SideProxy.onAddReloadListeners`.

**Custom ingredients** (`crafting/ingredient`): `BlueprintIngredient`, `CustomAlloyIngredient`,
`GearPartIngredient`, `GearTypeIngredient`, `OptionalTagIngredient`, `PartMaterialIngredient`.

**Silent Gear's own add-on events** (`api/event`, extend NeoForge `Event`): `GearItemEvent`,
`GearNamePrefixesEvent`, `GetMaterialPropertiesEvent`, `GetPropertyModifiersEvent`, `GetTraitsEvent`.

## 5. Event handlers

| Event | Handler class(es) | Fabric target (see API_MAPPING) |
|---|---|---|
| `NewRegistryEvent` | SgRegistries | static `FabricRegistryBuilder` |
| `FMLCommonSetupEvent` | SideProxy, SgBlocks | end of `onInitialize` |
| `RegisterCapabilitiesEvent` | SideProxy, CurioGearItemCapability | nothing (body commented out), Trinkets for curios |
| `RegisterPayloadHandlersEvent` | SgNetwork | `PayloadTypeRegistry` + `ServerPlayNetworking` / `ClientPlayNetworking` |
| `AddServerReloadListenersEvent` | SideProxy | `ResourceLoader` (resource loader v1) |
| `RegisterCommandsEvent` | SideProxy (SgCommands), SilentLib | `CommandRegistrationCallback` |
| `ServerStartedEvent`, `ServerStoppingEvent` | SideProxy | `ServerLifecycleEvents` |
| `OnDatapackSyncEvent` | ServerEvents, ClientRecipeHelper | `ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS` |
| `PlayerEvent.PlayerLoggedInEvent` | ServerEvents, GearData, SideProxy.Client, Greetings, InitialSpawnItems | `ServerPlayConnectionEvents.JOIN` |
| `PlayerEvent.BreakSpeed` | GearEvents, IAoeTool, FluffyBlock | mixin (see PORTING_NOTES) |
| `PlayerEvent.ItemCraftedEvent` | GearEvents | mixin into result slot |
| `BreakBlockEvent` | GearEvents, IAoeTool | `PlayerBlockBreakEvents.BEFORE` / `AFTER` |
| `BlockDropsEvent` | GearEvents | mixin or loot API, decide in M8 |
| `LivingIncomingDamageEvent`, `LivingDamageEvent.Pre`, `LivingDamageEvent.Post` (x2) | GearEvents | `ServerLivingEntityEvents.ALLOW_DAMAGE` / `AFTER_DAMAGE`, mixin for value changes |
| `LivingDeathEvent` | GearEvents | `ServerLivingEntityEvents.AFTER_DEATH` |
| `LivingFallEvent` | GearEvents | mixin |
| `LivingExperienceDropEvent` | GearEvents | mixin |
| `PlayerTickEvent.Pre` | GearEvents | `ServerTickEvents` loop or mixin |
| `LevelTickEvent.Post` | MagnetPullTracker | `ServerTickEvents.END_WORLD_TICK` |
| `ServerTickEvent` | SilentLib ServerTicks | `ServerTickEvents` |
| `ClientTickEvent`, `RenderFrameEvent` | SilentLib ClientTicks | `ClientTickEvents`, render event or mixin |
| `FurnaceFuelBurnTimeEvent` | GearEvents | `FuelValueEvents` |
| `GetEnchantmentLevelEvent` | EnchantmentTraitEffect | mixin into `EnchantmentHelper` level lookup (Fabric `EnchantmentEvents` does not cover level queries) |
| `AnvilUpdateEvent` | RepairHandler | mixin into `AnvilMenu` |
| `LootTableLoadEvent` | SgLoot | `LootTableEvents.MODIFY` |
| `ItemTooltipEvent` | TooltipHandler, GearItemTooltips, SgDataComponents | `ItemTooltipCallback` |
| `RegisterColorHandlersEvent.ItemTintSources` | SgItemTintSources | `ItemTintSources.ID_MAPPER.put` (opened by Fabric API) |
| `EntityRenderersEvent.RegisterRenderers` | SgEntities, SgBlockEntities | `EntityRendererRegistry`, `BlockEntityRendererRegistry` |
| `ModelEvent.RegisterStandalone` | SgEntities | Fabric model loading API |
| `RegisterClientExtensionsEvent` | SgEntities | special model renderer / armor render hook |
| `RegisterMenuScreensEvent` | SgMenuTypes | `MenuScreens.register` (opened by Fabric API) |
| `RegisterKeyMappingsEvent`, `InputEvent.Key`, `InputEvent.InteractionKeyMappingTriggered` | KeyTracker, ClientEvents | `KeyMappingHelper`, `ClientTickEvents`, mixin for interaction |
| `RecipesReceivedEvent` | ClientRecipeHelper | `ClientRecipeSynchronizedEvent` |
| `ModConfigEvent` | Config | Forge Config API Port `ModConfigEvents` |
| `GatherDataEvent.Client` | DataGenerators | M10 |
| `VillagerTradesEvent` | SgVillages | **commented out upstream, nothing to port** |
| `GearNamePrefixesEvent` (own) | OxidationTraitEffect | Fabric `Event` |
| `InterModEnqueueEvent`, `InterModProcessEvent` | SideProxy | empty bodies, drop |

## 6. Silent Lib classes used by Silent Gear (M2 scope)

35 of 51 Silent Lib classes are needed (30 used directly, 5 more pulled in by those).
Classes marked **NF** import NeoForge and need real porting work. The rest should copy as is.

**Needed, runtime:**
- `SilentLib` **NF** (registers `loot_container` data component, `dimension_filter` placement modifier, teleport command)
- `collection.StackList`
- `component.LootContainer`
- `crafting.recipe.CraftingRecipeExtension`, `ExtendedShapedRecipe`, `ExtendedShapelessRecipe` (use the 3 access transformer entries)
- `event.ClientTicks` **NF**, `event.ServerTicks` **NF**, `event.Greetings` **NF**, `event.InitialSpawnItems` **NF**
- `inventory.SlotOutputOnly`
- `item.FakeItemUseContext`, `item.LootContainerItem`
- `util.Color`, `ColorBlendAlgorithm`, `ColorUtils` (uses mixbox), `EnumUtils`, `InventoryUtils`, `MathUtils`, `NameUtils` **NF** (`FluidStack`), `PlayerUtils`, `TimeUtils`, `DimPos`, `LootUtils`, `TeleportUtils` **NF**
- `command.internal.TeleportCommand`
- `world.placement.DimensionFilterPlacement`

**Needed, datagen only (defer to M10):**
`data.client.LibBlockModelGenerators`, `LibModelProvider`, `LibModelTemplates`,
`data.recipe.ExtendedShapedRecipeBuilder` **NF**, `ExtendedShapelessRecipeBuilder` **NF**, `LibRecipeProvider`,
`data.tag.LibBlockItemTagsProvider`, `LibItemTagsProvider` **NF**.

**Not needed:** `block.IBlockProvider`, `client.gui.DebugRenderOverlay`, `HudAnchor`,
`client.gui.nbt.DisplayNBTList`, `DisplayNBTScreen`, `client.key.InputUtils`,
`crafting.ingredient.IngredientWithCount`, `data.worldgen.LibWorldGenProvider`, `util.Anchor`,
`AttributeHelper`, `CollectionUtils`, `MCMathUtils`, `NBTToJson`, `StringUtils`,
`TextRenderUtils`, `WorldUtils`.

Silent Lib resources to copy: `assets/silentlib/lang/*`, `assets/silentlib/models/block/*` (5 models), `CREDITS.md`.

## 7. Resource files with NeoForge constructs (M3 scope)

| Construct | Count | Where | Fabric replacement |
|---|---|---|---|
| `"neoforge:ingredient_type": "silentgear:..."` | 488 uses in 218 files | generated `recipe/**` (157 files), `silentgear_materials` (28 files) | rename key to `"fabric:type"` (custom ingredient API), mechanical |
| `"type": "neoforge:add_table"` + `"condition": "neoforge:loot_table_id"` | 5 modifiers, 23 conditions | `loot_modifiers/add_mob_drops/*` | code: `LootTableEvents.MODIFY` adding the referenced table |
| `silentgear:bonus_drops_trait`, `silentgear:magmatic_smelting` loot modifiers | 2 | `loot_modifiers/*` | code (they change drops after the table rolls) |
| `"type": "neoforge:add_features"` biome modifiers | 3 | `data/silentgear/neoforge/biome_modifier/{overworld,nether,end}_ores.json` | code: `BiomeModifications.addFeature` |
| `"conditions": [{"type": "neoforge:mod_loaded"}]` | 1 | `recipe/sapling/netherwood.json` (bonsaitrees2 compat) | `"fabric:load_conditions"` with `fabric:all_mods_loaded` |
| attribute `neoforge:swim_speed` | 1 | `silentgear_traits/swift_swim.json` | needs a decision, see PORTING_NOTES |
| data map `data/neoforge/data_maps/item/furnace_fuels.json` | 1 | generated | code: `FuelValueEvents` |
| data map `data/neoforge/data_maps/item/compostables.json` | 1 | generated | code: `CompostableRegistry` |
| tag `data/neoforge/tags/block/needs_netherite_tool.json` | 1 | generated | keep file, our `Tags` shim keeps the same id |
| `META-INF/neoforge.mods.toml` | 1 | main | `fabric.mod.json` (done in M0) |

Other data folders that are for third-party mods and load harmlessly when that mod is absent:
`data/silentgear/curios` (Curios slots, Trinkets needs its own format), `patchouli_books`,
`weapon_attributes` (Better Combat), `data/mmorpg`.

## 8. Access transformers and mixins

**Silent Gear `accesstransformer.cfg`:** empty.

**Silent Lib `accesstransformer.cfg`:**
```
protected net.minecraft.world.item.crafting.ShapelessRecipe result
protected net.minecraft.world.item.crafting.ShapelessRecipe ingredients
protected net.minecraft.world.item.crafting.ShapedRecipe result
```
Translate to a class tweaker (`silentlib.classtweaker`) in M2. Fabric 26.1 calls access
wideners "class tweakers".

**Mixins:** `silentgear.mixins.json` exists upstream but lists no mixins.

## 9. Third-party integrations

| Upstream | Files | Fabric plan |
|---|---|---|
| Curios (`compat/curios`) | `CuriosCompat`, `CurioGearItemCapability`, plus `isLoaded` checks in 4 places | Trinkets Updated, soft dependency |
| JEI (`compat/jei`) | 6 classes | ported in M7 (JEI Fabric 29.47.0.111, `jei_mod_plugin` entrypoint) |
| Caelus | only a mod id constant in `util.Const` | `GearElytraItem` uses vanilla glider component, verify in M8 |
| sgearmetalworks | 2 `isLoaded` checks | keep check, swap to `FabricLoader` |

## Appendix: every NeoForge import and the files that use it

Package prefix `net.neoforged.` is left out. File names without `.java`.

### Silent Gear

| NeoForge import | Files |
|---|---|
| `api.distmarker.Dist` | ClientEvents, GearTridentItem, KeyTracker, SgBlockEntities, SgEntities, SgItemTintSources, SgMenuTypes, SilentGear |
| `bus.api.EventPriority` | GearEvents, ServerEvents |
| `bus.api.Event` | GearItemEvent, GetMaterialPropertiesEvent, GetPropertyModifiersEvent |
| `bus.api.IEventBus` | CuriosCompat, SideProxy, SilentGear |
| `bus.api.SubscribeEvent` | ClientEvents, ClientRecipeHelper, Config, DataGenerators, EnchantmentTraitEffect, GearData, GearEvents, GearItemTooltips, IAoeTool, KeyTracker, MagnetPullTracker, OxidationTraitEffect, RepairHandler, ServerEvents, SgBlockEntities, SgBlocks, SgDataComponents, SgEntities, SgItemTintSources, SgLoot, SgMenuTypes, SgNetwork, SgRegistries, TooltipHandler |
| `fml.ModContainer` | SideProxy, SilentGear, TraitsCommand |
| `fml.ModList` | ChargerBlockEntity, GearCurioItem, GearData, GraderBlockEntity, SilentGear, TraitHelper, TraitsCommand |
| `fml.common.EventBusSubscriber` | ClientEvents, ClientRecipeHelper, Config, DataGenerators, EnchantmentTraitEffect, GearData, GearEvents, GearItemTooltips, IAoeTool, KeyTracker, MagnetPullTracker, OxidationTraitEffect, RepairHandler, ServerEvents, SgBlockEntities, SgBlocks, SgDataComponents, SgEntities, SgItemTintSources, SgLoot, SgMenuTypes, SgNetwork, SgRegistries |
| `fml.common.Mod` | SilentGear |
| `fml.config.ModConfig` | SilentGear |
| `fml.event.config.ModConfigEvent` | Config |
| `fml.event.lifecycle.*` | SideProxy |
| `fml.event.lifecycle.FMLCommonSetupEvent` | SgBlocks |
| `fml.loading.FMLEnvironment` | GearShieldItem, GearTridentItem, SilentGear |
| `neoforge.capabilities.RegisterCapabilitiesEvent` | CurioGearItemCapability, SideProxy |
| `neoforge.client.event.EntityRenderersEvent` | SgBlockEntities, SgEntities |
| `neoforge.client.event.InputEvent` | ClientEvents, KeyTracker |
| `neoforge.client.event.ModelEvent` | SgEntities |
| `neoforge.client.event.RecipesReceivedEvent` | ClientRecipeHelper |
| `neoforge.client.event.RegisterColorHandlersEvent` | SgItemTintSources |
| `neoforge.client.event.RegisterKeyMappingsEvent` | KeyTracker |
| `neoforge.client.event.RegisterMenuScreensEvent` | SgMenuTypes |
| `neoforge.client.extensions.common.IClientItemExtensions` | SgClientItemExtensions, SgEntities |
| `neoforge.client.extensions.common.RegisterClientExtensionsEvent` | SgEntities |
| `neoforge.client.gui.ConfigurationScreen` | SideProxy |
| `neoforge.client.gui.IConfigScreenFactory` | SideProxy |
| `neoforge.client.network.ClientPacketDistributor` | BlueprintBookContainerScreen, ClientEvents, KeyTracker, ToggleableWorkMode |
| `neoforge.client.settings.KeyConflictContext` | KeyTracker |
| `neoforge.common.CommonHooks` | CoatingSmithingRecipe, GearArrowItem, GearPartSwapRecipe, ModKitPaintPartRecipe, ModKitRemovePartRecipe, PaintUtils, QuickRepairRecipe, UpgradeSmithingRecipe |
| `neoforge.common.ItemAbilities` | GearTypes, WoodBlock |
| `neoforge.common.ItemAbility` | GearAxeItem, GearHammerItem, GearHelper, GearHoeItem, GearPaxelItem, GearProspectorHammerItem, GearShearsItem, GearType, WoodBlock |
| `neoforge.common.ModConfigSpec` | Config |
| `neoforge.common.NeoForgeMod` | ExtraDamageTraitEffect, TraitsProvider |
| `neoforge.common.NeoForge` | AbstractGearPart, CompoundMaterial, CoreGearPart, FluffyBlock, GearHelper, SideProxy |
| `neoforge.common.Tags` | GearEvents, IAoeTool, MaterialsProvider, ModBlockItemTagsProvider, ModBlockTagsProvider, ModItemTagsProvider, ModRecipesProvider, OreBlockItem, SgArmorMaterials, TraitsProvider |
| `neoforge.common.crafting.ICustomIngredient` | BlueprintIngredient, CustomAlloyIngredient, GearPartIngredient, GearTypeIngredient, OptionalTagIngredient, PartMaterialIngredient, SgIngredientTypes |
| `neoforge.common.crafting.IngredientType` | BlueprintIngredient, CustomAlloyIngredient, GearPartIngredient, GearTypeIngredient, OptionalTagIngredient, PartMaterialIngredient, SgIngredientTypes |
| `neoforge.common.data.BlockTagsProvider` | ModBlockTagsProvider, ModItemTagsProvider |
| `neoforge.common.data.DataMapProvider` | ModDataMapProvider |
| `neoforge.common.data.GlobalLootModifierProvider` | ModLootModifierProvider |
| `neoforge.common.data.SoundDefinitionsProvider` | ModSoundDefinitionsProvider |
| `neoforge.common.extensions.IMenuTypeExtension` | SgMenuTypes |
| `neoforge.common.loot.AddTableLootModifier` | ModLootModifierProvider |
| `neoforge.common.loot.IGlobalLootModifier` | BonusDropsTraitLootModifier, MagmaticTraitLootModifier, ModLootModifierProvider, SgLoot |
| `neoforge.common.loot.LootModifier` | BonusDropsTraitLootModifier, MagmaticTraitLootModifier |
| `neoforge.common.loot.LootTableIdCondition` | ModLootModifierProvider |
| `neoforge.common.util.Lazy` | GearTool, PartBlueprintItem, ShapedGearRecipe, ShapelessGearRecipe |
| `neoforge.data.event.GatherDataEvent` | DataGenerators, ModItemTagsProvider, ModLootModifierProvider, ModLootTables |
| `neoforge.event.AddServerReloadListenersEvent` | SideProxy |
| `neoforge.event.AnvilUpdateEvent` | RepairHandler |
| `neoforge.event.LootTableLoadEvent` | SgLoot |
| `neoforge.event.OnDatapackSyncEvent` | ClientRecipeHelper, ServerEvents |
| `neoforge.event.RegisterCommandsEvent` | SgCommands |
| `neoforge.event.enchanting.GetEnchantmentLevelEvent` | EnchantmentTraitEffect |
| `neoforge.event.entity.living.*` | GearEvents |
| `neoforge.event.entity.player.ItemTooltipEvent` | GearItemTooltips, SgDataComponents, TooltipHandler |
| `neoforge.event.entity.player.PlayerEvent` | FluffyBlock, GearData, GearEvents, IAoeTool, ServerEvents, SideProxy |
| `neoforge.event.furnace.FurnaceFuelBurnTimeEvent` | GearEvents |
| `neoforge.event.level.BlockDropsEvent` | GearEvents |
| `neoforge.event.level.block.BreakBlockEvent` | GearEvents, IAoeTool, OxidationTraitEffect, Trait, TraitEffect |
| `neoforge.event.server.ServerStartedEvent` | SideProxy |
| `neoforge.event.server.ServerStoppingEvent` | SideProxy |
| `neoforge.event.tick.LevelTickEvent` | MagnetPullTracker |
| `neoforge.event.tick.PlayerTickEvent` | GearEvents |
| `neoforge.items.ComponentItemHandler` | IContainerItem |
| `neoforge.items.IItemHandler` | AlloyMakerBlockEntity, BlueprintBookContainerMenu, BlueprintBookItem |
| `neoforge.items.ItemStackHandler` | AlloyMakerBlockEntity |
| `neoforge.items.SlotItemHandler` | BlueprintBookContainerMenu |
| `neoforge.network.IContainerFactory` | SgMenuTypes |
| `neoforge.network.PacketDistributor` | MaterialsCommand, ServerEvents, TraitsCommand |
| `neoforge.network.codec.NeoForgeStreamCodecs` | BlockFillerTraitEffect, ExtraDamageTraitEffect, MaterialGrade |
| `neoforge.network.event.RegisterPayloadHandlersEvent` | SgNetwork |
| `neoforge.network.handling.IPayloadContext` | DataResourceManager, SgClientPayloadHandler, SgServerPayloadHandler |
| `neoforge.registries.DeferredBlock` | ModBlockModelGenerator, ModBlockTagsProvider, SgBlockEntities, SgBlocks |
| `neoforge.registries.DeferredHolder` | GearItemSet, GearItemSets, GearTypes, MaterialModifiers, PartSerializers, PartTypes, SgAttributes, SgBlockEntities, SgBlocks, SgCreativeTabs, SgCriteriaTriggers, SgEntities, SgIngredientTypes, SgItems, SgLoot, SgMenuTypes, SgRecipeBookCategories, SgRecipes, SgSounds, TraitConditions |
| `neoforge.registries.DeferredItem` | CraftingItems, GearItemSet, ModItemModelProvider, ModRecipesProvider, SgItems |
| `neoforge.registries.DeferredRegister` | CraftingItems, GearItemSet, GearProperties, GearTypes, MaterialModifiers, MaterialSerializers, PartSerializers, PartTypes, SgAttributes, SgBlockEntities, SgBlocks, SgCreativeTabs, SgCriteriaTriggers, SgDataComponents, SgEntities, SgIngredientTypes, SgItems, SgLoot, SgMenuTypes, SgRecipeBookCategories, SgRecipes, SgSlotDisplayTypes, SgSounds, SgVillages, TraitConditions, TraitEffectTypes |
| `neoforge.registries.NeoForgeRegistries` | SgIngredientTypes, SgLoot |
| `neoforge.registries.NewRegistryEvent` | SgRegistries |
| `neoforge.registries.RegistryBuilder` | SgRegistries |
| `neoforge.registries.datamaps.builtin.Compostable` | ModDataMapProvider |
| `neoforge.registries.datamaps.builtin.FurnaceFuel` | ModDataMapProvider |
| `neoforge.registries.datamaps.builtin.NeoForgeDataMaps` | ModDataMapProvider |
| `neoforge.transfer.ResourceHandler` | SgContainerBlockEntity |
| `neoforge.transfer.item.ItemResource` | SgContainerBlockEntity |
| `neoforge.transfer.item.ItemStacksResourceHandler` | SgContainerBlockEntity |
| `neoforgespi.language.IModInfo` | TraitsCommand |

### Silent Lib

| NeoForge import | Files |
|---|---|
| `api.distmarker.Dist` | ClientTicks |
| `bus.api.IEventBus` | SilentLib |
| `fml.ModContainer` | SilentLib |
| `fml.ModList` | SilentLib |
| `fml.common.Mod` | SilentLib |
| `fml.loading.FMLEnvironment` | ClientTicks |
| `neoforge.client.event.ClientTickEvent` | ClientTicks, DebugRenderOverlay |
| `neoforge.client.event.RenderFrameEvent` | ClientTicks |
| `neoforge.client.event.RenderGuiLayerEvent` | DebugRenderOverlay |
| `neoforge.client.gui.VanillaGuiLayers` | DebugRenderOverlay |
| `neoforge.common.NeoForge` | ClientTicks, DebugRenderOverlay, Greetings, InitialSpawnItems, ServerTicks, SilentLib |
| `neoforge.common.Tags` | LibWorldGenProvider |
| `neoforge.common.crafting.ICustomIngredient` | ExtendedShapedRecipeBuilder, ExtendedShapelessRecipeBuilder |
| `neoforge.common.data.DatapackBuiltinEntriesProvider` | LibWorldGenProvider |
| `neoforge.common.data.ItemTagsProvider` | LibItemTagsProvider |
| `neoforge.common.world.BiomeModifier` | LibWorldGenProvider |
| `neoforge.common.world.BiomeModifiers` | LibWorldGenProvider |
| `neoforge.event.RegisterCommandsEvent` | SilentLib |
| `neoforge.event.entity.player.PlayerEvent` | Greetings, InitialSpawnItems |
| `neoforge.event.tick.ServerTickEvent` | ServerTicks |
| `neoforge.fluids.FluidStack` | NameUtils |
| `neoforge.registries.DeferredHolder` | SilentLib |
| `neoforge.registries.DeferredRegister` | SilentLib |
| `neoforge.registries.NeoForgeRegistries` | LibWorldGenProvider |
| `neoforge.server.ServerLifecycleHooks` | TeleportUtils |

