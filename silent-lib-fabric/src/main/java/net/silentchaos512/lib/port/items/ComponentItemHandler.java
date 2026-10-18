package net.silentchaos512.lib.port.items;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * An item handler that stores its items in an {@link ItemContainerContents} data component on
 * another item stack, such as a bag or book item.
 *
 * <p>Fabric port stand-in for NeoForge's class of the same name. Every change writes a new
 * component value to the parent stack, since component values must not be changed in place.
 */
public class ComponentItemHandler implements IItemHandlerModifiable {
    protected final ItemStack parent;
    protected final DataComponentType<ItemContainerContents> component;
    protected final int size;

    /**
     * Creates a handler for the items stored on a stack.
     *
     * @param parent    the stack that holds the items
     * @param component the component the items are stored in
     * @param size      the number of slots
     */
    public ComponentItemHandler(ItemStack parent, DataComponentType<ItemContainerContents> component, int size) {
        this.parent = parent;
        this.component = component;
        this.size = size;
    }

    @Override
    public int getSlots() {
        return size;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return readItems().get(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        NonNullList<ItemStack> items = readItems();
        items.set(slot, stack.copy());
        parent.set(component, ItemContainerContents.fromItems(items));
        onContentsChanged(slot);
    }

    /**
     * Called after a slot changes.
     *
     * @param slot the slot index
     */
    protected void onContentsChanged(int slot) {
    }

    /**
     * Copies the stored items into a list with exactly {@link #size} slots.
     */
    private NonNullList<ItemStack> readItems() {
        NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
        parent.getOrDefault(component, ItemContainerContents.EMPTY).copyInto(items);
        return items;
    }
}
