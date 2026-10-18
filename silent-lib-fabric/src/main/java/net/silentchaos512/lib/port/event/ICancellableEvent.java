package net.silentchaos512.lib.port.event;

/**
 * An event that a listener can cancel. Once canceled, later listeners are skipped, and the code
 * that posted the event stops the action it describes.
 *
 * <p>Fabric port stand-in for NeoForge's interface of the same name.
 */
public interface ICancellableEvent {
    /**
     * Cancels or un-cancels the event.
     *
     * @param canceled true to cancel
     */
    void setCanceled(boolean canceled);

    /**
     * Checks whether the event was canceled.
     *
     * @return true if canceled
     */
    boolean isCanceled();
}
