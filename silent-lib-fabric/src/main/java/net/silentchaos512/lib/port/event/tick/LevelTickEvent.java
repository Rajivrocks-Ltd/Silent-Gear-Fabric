package net.silentchaos512.lib.port.event.tick;

import net.minecraft.world.level.Level;
import net.silentchaos512.lib.port.event.Event;

/**
 * Fired every tick for every level.
 *
 * <p>Fabric: fired from {@code ServerTickEvents.START_WORLD_TICK} / {@code END_WORLD_TICK} for server
 * levels, and from {@code ClientTickEvents.START_LEVEL_TICK} / {@code END_LEVEL_TICK} for the client
 * level, like NeoForge which fires it on both sides.
 */
public abstract class LevelTickEvent extends Event {
    private final Level level;

    protected LevelTickEvent(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    /** Fired before the level ticks. */
    public static class Pre extends LevelTickEvent {
        public Pre(Level level) {
            super(level);
        }
    }

    /** Fired after the level ticks. */
    public static class Post extends LevelTickEvent {
        public Post(Level level) {
            super(level);
        }
    }
}
