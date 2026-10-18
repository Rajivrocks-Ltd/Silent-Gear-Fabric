package net.silentchaos512.lib.port.event;

/**
 * Base class of all events posted on {@link NeoForge#EVENT_BUS}.
 *
 * <p>Fabric port stand-in for NeoForge's {@code net.neoforged.bus.api.Event}. Event classes in this
 * package keep NeoForge's names, constructors and getters, so handler code ported from NeoForge
 * works unchanged. Silent Lib fires them from Fabric callbacks or from mixins; each class says where.
 */
public abstract class Event {
}
