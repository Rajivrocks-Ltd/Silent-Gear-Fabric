package net.silentchaos512.lib.port.event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A simple event bus that sends each posted event to the listeners registered for its class or
 * for any of its superclasses.
 *
 * <p>Fabric port stand-in for the parts of NeoForge's {@code IEventBus} that the Silent mods use.
 * NeoForge finds the event class from the listener's parameter type; here it is passed explicitly.
 * Listeners run in priority order, then in registration order. A canceled event is not passed to
 * the remaining listeners, like NeoForge's default.
 */
public final class EventBus {
    private final Map<Class<?>, List<Listener<?>>> listeners = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Listener<?>>> resolved = new ConcurrentHashMap<>();
    private long nextOrder = 0;

    /**
     * Registers a listener with normal priority.
     *
     * @param eventType the event class to listen for, including its subclasses
     * @param listener  the listener
     * @param <T>       the event type
     */
    public <T extends Event> void addListener(Class<T> eventType, Consumer<T> listener) {
        addListener(EventPriority.NORMAL, eventType, listener);
    }

    /**
     * Registers a listener with the given priority.
     *
     * @param priority  when the listener runs relative to others
     * @param eventType the event class to listen for, including its subclasses
     * @param listener  the listener
     * @param <T>       the event type
     */
    public synchronized <T extends Event> void addListener(EventPriority priority, Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(new Listener<>(priority, nextOrder++, eventType, listener));
        resolved.clear();
    }

    /**
     * Sends an event to every matching listener.
     *
     * @param event the event
     * @param <T>   the event type
     * @return the same event, so callers can read results set by listeners
     */
    public <T extends Event> T post(T event) {
        for (Listener<?> listener : resolve(event.getClass())) {
            if (event instanceof ICancellableEvent c && c.isCanceled()) {
                break;
            }
            listener.accept(event);
        }
        return event;
    }

    /**
     * Collects, sorts and caches the listeners for an event class and its superclasses.
     */
    private List<Listener<?>> resolve(Class<?> eventClass) {
        return resolved.computeIfAbsent(eventClass, cls -> {
            List<Listener<?>> all = new ArrayList<>();
            for (Class<?> c = cls; c != null && Event.class.isAssignableFrom(c); c = c.getSuperclass()) {
                all.addAll(listeners.getOrDefault(c, List.of()));
            }
            all.sort(Comparator.<Listener<?>>comparingInt(l -> l.priority.ordinal()).thenComparingLong(l -> l.order));
            return List.copyOf(all);
        });
    }

    /**
     * One registered listener.
     */
    private record Listener<T>(EventPriority priority, long order, Class<T> type, Consumer<T> consumer) {
        void accept(Object event) {
            consumer.accept(type.cast(event));
        }
    }
}
