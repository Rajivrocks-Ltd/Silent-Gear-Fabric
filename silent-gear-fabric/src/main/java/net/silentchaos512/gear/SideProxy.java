package net.silentchaos512.gear;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.silentchaos512.gear.client.ClientRecipeHelper;
import net.silentchaos512.gear.client.gui.book.MaterialBookScreen;
import net.silentchaos512.gear.event.GearEvents;
import net.silentchaos512.gear.event.RepairHandler;
import net.silentchaos512.gear.event.ServerEvents;
import net.silentchaos512.gear.gear.material.MaterialSerializers;
import net.silentchaos512.gear.gear.part.CoreGearPart;
import net.silentchaos512.gear.gear.part.PartSerializers;
import net.silentchaos512.gear.gear.trait.effect.EnchantmentTraitEffect;
import net.silentchaos512.gear.gear.trait.effect.OxidationTraitEffect;
import net.silentchaos512.gear.gear.util.MagnetPullTracker;
import net.silentchaos512.gear.loot.modifier.SgLootModifiers;
import net.silentchaos512.gear.network.SgNetwork;
import net.silentchaos512.gear.setup.*;
import net.silentchaos512.gear.setup.gear.*;
import net.silentchaos512.gear.util.GearData;
import net.silentchaos512.gear.util.IAoeTool;
import net.silentchaos512.lib.event.Greetings;
import net.silentchaos512.lib.event.InitialSpawnItems;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Common setup, and the server/client specific parts of {@link IProxy}.
 *
 * <p>Port notes:
 * <ul>
 *     <li>Upstream hands every {@code DeferredRegister} to the NeoForge mod event bus, and NeoForge
 *     creates the entries later, one registry at a time. Here the constructor calls
 *     {@code register()} on each one directly, in the same order NeoForge uses: attributes and data
 *     components first, then vanilla registries in vanilla order, then Silent Gear's own registries
 *     in alphabetical order.</li>
 *     <li>NeoForge game bus listeners become Fabric events. Classes that used
 *     {@code @EventBusSubscriber} now have a {@code register()} method called from here.</li>
 *     <li>Client-only setup (screens, renderers, key mappings, tooltips, config screen) moved to
 *     {@link SilentGearClient}, because Fabric runs it from a separate client entry point.</li>
 *     <li>Global loot modifiers are not registered yet, they become code in milestone M8.</li>
 * </ul>
 */
class SideProxy implements IProxy {
    @Nullable
    private static MinecraftServer server;
    @Nullable
    private static CreativeModeTab creativeModeTab;

    SideProxy() {
        registerContent();
        ClientRecipeHelper.registerRecipeSync();

        commonSetup();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SgCommands.registerAll(dispatcher));
        onAddReloadListeners();
        ServerLifecycleEvents.SERVER_STARTED.register(SideProxy::serverStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(SideProxy::serverStopping);

        SgNetwork.registerPayloadTypes();
        SgNetwork.registerServerReceivers();

        // Former @EventBusSubscriber classes (common side)
        GearEvents.register();
        ServerEvents.register();
        RepairHandler.register();
        GearData.EventHandler.register();
        IAoeTool.BreakHandler.register();
        MagnetPullTracker.register();
        EnchantmentTraitEffect.EventHandler.register();
        OxidationTraitEffect.EventHandler.register();
        SgLoot.register();
        SgLootModifiers.register();
        SgBiomeModifiers.register();
        SgBlocks.registerStrippables();
    }

    /**
     * Creates every registry entry, in NeoForge's registration order.
     */
    private static void registerContent() {
        // NeoForge puts these first, because items depend on them at construction time
        SgAttributes.ATTRIBUTES.register();
        SgDataComponents.REGISTRAR.register();

        // Vanilla registries, in vanilla order
        SgSounds.REGISTRAR.register();
        SgBlocks.BLOCKS.register();
        SgEntities.ENTITIES.register();
        SgItems.ITEMS.register();
        SgBlockEntities.BLOCK_ENTITIES.register();
        SgMenuTypes.MENU_TYPES.register();
        SgRecipes.RECIPE_TYPES.register();
        SgRecipes.RECIPE_SERIALIZERS.register();
        SgVillages.PROFESSIONS.register();
        SgVillages.POINTS_OF_INTEREST.register();
        SgLoot.LOOT_CONDITIONS.register();
        SgLoot.LOOT_FUNCTIONS.register();
        SgCreativeTabs.CREATIVE_TABS.register();
        SgCriteriaTriggers.TRIGGER_TYPES.register();
        SgSlotDisplayTypes.SLOT_DISPLAY_TYPES.register();
        SgRecipeBookCategories.RECIPE_BOOK_CATEGORIES.register();

        // Custom ingredients use Fabric's own serializer registry
        SgIngredientTypes.register();

        // Silent Gear's registries, in alphabetical order of registry id
        GearTypes.REGISTRAR.register();
        MaterialModifiers.REGISTRAR.register();
        MaterialSerializers.REGISTRAR.register();
        PartSerializers.REGISTRAR.register();
        PartTypes.REGISTRAR.register();
        GearProperties.REGISTRAR.register();
        TraitConditions.REGISTRAR.register();
        TraitEffectTypes.REGISTRAR.register();
    }

    private static void commonSetup() {
        InitialSpawnItems.add(SilentGear.getId("starter_blueprints"), p -> {
            if (Config.Common.isLoaded() && Config.Common.spawnWithStarterBlueprints.get()) {
                return List.of(SgItems.BLUEPRINT_PACKAGE.toStack());
            }
            return List.of();
        });
        InitialSpawnItems.add(SilentGear.getId("material_book"), p -> {
            if (Config.Common.isLoaded() && Config.Common.spawnWithMaterialBook.get()) {
                return List.of(SgItems.MATERIAL_BOOK.toStack());
            }
            return List.of();
        });

        Greetings.addMessage(SideProxy::detectDataLoadingFailure);

        SgDataMaps.register();
    }

    private static void onAddReloadListeners() {
        var loader = ResourceLoader.get(PackType.SERVER_DATA);
        loader.registerReloadListener(SilentGear.getId("trait"), SgRegistries.TRAIT);
        loader.registerReloadListener(SilentGear.getId("material"), SgRegistries.MATERIAL);
        loader.registerReloadListener(SilentGear.getId("part"), SgRegistries.PART);
        // Same order as upstream, which added the listeners in this order
        loader.addListenerOrdering(SilentGear.getId("trait"), SilentGear.getId("material"));
        loader.addListenerOrdering(SilentGear.getId("material"), SilentGear.getId("part"));
    }

    private static void serverStarted(MinecraftServer minecraftServer) {
        server = minecraftServer;
        SilentGear.LOGGER.info("Traits loaded: {}", SgRegistries.TRAIT.stream().count());
        SilentGear.LOGGER.info("Parts loaded: {}", SgRegistries.PART.stream().count());
        SilentGear.LOGGER.info("- Compound: {}", SgRegistries.PART.stream()
                .filter(part -> part instanceof CoreGearPart).count());
        SilentGear.LOGGER.info("- Simple: {}", SgRegistries.PART.stream()
                .filter(part -> !(part instanceof CoreGearPart)).count());
        SilentGear.LOGGER.info("Materials loaded: {}", SgRegistries.MATERIAL.stream().count());
        SilentGear.LOGGER.info("- Standard: {}", SgRegistries.MATERIAL.stream()
                .filter(mat -> mat.getSerializer() == MaterialSerializers.SIMPLE.get()).count());
    }

    private static void serverStopping(MinecraftServer minecraftServer) {
        server = null;
    }

    @Nullable
    @Override
    public Player getClientPlayer() {
        return null;
    }

    @Nullable
    @Override
    public Level getClientLevel() {
        return null;
    }

    @Override
    public boolean checkClientInstance() {
        return true;
    }

    @Override
    public boolean checkClientConnection() {
        return true;
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return server;
    }

    @Override
    public void openMaterialBookScreen() {
    }

    static class Client extends SideProxy {
        Client() {
            super();
            // Port note: upstream's client listeners and config screen are registered in SilentGearClient.
        }

        @Nullable
        @Override
        public Player getClientPlayer() {
            return Minecraft.getInstance().player;
        }

        @Nullable
        @Override
        public Level getClientLevel() {
            Minecraft mc = Minecraft.getInstance();
            //noinspection ConstantConditions -- mc can be null during runData and some other circumstances
            return mc != null ? mc.level : null;
        }

        @Override
        public boolean checkClientInstance() {
            //noinspection ConstantConditions -- mc can be null during runData and some other circumstances
            return Minecraft.getInstance() != null;
        }

        @Override
        public boolean checkClientConnection() {
            Minecraft mc = Minecraft.getInstance();
            //noinspection ConstantConditions -- mc can be null during runData and some other circumstances
            return mc != null && mc.getConnection() != null;
        }

        @Override
        public void openMaterialBookScreen() {
            var minecraft = Minecraft.getInstance();
            minecraft.execute(() -> minecraft.setScreen(new MaterialBookScreen()));
        }
    }

    static class Server extends SideProxy {
        Server() {
            super();
        }
    }

    @Nullable
    public static Component detectDataLoadingFailure(Player player) {
        // Check if parts/traits have loaded. If not, a mod has likely broken the data loading process.
        // We should inform the user and tell them what to look for in the log.
        if (SgRegistries.MATERIAL.keySet().isEmpty() || SgRegistries.PART.keySet().isEmpty() || SgRegistries.TRAIT.keySet().isEmpty()) {
            String msg = "Materials, parts, and/or traits have not loaded! This may be caused by a broken mod, even those not related to Silent Gear. Search your log for \"Failed to reload data packs\" to find the error.";
            SilentGear.LOGGER.error(msg);
            return Component.literal(msg);
        }
        return null;
    }
}
