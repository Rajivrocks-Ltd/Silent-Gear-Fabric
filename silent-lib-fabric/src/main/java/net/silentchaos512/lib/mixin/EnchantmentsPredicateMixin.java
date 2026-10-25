package net.silentchaos512.lib.mixin;

import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.silentchaos512.lib.port.CommonHooks;
import net.silentchaos512.lib.port.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

/**
 * Lets the {@code minecraft:enchantments} item predicate see enchantments added by
 * {@link net.silentchaos512.lib.port.event.enchanting.GetEnchantmentLevelEvent}, as NeoForge's patch does.
 * Block loot tables use this predicate for Silk Touch checks, for example.
 *
 * <p>Fabric port only. Adds the same {@code matches(DataComponentGetter)} override that NeoForge adds. Item
 * stacks are checked against their enchantments after the event. Anything else, or no registry, uses the
 * vanilla check.
 */
@Mixin(EnchantmentsPredicate.Enchantments.class)
public abstract class EnchantmentsPredicateMixin extends EnchantmentsPredicate {
    private EnchantmentsPredicateMixin(List<EnchantmentPredicate> enchantments) {
        super(enchantments);
    }

    @Override
    public boolean matches(DataComponentGetter components) {
        if (components instanceof ItemStack stack) {
            HolderLookup.RegistryLookup<Enchantment> lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
            if (lookup != null) {
                ItemEnchantments enchantments = stack.getOrDefault(this.componentType(), ItemEnchantments.EMPTY);
                return this.matches(EventHooks.getAllEnchantmentLevels(enchantments, stack, lookup));
            }
        }
        return super.matches(components);
    }
}
