package net.silentchaos512.lib;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

/**
 * Common entry point for Silent Lib on Fabric.
 *
 * <p>Replaces the NeoForge {@code @Mod} class of the same name. Fabric creates this class
 * through its no-argument constructor and then calls {@link #onInitialize()} on both the
 * client and the dedicated server.
 *
 * <p>Milestone M0 skeleton: only logs a message. Registration is ported in milestone M2.
 */
public final class SilentLib implements ModInitializer {
    /** The mod id, shared with the upstream NeoForge version. */
    public static final String MOD_ID = "silentlib";
    /** Logger for all Silent Lib messages. */
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Called by Fabric Loader once the game is ready for mods to register content.
     */
    @Override
    public void onInitialize() {
        LOGGER.info("Silent Lib (Fabric) common init");
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
