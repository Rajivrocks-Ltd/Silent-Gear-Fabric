package net.silentchaos512.lib.port.event;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.silentchaos512.lib.port.CommonHooks;
import net.silentchaos512.lib.port.event.enchanting.GetEnchantmentLevelEvent;

/**
 * Helpers that post events, like NeoForge's {@code EventHooks}. Only the ones the Silent Lib mixins need.
 *
 * <p>Fabric port stand-in.
 */
public final class EventHooks {
    private EventHooks() {}

    /**
     * Posts {@link GetEnchantmentLevelEvent} for a single enchantment and returns the (possibly changed) level.
     *
     * <p>Port note: NeoForge gets the registry lookup from {@code Holder.unwrapLookup()}, which vanilla does not
     * have. {@link CommonHooks#resolveLookup} gives the same registry.
     *
     * @param level the level the item has
     * @param stack the item being queried
     * @param ench  the enchantment being queried
     * @return the new level
     */
    public static int getEnchantmentLevelSpecific(int level, ItemInstance stack, Holder<Enchantment> ench) {
        HolderLookup.RegistryLookup<Enchantment> lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
        if (lookup == null) {
            return level;
        }

        var enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.set(ench, level);
        NeoForge.EVENT_BUS.post(new GetEnchantmentLevelEvent(stack, enchantments, ench, lookup));
        return enchantments.getLevel(ench);
    }

    /**
     * Posts {@link GetEnchantmentLevelEvent} for all enchantments and returns the (possibly changed) set.
     *
     * @param enchantments the enchantments the item has
     * @param stack        the item being queried
     * @param lookup       the enchantment registry
     * @return the new enchantments
     */
    public static ItemEnchantments getAllEnchantmentLevels(ItemEnchantments enchantments, ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        var mutableEnchantments = new ItemEnchantments.Mutable(enchantments);
        NeoForge.EVENT_BUS.post(new GetEnchantmentLevelEvent(stack, mutableEnchantments, null, lookup));
        return mutableEnchantments.toImmutable();
    }
}
