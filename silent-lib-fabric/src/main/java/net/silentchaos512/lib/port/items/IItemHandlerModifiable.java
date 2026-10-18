package net.silentchaos512.lib.port.items;

import net.minecraft.world.item.ItemStack;

/**
 * An {@link IItemHandler} whose slots can be set directly. Provides the usual insert and extract
 * logic on top of {@link #setStackInSlot(int, ItemStack)}.
 *
 * <p>Fabric port stand-in for NeoForge's interface of the same name.
 */
public interface IItemHandlerModifiable extends IItemHandler {
    /**
     * Replaces the stack in a slot, without any checks.
     *
     * @param slot  the slot index
     * @param stack the new stack
     */
    void setStackInSlot(int slot, ItemStack stack);

    @Override
    default ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !isItemValid(slot, stack)) {
            return stack;
        }
        ItemStack existing = getStackInSlot(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing)) {
                return stack;
            }
            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return stack;
        }
        boolean reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                setStackInSlot(slot, stack.copyWithCount(reachedLimit ? limit : stack.getCount()));
            } else {
                setStackInSlot(slot, existing.copyWithCount(existing.getCount() + (reachedLimit ? limit : stack.getCount())));
            }
        }
        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    default ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack existing = getStackInSlot(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                setStackInSlot(slot, ItemStack.EMPTY);
            }
            return existing.copy();
        }
        if (!simulate) {
            setStackInSlot(slot, existing.copyWithCount(existing.getCount() - toExtract));
        }
        return existing.copyWithCount(toExtract);
    }

    @Override
    default int getSlotLimit(int slot) {
        return 99;
    }

    @Override
    default boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }
}
