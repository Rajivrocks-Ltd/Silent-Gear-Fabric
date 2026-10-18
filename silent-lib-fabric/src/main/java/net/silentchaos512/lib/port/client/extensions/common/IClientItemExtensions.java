package net.silentchaos512.lib.port.client.extensions.common;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side rendering hooks for an item. Client only.
 *
 * <p>Fabric port stand-in for the parts of NeoForge's interface of the same name that the Silent
 * mods use. Register implementations with {@link RegisterClientExtensionsEvent}. The defaults keep
 * vanilla behavior. Each method says which hook calls it.
 */
public interface IClientItemExtensions {
    /** Used for items without registered extensions. */
    IClientItemExtensions DEFAULT = new IClientItemExtensions() {};

    /**
     * Gets the extensions registered for an item.
     *
     * @param item the item
     * @return the extensions, or {@link #DEFAULT}
     */
    static IClientItemExtensions of(Item item) {
        return Registry.MAP.getOrDefault(item, DEFAULT);
    }

    /**
     * Gets the extensions registered for a stack's item.
     *
     * @param stack the stack
     * @return the extensions, or {@link #DEFAULT}
     */
    static IClientItemExtensions of(ItemStack stack) {
        return of(stack.getItem());
    }

    /**
     * Lets the item set the first-person hand pose itself. Return true to skip vanilla's transform.
     *
     * <p>Hook: not called by vanilla yet (milestone M9, {@code ItemInHandRenderer}).
     *
     * @param poseStack        the pose stack to transform
     * @param player           the local player
     * @param arm              the arm holding the item
     * @param itemInHand       the held stack
     * @param partialTick      the partial tick
     * @param equipProcess     the equip animation progress
     * @param swingProcess     the swing animation progress
     * @return true if the transform was applied
     */
    default boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        return false;
    }

    /**
     * Gets the tint color of an armor layer when the armor is worn.
     *
     * <p>Hook: not called by vanilla yet (milestone M9, equipment layer rendering).
     *
     * @param stack         the worn stack
     * @param layer         the equipment layer
     * @param layerIdx      the index of the layer
     * @param fallbackColor the color vanilla would use
     * @return the color as ARGB, or -1 for no tint
     */
    default int getArmorLayerTintColor(ItemStack stack, EquipmentClientInfo.Layer layer, int layerIdx, int fallbackColor) {
        return fallbackColor;
    }

    /**
     * Storage for registered extensions.
     */
    final class Registry {
        static final Map<Item, IClientItemExtensions> MAP = new ConcurrentHashMap<>();

        private Registry() {}
    }
}
