package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.silentchaos512.lib.port.CommonHooks;
import net.silentchaos512.lib.port.event.NeoForge;
import net.silentchaos512.lib.port.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Crafting hooks on the crafting result slot, matching what NeoForge does there.
 *
 * <p>Fabric port only.
 * <ul>
 *     <li>Posts {@link PlayerEvent.ItemCraftedEvent} when the player takes the result, right after
 *     vanilla's {@code onCraftedBy} call.</li>
 *     <li>Makes the crafting player available through {@link CommonHooks#getCraftingPlayer()} while the
 *     remaining items are computed.</li>
 * </ul>
 */
@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {
    @Shadow
    @Final
    private Player player;

    @Shadow
    @Final
    private CraftingContainer craftSlots;

    @Inject(
            method = "checkTakeAchievements",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;onCraftedBy(Lnet/minecraft/world/entity/player/Player;I)V", shift = At.Shift.AFTER)
    )
    private void silentlib$postItemCrafted(ItemStack carried, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(this.player, carried, this.craftSlots));
    }

    @WrapOperation(
            method = "onTake",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultSlot;getRemainingItems(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Lnet/minecraft/core/NonNullList;")
    )
    private NonNullList<ItemStack> silentlib$remainingItemsWithCraftingPlayer(ResultSlot self, CraftingInput input, Level level, Operation<NonNullList<ItemStack>> original, @Local(argsOnly = true) Player player) {
        CommonHooks.setCraftingPlayer(player);
        try {
            return original.call(self, input, level);
        } finally {
            CommonHooks.setCraftingPlayer(null);
        }
    }
}
