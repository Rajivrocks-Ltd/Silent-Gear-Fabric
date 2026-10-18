package net.silentchaos512.lib.mixin;

import net.minecraft.world.item.Item;
import net.silentchaos512.lib.port.IItemPropertiesExtension;
import net.silentchaos512.lib.port.internal.NoCombineRepairFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds {@link IItemPropertiesExtension} to {@link Item.Properties} and stores its flag.
 *
 * <p>Fabric port only.
 */
@Mixin(Item.Properties.class)
public abstract class ItemPropertiesMixin implements IItemPropertiesExtension, NoCombineRepairFlag {
    @Unique
    private boolean silentlib$noCombineRepair = false;

    @Override
    public Item.Properties setNoCombineRepair() {
        this.silentlib$noCombineRepair = true;
        return (Item.Properties) (Object) this;
    }

    @Override
    public boolean silentlib$isNoCombineRepair() {
        return this.silentlib$noCombineRepair;
    }
}
