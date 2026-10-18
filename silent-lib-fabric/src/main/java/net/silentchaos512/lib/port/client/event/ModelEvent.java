package net.silentchaos512.lib.port.client.event;

/**
 * Model setup. Client only.
 *
 * <p>Fabric port stand-in for NeoForge's {@code ModelEvent}.
 */
public abstract class ModelEvent {
    /**
     * Would let client setup code load extra models that no block or item uses. Silent Gear's only
     * use of it is commented out upstream, so it has no methods yet. Fabric's model loading API
     * ({@code ModelLoadingPlugin}) is the equivalent if one is needed.
     */
    public static class RegisterStandalone extends ModelEvent {
    }
}
