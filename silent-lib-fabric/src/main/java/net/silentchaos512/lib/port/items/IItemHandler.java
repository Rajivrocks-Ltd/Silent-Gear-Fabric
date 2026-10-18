package net.silentchaos512.lib.port.items;

import net.minecraft.world.item.ItemStack;

/**
 * A list of item slots that items can be put into and taken out of.
 *
 * <p>Fabric port stand-in for NeoForge's interface of the same name, with the same methods and
 * meaning. Stacks returned by {@link #getStackInSlot(int)} must not be changed by the caller.
 */
public interface IItemHandler {
    /**
     * Gets the number of slots.
     *
     * @return the slot count
     */
    int getSlots();

    /**
     * Gets the stack in a slot. Do not change the returned stack.
     *
     * @param slot the slot index
     * @return the stack, or {@link ItemStack#EMPTY}
     */
    ItemStack getStackInSlot(int slot);

    /**
     * Puts items into a slot.
     *
     * @param slot     the slot index
     * @param stack    the items to insert; not changed by this method
     * @param simulate true to only report what would happen
     * @return the items that did not fit
     */
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    /**
     * Takes items out of a slot.
     *
     * @param slot     the slot index
     * @param amount   the most items to take
     * @param simulate true to only report what would happen
     * @return the items taken
     */
    ItemStack extractItem(int slot, int amount, boolean simulate);

    /**
     * Gets the most items a slot can hold.
     *
     * @param slot the slot index
     * @return the limit
     */
    int getSlotLimit(int slot);

    /**
     * Checks whether a stack may go into a slot.
     *
     * @param slot  the slot index
     * @param stack the stack
     * @return true if allowed
     */
    boolean isItemValid(int slot, ItemStack stack);
}
