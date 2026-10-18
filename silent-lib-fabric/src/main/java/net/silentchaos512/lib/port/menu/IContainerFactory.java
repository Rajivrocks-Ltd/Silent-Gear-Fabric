package net.silentchaos512.lib.port.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Creates a menu on the client from the extra data the server wrote when it opened the menu.
 *
 * <p>Fabric port stand-in for NeoForge's interface of the same name. Use with
 * {@link IMenuTypeExtension#create(IContainerFactory)}.
 *
 * @param <T> the menu type
 */
@FunctionalInterface
public interface IContainerFactory<T extends AbstractContainerMenu> {
    /**
     * Creates the menu.
     *
     * @param windowId the container id
     * @param inv      the player inventory
     * @param data     the extra data written by the server
     * @return the menu
     */
    T create(int windowId, Inventory inv, RegistryFriendlyByteBuf data);
}
