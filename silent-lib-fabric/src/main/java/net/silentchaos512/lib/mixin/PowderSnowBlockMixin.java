package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Asks the worn boots whether they can walk on powder snow, as NeoForge's patch does.
 *
 * <p>Fabric port only. Vanilla checks for exactly leather boots; NeoForge calls
 * {@code IItemExtension.canWalkOnPowderedSnow}, whose default does the same check. Silent Gear boots with the
 * Snow Walker trait return true.
 */
@Mixin(PowderSnowBlock.class)
public abstract class PowderSnowBlockMixin {
    @WrapOperation(
            method = "canEntityWalkOnPowderSnow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z")
    )
    private static boolean silentlib$canWalkOnPowderedSnow(ItemStack stack, Object item, Operation<Boolean> original, @Local(argsOnly = true) Entity entity) {
        if (item == Items.LEATHER_BOOTS && entity instanceof LivingEntity wearer) {
            return stack.getItem().canWalkOnPowderedSnow(stack, wearer);
        }
        return original.call(stack, item);
    }
}
