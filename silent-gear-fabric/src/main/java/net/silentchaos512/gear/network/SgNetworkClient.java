package net.silentchaos512.gear.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.silentchaos512.gear.network.payload.server.CommandOutputPayload;
import net.silentchaos512.gear.network.payload.server.SyncMaterialsPayload;
import net.silentchaos512.gear.network.payload.server.SyncPartsPayload;
import net.silentchaos512.gear.network.payload.server.SyncTraitsPayload;
import net.silentchaos512.lib.port.network.ClientPayloadContext;

/**
 * Registers the client-side handlers for payloads sent from the server.
 *
 * <p>Fabric port only. Split out of {@link SgNetwork} so that client classes are never loaded on a
 * dedicated server. Called from {@link net.silentchaos512.gear.SilentGearClient}.
 */
public final class SgNetworkClient {
    private SgNetworkClient() {}

    /**
     * Registers the handlers for server to client payloads. Call once from client init.
     */
    public static void registerClientReceivers() {
        var handler = SgClientPayloadHandler.getInstance();
        ClientPlayNetworking.registerGlobalReceiver(SyncTraitsPayload.TYPE,
                (data, ctx) -> handler.handleSyncTraits(data, ClientPayloadContext.of(ctx)));
        ClientPlayNetworking.registerGlobalReceiver(SyncMaterialsPayload.TYPE,
                (data, ctx) -> handler.handleSyncMaterials(data, ClientPayloadContext.of(ctx)));
        ClientPlayNetworking.registerGlobalReceiver(SyncPartsPayload.TYPE,
                (data, ctx) -> handler.handleSyncParts(data, ClientPayloadContext.of(ctx)));
        ClientPlayNetworking.registerGlobalReceiver(CommandOutputPayload.TYPE,
                (data, ctx) -> handler.handleCommandOutput(data, ClientPayloadContext.of(ctx)));
    }
}
