package net.silentchaos512.lib.port.event.tick;

import net.minecraft.world.entity.player.Player;
import net.silentchaos512.lib.port.event.Event;

/**
 * Fired every tick for every player, on both sides.
 *
 * <p>Fabric: not fired yet. Needs a mixin at the start and end of {@code Player.tick}
 * (milestone M8).
 */
public abstract class PlayerTickEvent extends Event {
    private final Player player;

    protected PlayerTickEvent(Player player) {
        this.player = player;
    }

    public Player getEntity() {
        return player;
    }

    /** Fired before the player ticks. */
    public static class Pre extends PlayerTickEvent {
        public Pre(Player player) {
            super(player);
        }
    }

    /** Fired after the player ticks. */
    public static class Post extends PlayerTickEvent {
        public Post(Player player) {
            super(player);
        }
    }
}
