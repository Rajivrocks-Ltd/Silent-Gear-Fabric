package net.silentchaos512.gear.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.silentchaos512.gear.network.payload.client.*;
import net.silentchaos512.gear.network.payload.server.*;
import net.silentchaos512.lib.port.network.IPayloadContext;

/**
 * Registers Silent Gear's network payloads.
 *
 * <p>Port notes: upstream registers payload types and handlers together in NeoForge's
 * {@code RegisterPayloadHandlersEvent}. Fabric splits this into three steps:
 * <ol>
 *     <li>{@link #registerPayloadTypes()}: payload ids and codecs, on both sides.</li>
 *     <li>{@link #registerServerReceivers()}: handlers for client to server payloads, on both sides
 *     (the integrated server runs on the client too).</li>
 *     <li>{@link SgNetworkClient#registerClientReceivers()}: handlers for server to client
 *     payloads, on the client only.</li>
 * </ol>
 * The payload records, codecs and handler methods are unchanged from upstream.
 *
 * <p>The three data sync payloads are registered as large payloads. NeoForge splits payloads
 * over vanilla's size limit automatically; Fabric does that only for payloads registered with
 * {@code registerLarge}, and large data packs can produce big material lists.
 */
public final class SgNetwork {
    /** Size limit for the data sync payloads. Fabric splits them into parts on the wire. */
    private static final int MAX_SYNC_PAYLOAD_SIZE = 64 * 1024 * 1024;

    private SgNetwork() {}

    /**
     * Registers the id and codec of every payload. Call once from common init.
     */
    public static void registerPayloadTypes() {
        var toClient = PayloadTypeRegistry.clientboundPlay();
        var toServer = PayloadTypeRegistry.serverboundPlay();

        // Data resource sync packets
        toClient.registerLarge(SyncTraitsPayload.TYPE, SyncTraitsPayload.STREAM_CODEC, MAX_SYNC_PAYLOAD_SIZE);
        toClient.registerLarge(SyncMaterialsPayload.TYPE, SyncMaterialsPayload.STREAM_CODEC, MAX_SYNC_PAYLOAD_SIZE);
        toClient.registerLarge(SyncPartsPayload.TYPE, SyncPartsPayload.STREAM_CODEC, MAX_SYNC_PAYLOAD_SIZE);
        toServer.register(AckPayload.TYPE, StreamCodec.unit(new AckPayload()));

        // Play phase server 2 client packets
        toClient.register(CommandOutputPayload.TYPE, CommandOutputPayload.STREAM_CODEC);

        // Play phase client 2 server play packets
        toServer.register(ToggleWorkModePayload.TYPE, ToggleWorkModePayload.STREAM_CODEC);
        toServer.register(SwingGearPayload.TYPE, StreamCodec.unit(new SwingGearPayload()));
        toServer.register(KeyPressOnItemPayload.TYPE, KeyPressOnItemPayload.STREAM_CODEC);
        toServer.register(RecalculateStatsPayload.TYPE, RecalculateStatsPayload.STREAM_CODEC);
        toServer.register(SelectBlueprintInBookPayload.TYPE, SelectBlueprintInBookPayload.STREAM_CODEC);
    }

    /**
     * Registers the handlers for payloads sent from clients to the server. Call once from common init.
     */
    public static void registerServerReceivers() {
        var handler = SgServerPayloadHandler.getInstance();
        ServerPlayNetworking.registerGlobalReceiver(AckPayload.TYPE,
                (data, ctx) -> handler.handleAck(data, IPayloadContext.server(ctx)));
        ServerPlayNetworking.registerGlobalReceiver(ToggleWorkModePayload.TYPE,
                (data, ctx) -> handler.handleWorkModeTogglePayload(data, IPayloadContext.server(ctx)));
        ServerPlayNetworking.registerGlobalReceiver(SwingGearPayload.TYPE,
                (data, ctx) -> handler.handleSwingGear(data, IPayloadContext.server(ctx)));
        ServerPlayNetworking.registerGlobalReceiver(KeyPressOnItemPayload.TYPE,
                (data, ctx) -> handler.handleKeyPressOnItem(data, IPayloadContext.server(ctx)));
        ServerPlayNetworking.registerGlobalReceiver(RecalculateStatsPayload.TYPE,
                (data, ctx) -> handler.handleRecalculateStats(data, IPayloadContext.server(ctx)));
        ServerPlayNetworking.registerGlobalReceiver(SelectBlueprintInBookPayload.TYPE,
                (data, ctx) -> handler.handleSelectBlueprintInBook(data, IPayloadContext.server(ctx)));
    }
}
