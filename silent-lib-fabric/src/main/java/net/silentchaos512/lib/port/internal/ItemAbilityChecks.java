package net.silentchaos512.lib.port.internal;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.silentchaos512.lib.port.ItemAbility;

/**
 * Shared code for the mixins that turn vanilla "is this exactly shears / a fishing rod" checks into
 * {@code canPerformAction} checks, as NeoForge's patches do.
 *
 * <p>Fabric port only. Vanilla calls {@code ItemStack.is(Item)}, which compiles to {@code is(Object)}. The
 * mixins wrap every such call in a method; only the calls for the given vanilla item are replaced, the
 * others run as before.
 */
public final class ItemAbilityChecks {
    private ItemAbilityChecks() {}

    /**
     * Replaces {@code stack.is(vanillaItem)} with an ability check.
     *
     * @param stack       the stack being checked
     * @param value       the argument vanilla passed to {@code is}
     * @param original    the wrapped {@code is} call
     * @param vanillaItem the item vanilla checks for (for example {@code Items.SHEARS})
     * @param ability     the ability NeoForge checks instead
     * @return the ability check for {@code vanillaItem}, otherwise the original result
     */
    public static boolean isOrCanPerform(ItemStack stack, Object value, Operation<Boolean> original, Item vanillaItem, ItemAbility ability) {
        if (value == vanillaItem) {
            return stack.getItem().canPerformAction(stack, ability);
        }
        return original.call(stack, value);
    }
}
