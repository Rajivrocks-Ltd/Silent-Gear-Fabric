package net.silentchaos512.lib.port.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

/**
 * Fired on the server when data pack contents must be sent to clients: when a player joins
 * ({@link #getPlayer()} is that player) or after {@code /reload} ({@link #getPlayer()} is null,
 * meaning all players).
 *
 * <p>Fabric: fired from {@code ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS}. Fabric calls that
 * once per player, also after {@code /reload}, so on Fabric {@link #getPlayer()} is never null.
 * Listeners written for NeoForge handle both cases, so they work unchanged.
 */
public class OnDatapackSyncEvent extends Event {
    private final PlayerList playerList;
    private final @Nullable ServerPlayer player;

    public OnDatapackSyncEvent(PlayerList playerList, @Nullable ServerPlayer player) {
        this.playerList = playerList;
        this.player = player;
    }

    public PlayerList getPlayerList() {
        return playerList;
    }

    /**
     * Gets the players that need the data.
     *
     * @return the one player, or all players
     */
    public Stream<ServerPlayer> getRelevantPlayers() {
        return player != null ? Stream.of(player) : playerList.getPlayers().stream();
    }

    /**
     * Gets the player that joined.
     *
     * @return the player, or null when syncing to all players
     */
    public @Nullable ServerPlayer getPlayer() {
        return player;
    }
}
