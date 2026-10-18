package net.silentchaos512.lib.registry;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A reference to a registry entry that may not be registered yet.
 *
 * <p>Fabric port helper. It offers the same methods as NeoForge's class of the same name, so code
 * ported from NeoForge keeps working with only an import change. It is a {@link Holder} of the
 * registry type {@code R}, and a {@link Supplier} of the exact entry type {@code T}.
 *
 * <p>The holder looks up the real registry entry the first time it is used, and caches it.
 * Using it before the entry is registered throws an exception, like NeoForge.
 *
 * @param <R> the registry type, for example {@code Item}
 * @param <T> the exact type of this entry, for example {@code SwordItem}
 */
public class DeferredHolder<R, T extends R> implements Holder<R>, Supplier<T> {
    /** The key of the registry and of this entry. */
    protected final ResourceKey<R> key;
    /** The real registry holder, found on first use. */
    private @Nullable Holder<R> holder;

    /**
     * Creates a holder for the given entry key.
     *
     * @param key the key of the entry, which also names its registry
     */
    protected DeferredHolder(ResourceKey<R> key) {
        this.key = Objects.requireNonNull(key);
    }

    /**
     * Creates a holder for an entry in the given registry.
     *
     * @param registryKey the registry
     * @param valueName   the entry id
     * @param <R>         the registry type
     * @param <T>         the entry type
     * @return a new holder
     */
    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey, Identifier valueName) {
        return create(ResourceKey.create(registryKey, valueName));
    }

    /**
     * Creates a holder for an entry with the given key.
     *
     * @param key the entry key
     * @param <R> the registry type
     * @param <T> the entry type
     * @return a new holder
     */
    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<R> key) {
        return new DeferredHolder<>(key);
    }

    /**
     * Gets the registered value.
     *
     * @return the value
     * @throws IllegalStateException if the entry is not registered yet
     */
    @SuppressWarnings("unchecked")
    @Override
    public T value() {
        return (T) getDelegate().value();
    }

    /**
     * Gets the registered value. Same as {@link #value()}.
     *
     * @return the value
     * @throws IllegalStateException if the entry is not registered yet
     */
    @Override
    public T get() {
        return value();
    }

    /**
     * Gets the value if it is registered.
     *
     * @return the value, or empty if not registered yet
     */
    public Optional<T> asOptional() {
        return isBound() ? Optional.of(value()) : Optional.empty();
    }

    /**
     * Gets the id of this entry.
     *
     * @return the entry id
     */
    public Identifier getId() {
        return key.identifier();
    }

    /**
     * Gets the key of this entry.
     *
     * @return the entry key
     */
    public ResourceKey<R> getKey() {
        return key;
    }

    /**
     * Gets the real registry holder, looking it up on first use.
     *
     * @return the registry holder
     * @throws IllegalStateException if the registry or the entry does not exist yet
     */
    public Holder<R> getDelegate() {
        Holder<R> h = tryBind();
        if (h == null) {
            throw new IllegalStateException("Registry entry is not registered yet: " + key);
        }
        return h;
    }

    /**
     * Looks up the registry holder without throwing.
     *
     * @return the registry holder, or null if it is not available yet
     */
    @SuppressWarnings("unchecked")
    private @Nullable Holder<R> tryBind() {
        if (holder == null) {
            Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.getValue(key.registry());
            if (registry != null) {
                holder = registry.get(key).orElse(null);
            }
        }
        return holder;
    }

    @Override
    public boolean isBound() {
        Holder<R> h = tryBind();
        return h != null && h.isBound();
    }

    @Override
    public boolean areComponentsBound() {
        return getDelegate().areComponentsBound();
    }

    @Override
    public DataComponentMap components() {
        return getDelegate().components();
    }

    @Override
    public boolean is(Identifier id) {
        return key.identifier().equals(id);
    }

    @Override
    public boolean is(ResourceKey<R> resourceKey) {
        return key.equals(resourceKey);
    }

    @Override
    public boolean is(Predicate<ResourceKey<R>> filter) {
        return filter.test(key);
    }

    @Override
    public boolean is(TagKey<R> tag) {
        return isBound() && getDelegate().is(tag);
    }

    @Override
    public boolean is(Holder<R> other) {
        return other.is(key);
    }

    @Override
    public Stream<TagKey<R>> tags() {
        return isBound() ? getDelegate().tags() : Stream.empty();
    }

    @Override
    public Either<ResourceKey<R>, R> unwrap() {
        return Either.left(key);
    }

    @Override
    public Optional<ResourceKey<R>> unwrapKey() {
        return Optional.of(key);
    }

    @Override
    public Kind kind() {
        return Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<R> owner) {
        return getDelegate().canSerializeIn(owner);
    }

    /**
     * Two holders are equal when they refer to the same registry entry, including vanilla
     * reference holders for the same key.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof Holder<?> other
                && other.kind() == Kind.REFERENCE
                && other.unwrapKey().equals(Optional.of(key));
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "DeferredHolder{" + key + "}";
    }
}
