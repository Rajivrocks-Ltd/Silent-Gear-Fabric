package net.silentchaos512.lib.port.event;

/**
 * The order in which listeners receive an event. Higher priorities run first.
 *
 * <p>Fabric port stand-in for NeoForge's enum of the same name.
 */
public enum EventPriority {
    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST
}
