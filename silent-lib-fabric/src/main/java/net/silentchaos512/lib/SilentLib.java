package net.silentchaos512.lib;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.silentchaos512.lib.command.internal.TeleportCommand;
import net.silentchaos512.lib.component.LootContainer;
import net.silentchaos512.lib.event.Greetings;
import net.silentchaos512.lib.event.InitialSpawnItems;
import net.silentchaos512.lib.event.ServerTicks;
import net.silentchaos512.lib.port.event.FabricEventBridge;
import net.silentchaos512.lib.world.placement.DimensionFilterPlacement;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Common entry point for Silent Lib on Fabric.
 *
 * <p>Replaces the NeoForge {@code @Mod} class of the same name. Fabric creates this class
 * through its no-argument constructor and then calls {@link #onInitialize()} on both the
 * client and the dedicated server.
 *
 * <p>Port notes: upstream registers its content through {@code DeferredRegister} on the mod
 * event bus. Here the static fields register themselves when this class loads, which Fabric
 * does during mod initialization while the registries are still open. The fields are plain
 * vanilla objects instead of {@code DeferredHolder}s, so callers use them directly.
 */
public final class SilentLib implements ModInitializer {
    /** The mod id, shared with the upstream NeoForge version. */
    public static final String MOD_ID = "silentlib";
    /** Logger for all Silent Lib messages. */
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Data component that stores which loot table a {@link net.silentchaos512.lib.item.LootContainerItem} rolls. */
    public static final DataComponentType<LootContainer> LOOT_CONTAINER = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            getId("loot_container"),
            DataComponentType.<LootContainer>builder()
                    .persistent(LootContainer.CODEC)
                    .networkSynchronized(LootContainer.STREAM_CODEC)
                    .build()
    );

    /** Placement modifier that only allows a feature in (or outside of) a list of dimensions. */
    public static final PlacementModifierType<DimensionFilterPlacement> DIMENSION_FILTER_PLACEMENT = Registry.register(
            BuiltInRegistries.PLACEMENT_MODIFIER_TYPE,
            getId("dimension_filter"),
            () -> DimensionFilterPlacement.CODEC
    );

    /**
     * Player data that survives death. Replaces NeoForge's {@code Player.getPersistentData()}
     * sub-compound {@code PlayerPersisted}. Used by {@link net.silentchaos512.lib.util.PlayerUtils}.
     */
    public static final AttachmentType<CompoundTag> PERSISTED_PLAYER_DATA = AttachmentRegistry.create(
            getId("persisted_data"),
            builder -> builder
                    .persistent(CompoundTag.CODEC)
                    .initializer(CompoundTag::new)
                    .copyOnDeath()
    );

    /**
     * Called by Fabric Loader once the game is ready for mods to register content.
     *
     * <p>Upstream registers the command and creates the event helper singletons lazily. On Fabric
     * the helpers register their callbacks in {@code init()} methods, so they are called here.
     */
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                TeleportCommand.register(dispatcher));

        FabricEventBridge.init();
        ServerTicks.init();
        Greetings.init();
        InitialSpawnItems.init();

        LOGGER.info("Silent Lib (Fabric) initialized");
    }

    /**
     * Gets the version of the loaded Silent Lib mod.
     *
     * @return the version string, or {@code "0.0.0"} if the mod container is missing
     */
    public static String getVersion() {
        Optional<ModContainer> o = FabricLoader.getInstance().getModContainer(MOD_ID);
        if (o.isPresent()) {
            return o.get().getMetadata().getVersion().getFriendlyString();
        }
        return "0.0.0";
    }

    /**
     * Checks whether this is a development build.
     *
     * @return true if the mod version is the placeholder {@code "NONE"}
     */
    public static boolean isDevBuild() {
        return "NONE".equals(getVersion());
    }

    /**
     * Creates an identifier in the Silent Lib namespace.
     *
     * @param path the path part of the identifier
     * @return the identifier {@code silentlib:<path>}
     */
    public static Identifier getId(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
