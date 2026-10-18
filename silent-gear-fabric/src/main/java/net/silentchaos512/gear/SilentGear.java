package net.silentchaos512.gear;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Common entry point for Silent Gear on Fabric.
 *
 * <p>Replaces the NeoForge {@code @Mod} class of the same name. Fabric creates this class
 * through its no-argument constructor and then calls {@link #onInitialize()} on both the
 * client and the dedicated server.
 *
 * <p>Milestone M0 skeleton: only logs a message. The rest of the upstream class is ported
 * in milestone M4.
 */
public final class SilentGear implements ModInitializer {
    /** The mod id, kept identical to upstream so data packs stay compatible. */
    public static final String MOD_ID = "silentgear";
    /** Human readable mod name, used for the logger. */
    public static final String MOD_NAME = "Silent Gear";

    /** Logger for all Silent Gear messages. */
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    /**
     * Called by Fabric Loader once the game is ready for mods to register content.
     */
    @Override
    public void onInitialize() {
        LOGGER.info("Silent Gear (Fabric) common init, trinkets loaded: {}",
                FabricLoader.getInstance().isModLoaded("trinkets"));
    }

    /**
     * Creates an identifier in the Silent Gear namespace.
     *
     * @param path the path part of the identifier
     * @return the identifier {@code silentgear:<path>}
     */
    public static Identifier getId(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
