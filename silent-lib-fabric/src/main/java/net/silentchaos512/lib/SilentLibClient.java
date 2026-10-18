package net.silentchaos512.lib;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client-only entry point for Silent Lib on Fabric.
 *
 * <p>Fabric calls {@link #onInitializeClient()} after {@link SilentLib#onInitialize()},
 * and only on the physical client. Client setup that NeoForge did through mod bus events
 * goes here.
 *
 * <p>Milestone M0 skeleton: only logs a message.
 */
public final class SilentLibClient implements ClientModInitializer {
    /**
     * Called by Fabric Loader on the physical client.
     */
    @Override
    public void onInitializeClient() {
        SilentLib.LOGGER.info("Silent Lib (Fabric) client init");
    }
}
