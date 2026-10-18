package net.silentchaos512.lib.port.client.event;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.resources.Identifier;

/**
 * Color handler setup. Client only.
 *
 * <p>Fabric port stand-in for NeoForge's {@code RegisterColorHandlersEvent}.
 */
public abstract class RegisterColorHandlersEvent {
    /**
     * Lets client setup code register item tint source types, which item model JSON files refer to
     * by id. Not posted on a bus (see {@link RegisterKeyMappingsEvent}). Forwards to vanilla's
     * {@code ItemTintSources.ID_MAPPER}, which Fabric API makes accessible.
     */
    public static class ItemTintSources extends RegisterColorHandlersEvent {
        /**
         * Registers an item tint source type.
         *
         * @param id    the id used in item model JSON
         * @param codec reads the tint source from JSON
         */
        public void register(Identifier id, MapCodec<? extends ItemTintSource> codec) {
            net.minecraft.client.color.item.ItemTintSources.ID_MAPPER.put(id, codec);
        }
    }
}
