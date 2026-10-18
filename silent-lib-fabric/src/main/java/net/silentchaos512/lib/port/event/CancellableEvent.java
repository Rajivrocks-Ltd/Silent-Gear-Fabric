package net.silentchaos512.lib.port.event;

/**
 * Convenience base class for events that can be canceled.
 */
public abstract class CancellableEvent extends Event implements ICancellableEvent {
    private boolean canceled = false;

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }
}
