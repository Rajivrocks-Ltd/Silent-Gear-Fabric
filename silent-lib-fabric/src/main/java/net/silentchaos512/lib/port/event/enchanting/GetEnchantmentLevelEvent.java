package net.silentchaos512.lib.port.event.enchanting;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.silentchaos512.lib.port.event.Event;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Fired when the game looks up enchantment levels on an item. Listeners can add or change levels
 * in {@link #getEnchantments()}; the change only affects this lookup, not the item.
 *
 * <p>Fabric: posted by {@code EnchantmentHelperMixin} and {@code EnchantmentsPredicateMixin}, from the same
 * places NeoForge posts it.
 */
public class GetEnchantmentLevelEvent extends Event {
    private final ItemInstance stack;
    private final ItemEnchantments.Mutable enchantments;
    private final @Nullable Holder<Enchantment> targetEnchant;
    private final HolderLookup.RegistryLookup<Enchantment> lookup;

    public GetEnchantmentLevelEvent(ItemInstance stack, ItemEnchantments.Mutable enchantments, @Nullable Holder<Enchantment> targetEnchant, HolderLookup.RegistryLookup<Enchantment> lookup) {
        this.stack = stack;
        this.enchantments = enchantments;
        this.targetEnchant = targetEnchant;
        this.lookup = lookup;
    }

    public ItemInstance getStack() {
        return stack;
    }

    public ItemEnchantments.Mutable getEnchantments() {
        return enchantments;
    }

    /**
     * Gets the enchantment being looked up, or null when all enchantments are requested.
     *
     * @return the target enchantment, or null
     */
    public @Nullable Holder<Enchantment> getTargetEnchant() {
        return targetEnchant;
    }

    /**
     * Checks whether the lookup asks for the given enchantment (or for all of them).
     *
     * @param ench the enchantment
     * @return true if listeners should provide a level for it
     */
    public boolean isTargetting(Holder<Enchantment> ench) {
        return targetEnchant == null || targetEnchant.is(ench);
    }

    /**
     * Checks whether the lookup asks for the given enchantment (or for all of them).
     *
     * @param ench the enchantment key
     * @return true if listeners should provide a level for it
     */
    public boolean isTargetting(ResourceKey<Enchantment> ench) {
        return targetEnchant == null || targetEnchant.is(ench);
    }

    public Optional<Holder.Reference<Enchantment>> getHolder(ResourceKey<Enchantment> key) {
        return lookup.get(key);
    }

    public HolderLookup.RegistryLookup<Enchantment> getLookup() {
        return lookup;
    }
}
