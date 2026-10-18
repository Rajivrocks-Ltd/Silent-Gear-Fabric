package net.silentchaos512.lib.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * A {@link DeferredHolder} for items that can also be used as an {@link ItemLike}.
 *
 * <p>Fabric port helper with the same methods as NeoForge's class of the same name.
 *
 * @param <T> the exact item type
 */
public class DeferredItem<T extends Item> extends DeferredHolder<Item, T> implements ItemLike {
    /**
     * Creates a holder for the given item key.
     *
     * @param key the item key
     */
    protected DeferredItem(ResourceKey<Item> key) {
        super(key);
    }

    /**
     * Creates a holder for the item with the given id.
     *
     * @param key the item id
     * @param <T> the item type
     * @return a new holder
     */
    public static <T extends Item> DeferredItem<T> createItem(Identifier key) {
        return createItem(ResourceKey.create(Registries.ITEM, key));
    }

    /**
     * Creates a holder for the item with the given key.
     *
     * @param key the item key
     * @param <T> the item type
     * @return a new holder
     */
    public static <T extends Item> DeferredItem<T> createItem(ResourceKey<Item> key) {
        return new DeferredItem<>(key);
    }

    /**
     * Creates a stack of one of this item.
     *
     * @return a new stack
     */
    public ItemStack toStack() {
        return toStack(1);
    }

    /**
     * Creates a stack of this item.
     *
     * @param count the stack size
     * @return a new stack
     */
    public ItemStack toStack(int count) {
        ItemStack stack = asItem().getDefaultInstance();
        if (stack.isEmpty()) {
            throw new IllegalStateException("Default instance of " + key + " is empty");
        }
        stack.setCount(count);
        return stack;
    }

    @Override
    public Item asItem() {
        return get();
    }
}
