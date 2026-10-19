# NeoForge to Fabric API mapping

Lookup table for the port. Fabric class names were checked against the Fabric API source at
`reference/fabric` (tag `0.155.3+26.1.2`). Many Fabric API names changed in 26.1 to match
Mojang names (for example `KeyBindingHelper` is now `KeyMappingHelper`, `ItemGroupEvents` is now
`CreativeModeTabEvents`), so do not trust older tutorials.

Abbreviation: `f.a` means `net.fabricmc.fabric.api`.

## Silent Lib stand-ins (built in M2 and M4)

Independent classes in Silent Lib with NeoForge's class and method names. A ported file usually only
changes its import from the left column to the right column.

| NeoForge | Silent Lib stand-in |
|---|---|
| `neoforge.registries.DeferredRegister/DeferredHolder/DeferredItem/DeferredBlock` | `lib.registry.*` (call `register()` instead of `register(IEventBus)`) |
| `neoforge.common.ItemAbility`, `ItemAbilities` | `lib.port.ItemAbility`, `ItemAbilities` |
| `neoforge.common.Tags` | `lib.port.Tags` (only the constants in use) |
| `neoforge.common.util.Lazy` | `lib.port.Lazy` |
| `neoforge.common.CommonHooks.getCraftingPlayer()` | `lib.port.CommonHooks` |
| `bus.api.Event`, `EventPriority`, `ICancellableEvent`, `neoforge.common.NeoForge` | `lib.port.event.*` (`addListener` takes the event class) |
| `neoforge.event.**` (game events) | `lib.port.event.**`, same sub-packages |
| `neoforge.client.event.*` (registration events, `InputEvent`) | `lib.port.client.event.*` |
| `neoforge.client.extensions.common.*` | `lib.port.client.extensions.common.*` |
| `IItemExtension`, `IItemPropertiesExtensions` (`setNoCombineRepair`), `IBlockExtension`, `IBlockStateExtension`, `IBlockEntityExtension`, `IPlayerExtension` (`openMenu`) | `lib.port.*`, injected into the vanilla classes |
| `neoforge.items.IItemHandler`, `ItemStackHandler`, `ComponentItemHandler`, `SlotItemHandler` | `lib.port.items.*` |
| `common.crafting.ICustomIngredient`, `IngredientType` | `lib.port.crafting.*` (on Fabric's custom ingredient API) |
| `network.handling.IPayloadContext`, `network.codec.NeoForgeStreamCodecs` | `lib.port.network.*` |
| `common.extensions.IMenuTypeExtension`, `network.IContainerFactory` | `lib.port.menu.*` (on Fabric's `ExtendedMenuType`) |

## Entry, lifecycle, environment

| NeoForge | Fabric | Notes |
|---|---|---|
| `@Mod` class, `neoforge.mods.toml` | `ModInitializer` / `ClientModInitializer`, `fabric.mod.json` | done in M0 |
| `SideProxy.Client` / `SideProxy.Server` | `SilentGearClient` (client entrypoint), common code in `SilentGear` | keep `IProxy` if other code calls `SilentGear.PROXY` |
| `ModList.get().isLoaded(id)` | `FabricLoader.getInstance().isModLoaded(id)` | |
| `ModContainer.getModInfo().getVersion()` | `FabricLoader.getInstance().getModContainer(id).get().getMetadata().getVersion()` | |
| `FMLEnvironment.getDist()`, `Dist.CLIENT` | `FabricLoader.getInstance().getEnvironmentType()`, `EnvType.CLIENT` | |
| `FMLEnvironment.isProduction()` | `!FabricLoader.getInstance().isDevelopmentEnvironment()` | |
| `@EventBusSubscriber(value = Dist.CLIENT)` | register from `SilentGearClient` | |
| `FMLCommonSetupEvent` | end of `onInitialize()` | |
| `FMLClientSetupEvent` | `onInitializeClient()` | |
| `InterModEnqueueEvent`, `InterModProcessEvent` | drop (empty upstream) | |
| `ServerStartedEvent`, `ServerStoppingEvent` | `f.a.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED` / `SERVER_STOPPING` | |
| `ServerLifecycleHooks.getCurrentServer()` | `entity.level().getServer()` where an entity or level is at hand, otherwise keep a reference from `ServerLifecycleEvents` | done in Silent Lib `TeleportUtils` |
| `Player.getPersistentData()` + `PERSISTED_NBT_TAG` | Fabric data attachment, persistent and `copyOnDeath()` | done: `SilentLib.PERSISTED_PLAYER_DATA` |

## Registration

| NeoForge | Fabric | Notes |
|---|---|---|
| `DeferredRegister`, `DeferredHolder`, `DeferredItem`, `DeferredBlock` | `Registry.register(...)` in `onInitialize` | plan: small Silent Lib helper classes with the same method names (`get()`, `value()`, `getId()`, `toStack()`) to keep upstream code unchanged |
| `RegistryBuilder`, `NewRegistryEvent` | `f.a.event.registry.FabricRegistryBuilder` | add `RegistryAttribute.SYNCED` where upstream uses `.sync(true)` |
| `NeoForgeRegistries.INGREDIENT_TYPES` | `f.a.recipe.v1.ingredient.CustomIngredientSerializer.register` | |
| `IMenuTypeExtension.create(IContainerFactory)` | `f.a.menu.v1.ExtendedMenuType` | menu with extra open data |
| `common.util.Lazy` | `com.google.common.base.Suppliers.memoize` | or a tiny `Lazy` class in Silent Lib |
| Creative tabs registry | vanilla `Registries.CREATIVE_MODE_TAB` + `f.a.creativetab.v1.FabricCreativeModeTab.builder()` | `CreativeModeTabEvents` to add to vanilla tabs |

## Game events

| NeoForge | Fabric | Notes |
|---|---|---|
| `RegisterCommandsEvent` | `f.a.command.v2.CommandRegistrationCallback` | |
| `AddServerReloadListenersEvent` | `f.a.resource.v1.ResourceLoader` (server data) | 26.1 resource loader v1, keyed by `Identifier` |
| `OnDatapackSyncEvent` | `ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS` | fires on join and after `/reload` |
| `PlayerEvent.PlayerLoggedInEvent` | `f.a.networking.v1.ServerPlayConnectionEvents.JOIN` | |
| `PlayerEvent.BreakSpeed` | mixin into `Player.getDestroySpeed` | |
| `PlayerEvent.ItemCraftedEvent` | mixin into `ResultSlot.checkTakeAchievements` | |
| `BreakBlockEvent` | `f.a.event.player.PlayerBlockBreakEvents.BEFORE` / `AFTER` | |
| `BlockDropsEvent` | mixin into `Block.dropResources` path, or loot API | decide in M8 |
| `LivingIncomingDamageEvent`, `LivingDamageEvent.Pre` | `f.a.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE`, mixin when the amount is changed | |
| `LivingDamageEvent.Post` | `ServerLivingEntityEvents.AFTER_DAMAGE` | |
| `LivingDeathEvent` | `ServerLivingEntityEvents.AFTER_DEATH` (or `ALLOW_DEATH` if cancelling) | |
| `LivingFallEvent`, `LivingExperienceDropEvent` | mixin | |
| `PlayerTickEvent.Pre` | mixin into `Player.tick` head, or loop players in `ServerTickEvents.START_SERVER_TICK` | |
| `LevelTickEvent.Post` | `f.a.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK` | |
| `ServerTickEvent` | `ServerTickEvents.START_SERVER_TICK` / `END_SERVER_TICK` | |
| `ClientTickEvent` | `f.a.client.event.lifecycle.v1.ClientTickEvents` | |
| `RenderFrameEvent` | read `Minecraft.getInstance().getDeltaTracker()` directly | no Fabric event exists. Done in Silent Lib `ClientTicks` |
| `FurnaceFuelBurnTimeEvent` | `f.a.registry.FuelValueEvents` | |
| `GetEnchantmentLevelEvent` | mixin into `EnchantmentHelper` | `f.a.item.v1.EnchantmentEvents` does not cover level queries |
| `AnvilUpdateEvent` | mixin into `AnvilMenu.createResult` | |
| `LootTableLoadEvent` | `f.a.loot.v3.LootTableEvents.MODIFY` | |
| `ItemTooltipEvent` | `f.a.client.item.v1.ItemTooltipCallback` | |
| `VillagerTradesEvent` | not needed (commented out upstream) | |
| `NeoForge.EVENT_BUS` custom events extending `Event` | `f.a.event.EventFactory.createArrayBacked` | Silent Gear add-on API (`api/event`) |
| `EventPriority` | Fabric event phases (`Event.addPhaseOrdering`) | only if order matters |

## Items, blocks, tools

| NeoForge | Fabric | Notes |
|---|---|---|
| `IItemExtension.getMaxDamage(ItemStack)` | set `DataComponents.MAX_DAMAGE` on the stack, or mixin into `ItemStack.getMaxDamage` | gear durability depends on materials, so likely a mixin delegating to the item |
| `IItemExtension.setDamage`, `damageItem` | mixin into `ItemStack.setDamageValue` / `hurtAndBreak` delegating to the item | |
| `IItemExtension.shouldCauseReequipAnimation` | `f.a.item.v1.FabricItem.allowComponentsUpdateAnimation` / `allowContinuingBlockBreaking` | |
| `IItemExtension.getCraftingRemainder(ItemInstance)` | `FabricItem.getRecipeRemainder(ItemStack)` | check exact 26.1 signature in `reference/fabric` |
| `IItemExtension.getEquipmentSlot(ItemStack)` | `DataComponents.EQUIPPABLE` component | |
| `IItemExtension.getDefaultAttributeModifiers` | `FabricItem.getAttributeModifiers(ItemStack)` or `ATTRIBUTE_MODIFIERS` component | |
| `IItemExtension.makesPiglinsNeutral` | item tag `minecraft:piglin_safe_armor` is static, so dynamic check needs a mixin | |
| `IItemExtension.canWalkOnPowderedSnow` | mixin into `PowderSnowBlock.canEntityWalkOnPowderSnow` | |
| `IBlockEntityExtension.onDataPacket` | vanilla `loadWithComponents` path, drop the override | vanilla already applies the update tag |
| `IBlockExtension.getToolModifiedState` (log strip) | `f.a.registry.StrippableBlockRegistry` | |
| `ItemAbility`, `ItemAbilities`, `canPerformAction` | plan: small `ItemAbility` shim with the same constants, plus direct `useOn` logic | no Fabric equivalent |
| `BlockState.canHarvestBlock`, `onDestroyedByPlayer`, `getExpDrop` | vanilla equivalents: `Player.hasCorrectToolForDrops`, `Block.playerWillDestroy` + `Level.removeBlock`, `Block.popExperience` with enchantment logic | AOE mining code |
| `ItemStack.getEnchantmentLevel(Holder)` | `EnchantmentHelper.getItemEnchantmentLevel(Holder, ItemStack)` | |
| `common.Tags.Items.*`, `Tags.Blocks.*` | plan: `Tags` shim class with identical `c:` ids | Fabric `ConventionalItemTags` uses different Java field names, a shim keeps upstream code unchanged |
| `CommonHooks.getCraftingPlayer()` | small thread-local set by a mixin around crafting, or pass player where available | 8 uses |
| `NeoForgeMod.SWIM_SPEED` | open decision, see PORTING_NOTES | |
| `NeoForgeMod.WATER_TYPE`, `canDrownInFluidType` | `entity.canBreatheUnderwater()` / `isInWater()` logic | |

## Crafting

| NeoForge | Fabric | Notes |
|---|---|---|
| `ICustomIngredient`, `IngredientType` | `f.a.recipe.v1.ingredient.CustomIngredient`, `CustomIngredientSerializer` | JSON key `"neoforge:ingredient_type"` becomes `"fabric:type"` |
| `RecipesReceivedEvent` | `f.a.client.recipe.v1.sync.ClientRecipeSynchronizedEvent` | |
| `Ingredient.isSimple()` | `!ingredient.requiresTesting()` (Fabric interface injection) | done in Silent Lib |
| `RecipeMatcher.findMatches(items, ingredients)` | Silent Lib `ShapelessIngredientMatcher.matches` (own code) | NeoForge's class is LGPL, not copied |
| `stack.get(holder)`, `stack.set(holder, value)` with a `DeferredHolder` | make the field a plain `DataComponentType` | vanilla methods only take the type itself |
| Access transformer on `ShapedRecipe` / `ShapelessRecipe` | class tweaker in Silent Lib | |

## Inventories

| NeoForge | Fabric | Notes |
|---|---|---|
| `IItemHandler`, `ItemStackHandler`, `ComponentItemHandler`, `SlotItemHandler` | vanilla `Container` / `SimpleContainer`, `Slot` | Fabric Transfer API wraps vanilla containers for hoppers and pipes |
| `transfer.ResourceHandler`, `ItemResource`, `ItemStacksResourceHandler` | `f.a.transfer.v1.item.ItemVariant`, `InventoryStorage` | |
| `RegisterCapabilitiesEvent` | nothing (upstream bodies are commented out) | |
| `FluidStack` | `f.a.transfer.v1.fluid.FluidVariant` | Silent Lib `NameUtils` only |

## Networking

| NeoForge | Fabric | Notes |
|---|---|---|
| `RegisterPayloadHandlersEvent` `playToClient` / `playToServer` | `f.a.networking.v1.PayloadTypeRegistry.clientboundPlay()` / `serverboundPlay()` + `ServerPlayNetworking.registerGlobalReceiver` / `f.a.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver` | reuse the same `CustomPacketPayload` records and `StreamCodec`s. Check the exact 26.1 method names on `PayloadTypeRegistry` in M6 |
| `PacketDistributor.sendToPlayer`, `sendToAllPlayers` | `ServerPlayNetworking.send(player, payload)`, loop over players | |
| `ClientPacketDistributor.sendToServer` | `ClientPlayNetworking.send(payload)` | |
| `IPayloadContext` | `ServerPlayNetworking.Context` / `ClientPlayNetworking.Context` | |
| `NeoForgeStreamCodecs` | vanilla `ByteBufCodecs` or a copied helper | |

## Config

| NeoForge | Fabric | Notes |
|---|---|---|
| `ModConfigSpec`, `ModConfig.Type` | Forge Config API Port: `fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry.INSTANCE.register(MOD_ID, type, spec)` (check exact method in M8) | code and imports stay the same: the port itself provides `net.neoforged.neoforge.common.ModConfigSpec` and `net.neoforged.fml.config.ModConfig`. **Exception to the "no net.neoforged imports" rule**, the port ships these package names on purpose |
| `ModConfigEvent` | `fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents.loading(MOD_ID)` / `reloading(MOD_ID)` | |
| `ConfigurationScreen`, `IConfigScreenFactory` | Forge Config API Port ships `ConfigurationScreen`, register it with `fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry` | Mod Menu shows it |

## Loot and world

| NeoForge | Fabric | Notes |
|---|---|---|
| `neoforge:add_table` + `loot_table_id` | `LootTableEvents.MODIFY`: add a pool that runs the referenced table, for the listed table keys | |
| Custom `IGlobalLootModifier` (bonus drops trait, magmatic smelting) | mixin at the end of block loot generation, or `LootTableEvents.MODIFY_DROPS` | check event list in `reference/fabric/fabric-loot-api-v3` |
| `neoforge:add_features` biome modifier | `f.a.biome.v1.BiomeModifications.addFeature` | |
| `neoforge:mod_loaded` condition | `"fabric:load_conditions": [{"condition": "fabric:all_mods_loaded", "values": [...]}]` | |
| data maps `furnace_fuels`, `compostables` | `FuelValueEvents`, `f.a.registry.CompostableRegistry` | |

## Client

| NeoForge | Fabric | Notes |
|---|---|---|
| `RegisterColorHandlersEvent.ItemTintSources` | `ItemTintSources.ID_MAPPER.put(id, codec)` | field opened by Fabric API's transitive class tweaker |
| `IClientItemExtensions` special renderers | `SpecialModelRenderers.ID_MAPPER.put(id, codec)` | also opened by Fabric API |
| `RegisterMenuScreensEvent` | `MenuScreens.register(type, Screen::new)` | opened by Fabric API |
| `EntityRenderersEvent.RegisterRenderers` | `f.a.client.rendering.v1.EntityRendererRegistry`, `BlockEntityRendererRegistry` | |
| `ModelEvent.RegisterStandalone` | `f.a.client.model.loading.v1.ModelLoadingPlugin` | |
| `RegisterKeyMappingsEvent` | `f.a.client.keymapping.v1.KeyMappingHelper.registerKeyMapping` | |
| `KeyConflictContext` | drop | no Fabric equivalent, minor |
| `InputEvent.Key` | poll in `ClientTickEvents.END_CLIENT_TICK` | |
| `InputEvent.InteractionKeyMappingTriggered` | mixin into `Minecraft.startUseItem` / `startAttack` | |
| `getArmorLayerTintColor` | 26.x equipment layers with dyeable tint, or `f.a.client.rendering.v1.ArmorRenderer` | check in M9 |
| `applyForgeHandTransform` | mixin into `ItemInHandRenderer` | check in M9 |

## Datagen

| NeoForge | Fabric |
|---|---|
| `GatherDataEvent` | `f.a.datagen.v1.DataGeneratorEntrypoint`, `FabricDataGenerator` |
| `BlockTagsProvider`, `ItemTagsProvider` | `FabricTagsProvider.BlockTagsProvider` / `ItemTagsProvider` |
| `GlobalLootModifierProvider`, `DataMapProvider` | not needed once those are code |

## Third party

| NeoForge | Fabric |
|---|---|
| Curios | Trinkets Updated `eu.pb4:trinkets:4.0.1+26.1` (soft dependency) |
| JEI NeoForge | JEI Fabric `29.47.0.111` for 26.1.2; plugin registered with the `jei_mod_plugin` entrypoint (done in M7) |
| Caelus | vanilla glider component |
