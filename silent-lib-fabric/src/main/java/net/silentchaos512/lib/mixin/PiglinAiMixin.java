package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Asks the worn item whether it makes piglins neutral, as NeoForge's patch does.
 *
 * <p>Fabric port only. Vanilla checks the {@code piglin_safe_armor} tag; NeoForge calls
 * {@code IItemExtension.makesPiglinsNeutral}, whose default does the same tag check. Silent Gear armor with
 * the Brilliant trait returns true.
 */
@Mixin(PiglinAi.class)
public abstract class PiglinAiMixin {
    @WrapOperation(
            method = "isWearingSafeArmor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z")
    )
    private static boolean silentlib$makesPiglinsNeutral(ItemStack stack, TagKey<Item> tag, Operation<Boolean> original, @Local(argsOnly = true) LivingEntity wearer) {
        return stack.getItem().makesPiglinsNeutral(stack, wearer);
    }
}
