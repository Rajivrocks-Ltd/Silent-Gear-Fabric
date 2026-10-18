package net.silentchaos512.lib.port.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.CompletableFuture;

/**
 * What a payload handler needs to know about the payload it is handling.
 *
 * <p>Fabric port stand-in for the parts of NeoForge's {@code IPayloadContext} that Silent Gear
 * uses. Fabric already calls play payload handlers on the main game thread, so
 * {@link #enqueueWork(Runnable)} runs the task right away.
 */
public interface IPayloadContext {
    /**
     * Gets the player on this side of the connection: the sender on the server, the local
     * player on the client.
     *
     * @return the player
     */
    Player player();

    /**
     * Gets the object used to send replies on the same connection.
     *
     * @return the packet sender
     */
    PacketSender responseSender();

    /**
     * Runs a task on the main game thread. Handlers already run there on Fabric, so the task
     * runs immediately.
     *
     * @param task the task
     * @return a future that completes when the task is done, or fails with its exception
     */
    default CompletableFuture<Void> enqueueWork(Runnable task) {
        try {
            task.run();
            return CompletableFuture.completedFuture(null);
        } catch (Throwable t) {
            return CompletableFuture.failedFuture(t);
        }
    }

    /**
     * Sends a payload back to the other side.
     *
     * @param payload the payload
     */
    default void reply(CustomPacketPayload payload) {
        responseSender().sendPacket(payload);
    }

    /**
     * Closes the connection.
     *
     * @param reason the message shown to the player
     */
    default void disconnect(Component reason) {
        responseSender().disconnect(reason);
    }

    /**
     * Wraps the context Fabric gives to server-side handlers.
     *
     * @param context the Fabric context
     * @return the wrapped context
     */
    static IPayloadContext server(ServerPlayNetworking.Context context) {
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
