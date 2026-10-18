package net.silentchaos512.lib.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.silentchaos512.lib.port.IItemExtension;
import net.silentchaos512.lib.port.internal.NoCombineRepairFlag;
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
}
