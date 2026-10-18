package net.silentchaos512.lib.port.items;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/**
 * An item handler backed by a list of stacks.
 *
 * <p>Fabric port stand-in for NeoForge's class of the same name.
 */
public class ItemStackHandler implements IItemHandlerModifiable {
    protected NonNullList<ItemStack> stacks;

    public ItemStackHandler() {
        this(1);
    }

    public ItemStackHandler(int size) {
        this(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    public ItemStackHandler(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return stacks.get(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    /**
     * Called after a slot changes. Override to mark a block entity dirty.
     *
     * @param slot the slot index
     */
    protected void onContentsChanged(int slot) {
    }
}
