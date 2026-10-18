package net.silentchaos512.lib.port;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A supplier that computes its value once, on first use, and then returns the same value.
 *
 * <p>Fabric port stand-in for NeoForge's {@code Lazy}, with the same methods.
 *
 * @param <T> the value type
 */
public final class Lazy<T> implements Supplier<T> {
    private @Nullable Supplier<T> supplier;
    private @Nullable T value;
    private boolean computed = false;

    private Lazy(Supplier<T> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    /**
     * Wraps a supplier so that it is called at most once.
     *
     * @param supplier computes the value
     * @param <T>      the value type
     * @return a new lazy value
     */
    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    /**
     * Gets the value, computing it on the first call.
     *
     * @return the value
     */
    @SuppressWarnings("DataFlowIssue")
    @Override
    public synchronized T get() {
        if (!computed) {
            value = Objects.requireNonNull(supplier).get();
            computed = true;
        }
        return value;
    }
}
