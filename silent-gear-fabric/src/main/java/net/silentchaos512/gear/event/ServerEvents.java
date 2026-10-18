package net.silentchaos512.gear.event;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.silentchaos512.lib.port.event.EventPriority;
import net.silentchaos512.lib.port.event.NeoForge;
import net.silentchaos512.lib.port.event.OnDatapackSyncEvent;
import net.silentchaos512.lib.port.event.entity.player.PlayerEvent;
import net.silentchaos512.gear.network.payload.server.SyncMaterialsPayload;
import net.silentchaos512.gear.network.payload.server.SyncPartsPayload;
import net.silentchaos512.gear.network.payload.server.SyncTraitsPayload;
import net.silentchaos512.gear.setup.SgRegistries;

/**
 * Server-side handlers for data sync and player join.
 *
 * <p>Port notes: registered by {@link #register()} instead of {@code @EventBusSubscriber}. NeoForge's
 * {@code PacketDistributor.sendToPlayer(player, payloads...)} becomes one Fabric
 * {@code ServerPlayNetworking.send} per payload, in the same order.
 */
public final class ServerEvents {
    private ServerEvents() {}

    /**
     * Registers all handlers in this class. Called once from common init.
     */
    public static void register() {
        NeoForge.EVENT_BUS.addListener(OnDatapackSyncEvent.class, ServerEvents::onDataPackSync);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, PlayerEvent.PlayerLoggedInEvent.class, ServerEvents::onPlayerJoinServer);
    }

    public static void onDataPackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            ServerPlayer player = event.getPlayer();
            sendSyncPayloads(player);
        } else {
            event.getPlayerList().getPlayers().forEach(ServerEvents::sendSyncPayloads);
        }
    }

    private static void sendSyncPayloads(ServerPlayer player) {
        for (CustomPacketPayload payload : new CustomPacketPayload[]{new SyncTraitsPayload(), new SyncMaterialsPayload(), new SyncPartsPayload()}) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void onPlayerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        SgRegistries.TRAIT.getErrorMessages(serverPlayer).forEach(serverPlayer::sendSystemMessage);
        SgRegistries.MATERIAL.getErrorMessages(serverPlayer).forEach(serverPlayer::sendSystemMessage);
        SgRegistries.PART.getErrorMessages(serverPlayer).forEach(serverPlayer::sendSystemMessage);
    }
}
