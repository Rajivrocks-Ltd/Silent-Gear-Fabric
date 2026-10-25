package net.silentchaos512.lib.port.event.furnace;

import net.minecraft.world.item.ItemStack;
import net.silentchaos512.lib.port.event.CancellableEvent;

/**
 * Fired when a furnace asks how long an item stack burns. Listeners can change the time;
 * 0 means it is not a fuel.
 *
 * <p>Fabric: posted by {@code FuelValuesMixin} from {@code FuelValues.burnDuration}, with the vanilla time as the
 * starting value. Fabric's {@code FuelValueEvents} only maps items to fixed times, while this event sees the
 * whole stack.
 */
public class FurnaceFuelBurnTimeEvent extends CancellableEvent {
    private final ItemStack itemStack;
    private int burnTime;

    public FurnaceFuelBurnTimeEvent(ItemStack itemStack, int burnTime) {
        this.itemStack = itemStack;
        this.burnTime = burnTime;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = burnTime;
        setCanceled(true);
    }
}
