package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;
import net.silentchaos512.lib.port.event.NeoForge;
import net.silentchaos512.lib.port.event.furnace.FurnaceFuelBurnTimeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lets {@link FurnaceFuelBurnTimeEvent} decide how long a stack burns, as NeoForge does.
 *
 * <p>Fabric port only. Vanilla and Fabric's {@code FuelValueEvents} map items to fixed burn times; the event
 * sees the whole stack (Silent Gear uses it for gear with the Flammable trait).
 * <ul>
 *     <li>{@code burnDuration}: posts the event with vanilla's value and returns the event's value.</li>
 *     <li>{@code isFuel}: also true for stacks that are not vanilla fuels but get a burn time from the event,
 *     otherwise furnaces would not accept them.</li>
 * </ul>
 */
@Mixin(FuelValues.class)
public abstract class FuelValuesMixin {
    @Shadow
    public abstract int burnDuration(ItemStack itemStack);

    @ModifyReturnValue(method = "burnDuration", at = @At("RETURN"))
    private int silentlib$postBurnTime(int burnTime, @Local(argsOnly = true) ItemStack itemStack) {
        return NeoForge.EVENT_BUS.post(new FurnaceFuelBurnTimeEvent(itemStack, burnTime)).getBurnTime();
    }

    @ModifyReturnValue(method = "isFuel", at = @At("RETURN"))
    private boolean silentlib$fuelFromEvent(boolean isFuel, @Local(argsOnly = true) ItemStack itemStack) {
        return isFuel || (!itemStack.isEmpty() && this.burnDuration(itemStack) > 0);
    }
}
