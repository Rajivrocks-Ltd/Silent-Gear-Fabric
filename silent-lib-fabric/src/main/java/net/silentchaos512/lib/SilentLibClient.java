package net.silentchaos512.lib;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.lib.port.CommonHooks;
import net.silentchaos512.lib.port.event.FabricClientEventBridge;

/**
 * Client-only entry point for Silent Lib on Fabric.
 *
 * <p>Fabric calls {@link #onInitializeClient()} after {@link SilentLib#onInitialize()},
 * and only on the physical client. Client setup that NeoForge did through mod bus events
 * or client-side event listeners goes here.
 */
public final class SilentLibClient implements ClientModInitializer {
    /**
     * Called by Fabric Loader on the physical client.
     */
    @Override
    public void onInitializeClient() {
        FabricClientEventBridge.init();
        ClientTicks.init();
        CommonHooks.setClientRegistryAccess(() -> {
            var level = Minecraft.getInstance().level;
            return level != null ? level.registryAccess() : null;
        });

        SilentLib.LOGGER.info("Silent Lib (Fabric) client initialized");
    }
}
