package net.silentchaos512.lib.port;

import net.minecraft.world.item.Item;

/**
 * Extra {@link Item.Properties} methods that NeoForge adds, for the Fabric port.
 *
 * <p>Injected into {@link Item.Properties} by Silent Lib; the mixin provides the implementation.
 */
public interface IItemPropertiesExtension {
    /**
     * Marks the item so that two damaged copies cannot be combined into one in a crafting grid.
     * Read it back with {@link IItemExtension#isCombineRepairable(net.minecraft.world.item.ItemStack)}.
     *
     * @return these properties
     */
    default Item.Properties setNoCombineRepair() {
        throw new AssertionError("Implemented by Silent Lib's ItemPropertiesMixin");
    }
}
