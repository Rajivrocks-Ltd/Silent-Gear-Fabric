package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.silentchaos512.lib.port.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Makes the crafting player available to recipes while a crafting grid result is computed.
 *
 * <p>Fabric port only. NeoForge sets {@code CommonHooks.getCraftingPlayer()} around the recipe's
 * {@code assemble} call; this does the same for Silent Lib's {@link CommonHooks} stand-in. The method is
 * shared by the crafting table and the player inventory grid.
 */
// Note: assemble is declared on the generic Recipe<T>, so the compiled call takes RecipeInput.
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {
    @WrapOperation(
            method = "slotChangedCraftingGrid",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/CraftingRecipe;assemble(Lnet/minecraft/world/item/crafting/RecipeInput;)Lnet/minecraft/world/item/ItemStack;")
    )
    private static ItemStack silentlib$assembleWithCraftingPlayer(CraftingRecipe recipe, RecipeInput input, Operation<ItemStack> original, @Local(argsOnly = true) Player player) {
        CommonHooks.setCraftingPlayer(player);
        try {
            return original.call(recipe, input);
        } finally {
            CommonHooks.setCraftingPlayer(null);
        }
    }
}
