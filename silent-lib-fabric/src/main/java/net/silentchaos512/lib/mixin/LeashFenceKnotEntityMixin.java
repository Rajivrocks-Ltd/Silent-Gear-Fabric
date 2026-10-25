package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.silentchaos512.lib.port.ItemAbilities;
import net.silentchaos512.lib.port.internal.ItemAbilityChecks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lets any item with {@code shears_harvest} cut the leashes on a fence knot, as NeoForge's patch does.
 *
 * <p>Fabric port only.
 */
@Mixin(LeashFenceKnotEntity.class)
public abstract class LeashFenceKnotEntityMixin {
    @WrapOperation(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
    private boolean silentlib$shearsAbility(ItemStack stack, Object item, Operation<Boolean> original) {
        return ItemAbilityChecks.isOrCanPerform(stack, item, original, Items.SHEARS, ItemAbilities.SHEARS_HARVEST);
    }
}
