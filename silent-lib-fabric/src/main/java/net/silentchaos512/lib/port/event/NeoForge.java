package net.silentchaos512.lib.port.event;

/**
 * Holds the shared game event bus.
 *
 * <p>Fabric port stand-in for NeoForge's {@code NeoForge} class, so that ported code can keep
 * calling {@code NeoForge.EVENT_BUS.post(...)} and {@code NeoForge.EVENT_BUS.addListener(...)}.
 */
public final class NeoForge {
    /** The game event bus. */
    public static final EventBus EVENT_BUS = new EventBus();

    private NeoForge() {}
}
