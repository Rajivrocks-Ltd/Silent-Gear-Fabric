package net.silentchaos512.lib.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.silentchaos512.lib.port.IItemExtension;
import net.silentchaos512.lib.port.internal.NoCombineRepairFlag;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds {@link IItemExtension} to {@link Item} at runtime, and keeps the
 * {@link net.silentchaos512.lib.port.IItemPropertiesExtension#setNoCombineRepair()} flag.
 *
 * <p>Fabric port only. The compiler sees the interface through {@code silentlib.classtweaker}.
 */
@Mixin(Item.class)
public abstract class ItemMixin implements IItemExtension {
    @Unique
    private boolean silentlib$canCombineRepair = true;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void silentlib$readProperties(Item.Properties properties, CallbackInfo ci) {
        this.silentlib$canCombineRepair = !((NoCombineRepairFlag) properties).silentlib$isNoCombineRepair();
    }

    @Override
    public boolean isCombineRepairable(ItemStack stack) {
        return this.silentlib$canCombineRepair;
    }

    /**
     * Bridges Fabric's stack-aware crafting remainder ({@code FabricItem.getCraftingRemainder(ItemStack)},
     * which Fabric API already calls from crafting, furnaces and brewing) to
     * {@link IItemExtension#getCraftingRemainder(net.minecraft.world.item.ItemInstance)}, the NeoForge-style
     * method that ported items override.
     *
     * @param stack the stack being used up
     * @return what stays behind, or null for nothing
     */
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        return this.getCraftingRemainder((ItemInstance) stack);
    }

    /**
     * Bridges Fabric's held item update animation check ({@code FabricItem.allowComponentsUpdateAnimation},
     * which Fabric API calls from {@code ItemInHandRenderer.tick} when the stack in a hand changed but is still
     * the same item) to {@link IItemExtension#shouldCauseReequipAnimation}, the NeoForge-style method that ported
     * items override. Returning false shows the new stack without the hand lowering and rising again.
     *
     * <p>Port note: NeoForge also asks the item when the item changes or the selected slot changes. Fabric only
     * asks for the same item, and the default answers here match vanilla in the other cases. Silent Gear's
     * override only compares the item and its components and ignores the slot, so it behaves the same.
     *
     * @param player   the player holding the item
     * @param hand     the hand
     * @param oldStack the stack shown until now
     * @param newStack the stack now in the hand
     * @return true to play the animation
     */
    public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return this.shouldCauseReequipAnimation(oldStack, newStack, false);
    }
}
