package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Stops the vanilla "combine two damaged items" crafting recipe from accepting items marked with
 * {@link net.silentchaos512.lib.port.IItemPropertiesExtension#setNoCombineRepair()}.
 *
 * <p>Fabric port only. NeoForge makes the same check. Without it, combining two Silent Gear tools
 * would produce a plain item and lose all of their parts and materials.
 */
@Mixin(RepairItemRecipe.class)
public abstract class RepairItemRecipeMixin {
    @ModifyReturnValue(method = "canCombine", at = @At("RETURN"))
    private static boolean silentlib$respectNoCombineRepair(boolean canCombine, @Local(argsOnly = true, ordinal = 0) ItemStack first, @Local(argsOnly = true, ordinal = 1) ItemStack second) {
        return canCombine
                && first.getItem().isCombineRepairable(first)
                && second.getItem().isCombineRepairable(second);
    }
}
