package net.silentchaos512.gear;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

/**
 * Common entry point for Silent Gear on Fabric.
 *
 * <p>Port notes: upstream is a NeoForge {@code @Mod} class whose constructor receives the mod event
 * bus. Fabric creates this class with a no-argument constructor and calls {@link #onInitialize()}.
 * That method does what the upstream constructor did: create the side proxy (which registers all
 * content) and register the configs. Configs use Forge Config API Port, which keeps NeoForge's
 * {@code ModConfig} and {@code ModConfigSpec} classes.
 *
 * <p>Client-only setup that upstream did in {@code SideProxy.Client} through mod bus events now
 * lives in {@link SilentGearClient}.
 */
public final class SilentGear implements ModInitializer {
    public static final String MOD_ID = "silentgear";
    public static final String MOD_NAME = "Silent Gear";

    public static final String RESOURCE_PREFIX = MOD_ID + ":";

    public static final Random RANDOM = new Random();
    public static final RandomSource RANDOM_SOURCE = RandomSource.create();
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static SilentGear INSTANCE;
    public static IProxy PROXY;

    /**
     * Called by Fabric Loader once the game is ready for mods to register content.
     */
    @Override
    public void onInitialize() {
        INSTANCE = this;

        // Port note: registered before the proxy, unlike upstream's constructor. NeoForge loads
        // configs after all mod constructors but before it creates any registry entries, so entry
        // factories may read config values. Here the proxy creates the entries right away.
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, Config.Common.SPEC);
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, Config.Client.SPEC);
        ModConfigEvents.loading(MOD_ID).register(Config::onLoad);

        PROXY = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                ? new SideProxy.Client()
                : new SideProxy.Server();

        // Port note: upstream registers Curios event handlers here when Curios is loaded.
        // Curios does not exist on Fabric; the Trinkets integration replaces it in milestone M8.

        LOGGER.info("Silent Gear (Fabric) initialized");
    }

    public static String getVersion() {
        Optional<ModContainer> o = FabricLoader.getInstance().getModContainer(MOD_ID);
        if (o.isPresent()) {
            return o.get().getMetadata().getVersion().getFriendlyString();
        }
        return "0.0.0";
    }

    public static boolean isDevBuild() {
        return "NONE".equals(getVersion()) || FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static Identifier getId(String path) {
        if (path.contains(":")) {
            if (path.startsWith(SilentGear.MOD_ID)) {
                return Identifier.tryParse(path);
            } else {
                throw new IllegalArgumentException("path contains namespace other than " + SilentGear.MOD_ID);
            }
        }
        return Identifier.fromNamespaceAndPath(SilentGear.MOD_ID, path);
    }

    public static Identifier getIdWithModNamespaceAsDefault(String path) {
        if (path.contains(":")) {
            return Identifier.tryParse(path);
        }
        return Identifier.fromNamespaceAndPath(SilentGear.MOD_ID, path);
    }

    @Nullable
    public static Identifier getIdWithDefaultNamespace(String name) {
        if (name.contains(":"))
            return Identifier.tryParse(name);
        return Identifier.tryParse(RESOURCE_PREFIX + name);
    }

    public static String shortenId(@Nullable Identifier id) {
        if (id == null)
            return "null";
        if (MOD_ID.equals(id.getNamespace()))
            return id.getPath();
        return id.toString();
    }
}
