package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.silentchaos512.lib.port.ItemAbilities;
import net.silentchaos512.lib.port.internal.ItemAbilityChecks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Turns the two shears checks in {@code Entity.interact} into ability checks, as NeoForge's patch does:
 * cutting leashes ({@code shears_harvest}) and shearing equipment off a mob ({@code shears_remove_armor}).
 *
 * <p>Fabric port only. The method checks {@code is(Items.SHEARS)} twice, then {@code is(Items.LEAD)};
 * ordinals 0 and 1 are the two shears checks.
 */
@Mixin(Entity.class)
public abstract class EntityShearsMixin {
    @WrapOperation(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean silentlib$shearsHarvest(ItemStack stack, Object item, Operation<Boolean> original) {
        return ItemAbilityChecks.isOrCanPerform(stack, item, original, Items.SHEARS, ItemAbilities.SHEARS_HARVEST);
    }

    @WrapOperation(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z", ordinal = 1))
    private boolean silentlib$shearsRemoveArmor(ItemStack stack, Object item, Operation<Boolean> original) {
        return ItemAbilityChecks.isOrCanPerform(stack, item, original, Items.SHEARS, ItemAbilities.SHEARS_REMOVE_ARMOR);
    }
}
