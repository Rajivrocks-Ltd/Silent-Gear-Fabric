package net.silentchaos512.lib.port;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * The parts of NeoForge's {@code CommonHooks} that Silent Lib and Silent Gear use.
 *
 * <p>Fabric port stand-in.
 * <ul>
 *     <li>Crafting player: NeoForge remembers which player is crafting while a crafting result is computed,
 *     so recipes can read it. The same per-thread value is kept here, set by {@code CraftingMenuMixin} and
 *     {@code ResultSlotMixin}. Outside crafting it is null, as in NeoForge.</li>
 *     <li>{@link #resolveLookup}: finds a registry without a level or server at hand. The running server is
 *     tracked by {@code FabricEventBridge}; on the client, {@code SilentLibClient} adds the client level as a
 *     fallback, as NeoForge's client proxy does.</li>
 * </ul>
 */
public final class CommonHooks {
    private static final ThreadLocal<@Nullable Player> CRAFTING_PLAYER = new ThreadLocal<>();
    private static volatile @Nullable MinecraftServer currentServer;
    private static volatile Supplier<@Nullable RegistryAccess> clientRegistryAccess = () -> null;

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

    /**
     * Gets a registry lookup from the running server, or from the client level when no server runs in this
     * game instance. Same behavior as NeoForge's {@code CommonHooks.resolveLookup}.
     *
     * @param key the registry key
     * @param <T> the registry element type
     * @return the lookup, or null if there is no server or client level, or the registry does not exist
     */
    public static <T> HolderLookup.@Nullable RegistryLookup<T> resolveLookup(ResourceKey<? extends Registry<T>> key) {
        MinecraftServer server = currentServer;
        RegistryAccess access = server != null ? server.registryAccess() : clientRegistryAccess.get();
        return access != null ? access.lookup(key).orElse(null) : null;
    }

    /**
     * Sets or clears the running server. Fabric port only; called by {@code FabricEventBridge}.
     *
     * @param server the server that is starting, or null when it has stopped
     */
    public static void setCurrentServer(@Nullable MinecraftServer server) {
        currentServer = server;
    }

    /**
     * Sets where {@link #resolveLookup} gets registries on the client. Fabric port only; called by
     * {@code SilentLibClient}.
     *
     * @param supplier gives the client level's registries, or null when no world is loaded
     */
    public static void setClientRegistryAccess(Supplier<@Nullable RegistryAccess> supplier) {
        clientRegistryAccess = supplier;
    }
}
