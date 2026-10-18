package net.silentchaos512.lib.port;

import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * The parts of NeoForge's {@code CommonHooks} that Silent Gear uses.
 *
 * <p>Fabric port stand-in. NeoForge remembers which player is crafting while a crafting result is
 * computed, so recipes can read it. The same per-thread value is kept here. It is set by whoever
 * wraps the crafting call; until that is wired up, {@link #getCraftingPlayer()} returns null,
 * which recipes must already handle because NeoForge also returns null outside crafting.
 */
public final class CommonHooks {
    private static final ThreadLocal<@Nullable Player> CRAFTING_PLAYER = new ThreadLocal<>();

    private CommonHooks() {}

    /**
     * Gets the player who is crafting on this thread right now.
     *
     * @return the crafting player, or null if no crafting is in progress
     */
    public static @Nullable Player getCraftingPlayer() {
        return CRAFTING_PLAYER.get();
    }

    /**
     * Sets or clears the crafting player for this thread.
     *
     * @param player the crafting player, or null to clear it
     */
    public static void setCraftingPlayer(@Nullable Player player) {
        CRAFTING_PLAYER.set(player);
    }
}
