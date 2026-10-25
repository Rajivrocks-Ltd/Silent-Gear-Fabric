package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.silentchaos512.lib.port.ItemAbilities;
import net.silentchaos512.lib.port.internal.ItemAbilityChecks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps a fishing hook alive while its owner holds any item with {@code fishing_rod_cast}, as NeoForge's
 * patch does.
 *
 * <p>Fabric port only. Vanilla checks for exactly {@code Items.FISHING_ROD} in both hands, so the hook of a
 * Silent Gear fishing rod would be removed right after it is cast.
 */
@Mixin(FishingHook.class)
public abstract class FishingHookMixin {
    @WrapOperation(method = "shouldStopFishing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
    private boolean silentlib$fishingRodAbility(ItemStack stack, Object item, Operation<Boolean> original) {
        return ItemAbilityChecks.isOrCanPerform(stack, item, original, Items.FISHING_ROD, ItemAbilities.FISHING_ROD_CAST);
    }
}
