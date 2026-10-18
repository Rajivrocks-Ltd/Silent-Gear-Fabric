package net.silentchaos512.lib.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Collects registry entries and registers them all at once when {@link #register()} is called.
 *
 * <p>Fabric port helper. It offers the same methods as NeoForge's class of the same name, so code
 * ported from NeoForge keeps working with only an import change. The one difference: NeoForge
 * registers the entries when it fires its registration event for the mod event bus
 * ({@code register(IEventBus)}). On Fabric the mod calls {@link #register()} itself during
 * {@code onInitialize}, so the mod controls the order between registries.
 *
 * <p>Entry factories run inside {@link #register()}, in the order the entries were added. A factory
 * may use holders from registers that were registered earlier.
 *
 * @param <T> the registry type
 */
public class DeferredRegister<T> {
    /** The registry the entries go into. */
    protected final ResourceKey<? extends Registry<T>> registryKey;
    /** The namespace for all entry ids, usually the mod id. */
    protected final String namespace;
    /** Entries in the order they were added. */
    private final Map<DeferredHolder<T, ? extends T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    /** True once {@link #register()} has run. */
    private boolean registered = false;

    /**
     * Creates a register for the given registry and namespace.
     *
     * @param registryKey the registry
     * @param namespace   the namespace for entry ids
     */
    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        this.registryKey = Objects.requireNonNull(registryKey);
        this.namespace = Objects.requireNonNull(namespace);
    }

    /**
     * Creates a register for an existing registry.
     *
     * @param registry  the registry
     * @param namespace the namespace for entry ids
     * @param <T>       the registry type
     * @return a new register
     */
    public static <T> DeferredRegister<T> create(Registry<T> registry, String namespace) {
        return new DeferredRegister<>(registry.key(), namespace);
    }

    /**
     * Creates a register for the registry with the given key.
     *
     * @param key       the registry key
     * @param namespace the namespace for entry ids
     * @param <T>       the registry type
     * @return a new register
     */
    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String namespace) {
        return new DeferredRegister<>(key, namespace);
    }

    /**
     * Creates a register for items, with item helper methods.
     *
     * @param namespace the namespace for entry ids
     * @return a new item register
     */
    public static Items createItems(String namespace) {
        return new Items(namespace);
    }

    /**
     * Creates a register for blocks, with block helper methods.
     *
     * @param namespace the namespace for entry ids
     * @return a new block register
     */
    public static Blocks createBlocks(String namespace) {
        return new Blocks(namespace);
    }

    /**
     * Creates a register for data component types.
     *
     * @param registryKey the data component type registry key
     * @param namespace   the namespace for entry ids
     * @return a new data component register
     */
    public static DataComponents createDataComponents(ResourceKey<Registry<DataComponentType<?>>> registryKey, String namespace) {
        return new DataComponents(registryKey, namespace);
    }

    /**
     * Adds an entry.
     *
     * @param name the entry name, without namespace
     * @param sup  creates the entry; called once, in {@link #register()}
     * @param <I>  the exact entry type
     * @return a holder for the entry
     */
    public <I extends T> DeferredHolder<T, I> register(final String name, final Supplier<? extends I> sup) {
        return this.register(name, key -> sup.get());
    }

    /**
     * Adds an entry whose factory needs the entry id.
     *
     * @param name the entry name, without namespace
     * @param func creates the entry from its id; called once, in {@link #register()}
     * @param <I>  the exact entry type
     * @return a holder for the entry
     */
    public <I extends T> DeferredHolder<T, I> register(final String name, final Function<Identifier, ? extends I> func) {
        if (registered) {
            throw new IllegalStateException("Cannot add entries to a DeferredRegister after register() was called: " + name);
        }
        Objects.requireNonNull(name);
        Objects.requireNonNull(func);
        final Identifier id = Identifier.fromNamespaceAndPath(namespace, name);
        DeferredHolder<T, I> holder = createHolder(registryKey, id);
        if (entries.putIfAbsent(holder, () -> func.apply(id)) != null) {
            throw new IllegalArgumentException("Duplicate registration " + id);
        }
        return holder;
    }

    /**
     * Creates the holder object for a new entry. Subclasses return more specific holder types.
     *
     * @param registryKey the registry key
     * @param id          the entry id
     * @param <I>         the exact entry type
     * @return a new holder
     */
    protected <I extends T> DeferredHolder<T, I> createHolder(ResourceKey<? extends Registry<T>> registryKey, Identifier id) {
        return DeferredHolder.create(ResourceKey.create(registryKey, id));
    }

    /**
     * Creates every entry and adds it to the registry. Call this exactly once, during mod
     * initialization, while registries are still open.
     *
     * @throws IllegalStateException if called twice, or if the registry does not exist
     */
    public void register() {
        if (registered) {
            throw new IllegalStateException("DeferredRegister for " + registryKey.identifier() + " was already registered");
        }
        registered = true;
        Registry<T> registry = getRegistry();
        entries.forEach((holder, factory) -> Registry.register(registry, holder.getKey(), factory.get()));
    }

    /**
     * Gets the registry this register writes to.
     *
     * @return the registry
     * @throws IllegalStateException if the registry does not exist
     */
    @SuppressWarnings("unchecked")
    public Registry<T> getRegistry() {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
        if (registry == null) {
            throw new IllegalStateException("Registry does not exist: " + registryKey.identifier());
        }
        return registry;
    }

    /**
     * Creates a tag key in this register's registry.
     *
     * @param path the tag path, in this register's namespace
     * @return the tag key
     */
    public TagKey<T> createTagKey(String path) {
        return createTagKey(Identifier.fromNamespaceAndPath(namespace, path));
    }

    /**
     * Creates a tag key in this register's registry.
     *
     * @param id the tag id
     * @return the tag key
     */
    public TagKey<T> createTagKey(Identifier id) {
        return TagKey.create(registryKey, id);
    }

    /**
     * Gets all holders added to this register, in the order they were added.
     *
     * @return an unmodifiable view of the holders
     */
    public Collection<DeferredHolder<T, ? extends T>> getEntries() {
        return Collections.unmodifiableSet(entries.keySet());
    }

    /**
     * Gets the registry key.
     *
     * @return the registry key
     */
    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return registryKey;
    }

    /**
     * Gets the registry id.
     *
     * @return the registry id
     */
    public Identifier getRegistryName() {
        return registryKey.identifier();
    }

    /**
     * Gets the namespace used for entry ids.
     *
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * A register for blocks. Its helpers set the block id on the properties, which 26.x requires.
     */
    public static class Blocks extends DeferredRegister<Block> {
        /**
         * Creates a block register.
         *
         * @param namespace the namespace for entry ids
         */
        protected Blocks(String namespace) {
            super(Registries.BLOCK, namespace);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <B extends Block> DeferredBlock<B> register(String name, Function<Identifier, ? extends B> func) {
            return (DeferredBlock<B>) super.register(name, func);
        }

        @Override
        public <B extends Block> DeferredBlock<B> register(String name, Supplier<? extends B> sup) {
            return this.register(name, key -> sup.get());
        }

        /**
         * Adds a block made from properties that already carry the block id.
         *
         * @param name       the block name
         * @param func       creates the block from its properties
         * @param properties supplies fresh properties
         * @param <B>        the block type
         * @return a holder for the block
         */
        public <B extends Block> DeferredBlock<B> registerBlock(String name, Function<BlockBehaviour.Properties, ? extends B> func, Supplier<BlockBehaviour.Properties> properties) {
            return this.register(name, key -> func.apply(properties.get().setId(ResourceKey.create(Registries.BLOCK, key))));
        }

        /**
         * Adds a block made from default properties changed by the given operator.
         *
         * @param name       the block name
         * @param func       creates the block from its properties
         * @param properties changes the default properties
         * @param <B>        the block type
         * @return a holder for the block
         */
        public <B extends Block> DeferredBlock<B> registerBlock(String name, Function<BlockBehaviour.Properties, ? extends B> func, UnaryOperator<BlockBehaviour.Properties> properties) {
            return this.registerBlock(name, func, () -> properties.apply(BlockBehaviour.Properties.of()));
        }

        /**
         * Adds a block made from default properties.
         *
         * @param name the block name
         * @param func creates the block from its properties
         * @param <B>  the block type
         * @return a holder for the block
         */
        public <B extends Block> DeferredBlock<B> registerBlock(String name, Function<BlockBehaviour.Properties, ? extends B> func) {
            return this.registerBlock(name, func, UnaryOperator.identity());
        }

        /**
         * Adds a plain {@link Block}.
         *
         * @param name       the block name
         * @param properties changes the default properties
         * @return a holder for the block
         */
        public DeferredBlock<Block> registerSimpleBlock(String name, UnaryOperator<BlockBehaviour.Properties> properties) {
            return this.registerBlock(name, Block::new, properties);
        }

        /**
         * Adds a plain {@link Block} with default properties.
         *
         * @param name the block name
         * @return a holder for the block
         */
        public DeferredBlock<Block> registerSimpleBlock(String name) {
            return this.registerSimpleBlock(name, UnaryOperator.identity());
        }

        @Override
        protected <I extends Block> DeferredHolder<Block, I> createHolder(ResourceKey<? extends Registry<Block>> registryKey, Identifier id) {
            return DeferredBlock.createBlock(ResourceKey.create(registryKey, id));
        }
    }

    /**
     * A register for items. Its helpers set the item id on the properties, which 26.x requires.
     */
    public static class Items extends DeferredRegister<Item> {
        /**
         * Creates an item register.
         *
         * @param namespace the namespace for entry ids
         */
        protected Items(String namespace) {
            super(Registries.ITEM, namespace);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <I extends Item> DeferredItem<I> register(String name, Function<Identifier, ? extends I> func) {
            return (DeferredItem<I>) super.register(name, func);
        }

        @Override
        public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> sup) {
            return this.register(name, key -> sup.get());
        }

        /**
         * Adds an item made from properties that already carry the item id.
         *
         * @param name       the item name
         * @param func       creates the item from its properties
         * @param properties supplies fresh properties
         * @param <I>        the item type
         * @return a holder for the item
         */
        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, ? extends I> func, Supplier<Item.Properties> properties) {
            return this.register(name, key -> func.apply(properties.get().setId(ResourceKey.create(Registries.ITEM, key))));
        }

        /**
         * Adds an item made from default properties changed by the given operator.
         *
         * @param name       the item name
         * @param func       creates the item from its properties
         * @param properties changes the default properties
         * @param <I>        the item type
         * @return a holder for the item
         */
        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, ? extends I> func, UnaryOperator<Item.Properties> properties) {
            return this.registerItem(name, func, () -> properties.apply(new Item.Properties()));
        }

        /**
         * Adds an item made from default properties.
         *
         * @param name the item name
         * @param func creates the item from its properties
         * @param <I>  the item type
         * @return a holder for the item
         */
        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, ? extends I> func) {
            return this.registerItem(name, func, UnaryOperator.identity());
        }

        /**
         * Adds a plain {@link Item}.
         *
         * @param name       the item name
         * @param properties changes the default properties
         * @return a holder for the item
         */
        public DeferredItem<Item> registerSimpleItem(String name, UnaryOperator<Item.Properties> properties) {
            return this.registerItem(name, Item::new, properties);
        }

        /**
         * Adds a plain {@link Item} with default properties.
         *
         * @param name the item name
         * @return a holder for the item
         */
        public DeferredItem<Item> registerSimpleItem(String name) {
            return this.registerSimpleItem(name, UnaryOperator.identity());
        }

        /**
         * Adds a {@link BlockItem} for a block, with the same name as the block.
         *
         * @param block      the block
         * @param properties changes the default properties
         * @return a holder for the item
         */
        public DeferredItem<BlockItem> registerSimpleBlockItem(Holder<Block> block, UnaryOperator<Item.Properties> properties) {
            String name = block.unwrapKey().orElseThrow().identifier().getPath();
            return this.registerItem(name, p -> new BlockItem(block.value(), p.useBlockDescriptionPrefix()), properties);
        }

        /**
         * Adds a {@link BlockItem} for a block, with the same name as the block and default properties.
         *
         * @param block the block
         * @return a holder for the item
         */
        public DeferredItem<BlockItem> registerSimpleBlockItem(Holder<Block> block) {
            return this.registerSimpleBlockItem(block, UnaryOperator.identity());
        }

        @Override
        protected <I extends Item> DeferredHolder<Item, I> createHolder(ResourceKey<? extends Registry<Item>> registryKey, Identifier id) {
            return DeferredItem.createItem(ResourceKey.create(registryKey, id));
        }
    }

    /**
     * A register for data component types.
     *
     * <p>Unlike the other registers, {@link #registerComponentType} creates and registers the type
     * right away and returns the type itself, not a holder. NeoForge lets code pass a holder to
     * {@code ItemStack.get(...)}; vanilla only accepts the type. Returning the type keeps those
     * call sites unchanged.
     */
    public static class DataComponents extends DeferredRegister<DataComponentType<?>> {
        /**
         * Creates a data component register.
         *
         * @param registryKey the data component type registry key
         * @param namespace   the namespace for entry ids
         */
        protected DataComponents(ResourceKey<Registry<DataComponentType<?>>> registryKey, String namespace) {
            super(registryKey, namespace);
        }

        /**
         * Builds a data component type and registers it immediately.
         *
         * @param name    the component name
         * @param builder configures a new builder
         * @param <D>     the component value type
         * @return the registered component type
         */
        public <D> DataComponentType<D> registerComponentType(String name, UnaryOperator<DataComponentType.Builder<D>> builder) {
            return Registry.register(
                    BuiltInRegistries.DATA_COMPONENT_TYPE,
                    Identifier.fromNamespaceAndPath(namespace, name),
                    builder.apply(DataComponentType.builder()).build()
            );
        }

        /**
         * Does nothing. Component types are registered when they are declared. Kept so that code
         * calling {@code register()} on every register needs no special case.
         */
        @Override
        public void register() {
        }
    }
}
