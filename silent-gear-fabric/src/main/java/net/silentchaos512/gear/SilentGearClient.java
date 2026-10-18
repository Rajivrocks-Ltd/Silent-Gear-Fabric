package net.silentchaos512.gear;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client-only entry point for Silent Gear on Fabric.
 *
 * <p>Upstream does client setup in {@code SideProxy.Client}, which is created from the
 * {@code @Mod} constructor. On Fabric that work moves here, because Fabric calls
 * {@link #onInitializeClient()} only on the physical client.
 *
 * <p>Milestone M0 skeleton: only logs a message.
 */
public final class SilentGearClient implements ClientModInitializer {
    /**
     * Called by Fabric Loader on the physical client, after {@link SilentGear#onInitialize()}.
     */
    @Override
    public void onInitializeClient() {
        SilentGear.LOGGER.info("Silent Gear (Fabric) client init");
    }
}
