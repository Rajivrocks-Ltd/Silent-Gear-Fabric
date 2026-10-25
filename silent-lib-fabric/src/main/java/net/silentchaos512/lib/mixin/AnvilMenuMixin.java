package net.silentchaos512.lib.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.silentchaos512.lib.port.event.AnvilUpdateEvent;
import net.silentchaos512.lib.port.event.NeoForge;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Posts {@link AnvilUpdateEvent} after the anvil computes its result, as NeoForge does.
 *
 * <p>Fabric port only. NeoForge renames vanilla's {@code createResult} body and posts the event after it on
 * every path. Here the event is posted at each return of {@code createResult}, which is the same point.
 * When the left slot is empty the event is not posted. A canceled event clears the result and both costs;
 * otherwise the event's output and costs replace vanilla's.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow
    private int repairItemCountCost;
    @Shadow
    private @Nullable String itemName;
    @Shadow
    @Final
    private DataSlot cost;

    private AnvilMenuMixin(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition itemInputSlots) {
        super(menuType, containerId, inventory, access, itemInputSlots);
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void silentlib$postAnvilUpdate(CallbackInfo ci) {
        ItemStack left = this.inputSlots.getItem(0);
        if (left.isEmpty()) {
            return;
        }

        ItemStack right = this.inputSlots.getItem(1);
        var event = new AnvilUpdateEvent(left, right, this.itemName, this.resultSlots.getItem(0), this.cost.get(), this.repairItemCountCost, this.player);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
            this.repairItemCountCost = 0;
            return;
        }

        this.resultSlots.setItem(0, event.getOutput());
        this.cost.set(event.getXpCost());
        this.repairItemCountCost = event.getMaterialCost();
    }
}
