package net.silentchaos512.lib.port.items;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A menu slot that shows one slot of an {@link IItemHandler}.
 *
 * <p>Fabric port stand-in for NeoForge's class of the same name. The handler is wrapped in a small
 * vanilla {@link Container}, so all of vanilla's {@link Slot} logic (shift-click, splitting, and so
 * on) works unchanged. The handler must be an {@link IItemHandlerModifiable}.
 */
public class SlotItemHandler extends Slot {
    private final IItemHandler itemHandler;
    private final int index;

    public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(new HandlerContainer(itemHandler), index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() && itemHandler.isItemValid(index, stack);
    }

    @Override
    public int getMaxStackSize() {
        return itemHandler.getSlotLimit(index);
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    /**
     * Presents an item handler as a vanilla container.
     */
    private record HandlerContainer(IItemHandler handler) implements Container {
        private IItemHandlerModifiable modifiable() {
            if (handler instanceof IItemHandlerModifiable m) return m;
            throw new UnsupportedOperationException("SlotItemHandler needs an IItemHandlerModifiable");
        }

        @Override
        public int getContainerSize() {
            return handler.getSlots();
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < handler.getSlots(); ++i) {
                if (!handler.getStackInSlot(i).isEmpty()) return false;
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return handler.getStackInSlot(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return handler.extractItem(slot, amount, false);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = handler.getStackInSlot(slot);
            modifiable().setStackInSlot(slot, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            modifiable().setStackInSlot(slot, stack);
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < handler.getSlots(); ++i) {
                modifiable().setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }
}
