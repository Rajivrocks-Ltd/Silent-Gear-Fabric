package net.silentchaos512.lib.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/**
 * A {@link DeferredHolder} for blocks that can also be used as an {@link ItemLike}.
 *
 * <p>Fabric port helper with the same methods as NeoForge's class of the same name.
 *
 * @param <T> the exact block type
 */
public class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> implements ItemLike {
    /**
     * Creates a holder for the given block key.
     *
     * @param key the block key
     */
    protected DeferredBlock(ResourceKey<Block> key) {
        super(key);
    }

    /**
     * Creates a holder for the block with the given id.
     *
     * @param key the block id
     * @param <T> the block type
     * @return a new holder
     */
    public static <T extends Block> DeferredBlock<T> createBlock(Identifier key) {
        return createBlock(ResourceKey.create(Registries.BLOCK, key));
    }

    /**
     * Creates a holder for the block with the given key.
     *
     * @param key the block key
     * @param <T> the block type
     * @return a new holder
     */
    public static <T extends Block> DeferredBlock<T> createBlock(ResourceKey<Block> key) {
        return new DeferredBlock<>(key);
    }

    /**
     * Creates a stack of one of this block's item.
     *
     * @return a new stack
     */
    public ItemStack toStack() {
        return toStack(1);
    }

    /**
     * Creates a stack of this block's item.
     *
     * @param count the stack size
     * @return a new stack
     */
    public ItemStack toStack(int count) {
        ItemStack stack = asItem().getDefaultInstance();
        if (stack.isEmpty()) {
            throw new IllegalStateException("Block does not have a corresponding item: " + key);
        }
        stack.setCount(count);
        return stack;
    }

    @Override
    public Item asItem() {
        return get().asItem();
    }
}
