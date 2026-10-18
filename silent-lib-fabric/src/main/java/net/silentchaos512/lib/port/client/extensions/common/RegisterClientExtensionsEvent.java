package net.silentchaos512.lib.port.client.extensions.common;

import net.minecraft.world.level.ItemLike;

/**
 * Lets client setup code attach {@link IClientItemExtensions} to items. Client only.
 *
 * <p>Fabric port stand-in for NeoForge's event of the same name. Not posted on a bus: client init
 * creates one and passes it to the method that upstream subscribed to the NeoForge event.
 */
public class RegisterClientExtensionsEvent {
    /**
     * Attaches extensions to items. Items must already be registered.
     *
     * @param extensions the extensions
     * @param items      the items
     */
    public void registerItem(IClientItemExtensions extensions, ItemLike... items) {
        for (ItemLike item : items) {
            IClientItemExtensions.Registry.MAP.put(item.asItem(), extensions);
        }
    }
}
