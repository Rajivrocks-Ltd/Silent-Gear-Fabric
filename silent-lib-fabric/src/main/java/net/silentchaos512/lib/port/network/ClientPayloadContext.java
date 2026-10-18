package net.silentchaos512.lib.port.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.world.entity.player.Player;

/**
 * Client-side {@link IPayloadContext}. Only load this class on the physical client.
 */
public final class ClientPayloadContext {
    private ClientPayloadContext() {}

    /**
     * Wraps the context Fabric gives to client-side handlers.
     *
     * @param context the Fabric context
     * @return the wrapped context
     */
    public static IPayloadContext of(ClientPlayNetworking.Context context) {
        return new IPayloadContext() {
            @Override
            public Player player() {
                return context.player();
            }

            @Override
            public PacketSender responseSender() {
                return context.responseSender();
            }
        };
    }
}
