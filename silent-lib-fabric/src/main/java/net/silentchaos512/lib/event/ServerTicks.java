package net.silentchaos512.lib.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.silentchaos512.lib.SilentLib;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Can schedule actions to run at the start of each server tick, which is mainly useful for
 * handling packets.
 *
 * <p>Port note: upstream listens to NeoForge's {@code ServerTickEvent.Pre}. Here the listener is
 * {@link ServerTickEvents#START_SERVER_TICK}, registered by {@link #init()}.
 *
 * @since 2.3.12
 */
public final class ServerTicks {
    private static final ServerTicks INSTANCE = new ServerTicks();
    private static final int QUEUE_OVERFLOW_LIMIT = 200;

    @SuppressWarnings("FieldMayBeFinal")
    private volatile Queue<Runnable> scheduledActions = new ConcurrentLinkedDeque<>();

    private ServerTicks() {}

    /**
     * Registers the server tick listener. Called once from {@link SilentLib#onInitialize()}.
     */
    public static void init() {
        ServerTickEvents.START_SERVER_TICK.register(server -> INSTANCE.serverTicks());
    }

    public static void scheduleAction(Runnable action) {
        // In SSP, this is still considered client side, so we can't check the side?
        INSTANCE.scheduledActions.add(action);

        if (INSTANCE.scheduledActions.size() > QUEUE_OVERFLOW_LIMIT) {
            SilentLib.LOGGER.warn("Too many server tick actions queued! Currently at {} items. Would have added '{}'.",
                    INSTANCE.scheduledActions.size(), action);
            SilentLib.LOGGER.error("ServerTicks queue overflow", new IllegalStateException("ServerTicks queue overflow"));
            INSTANCE.scheduledActions.clear();
        }
    }

    private void serverTicks() {
        runScheduledActions();
    }

    private void runScheduledActions() {
        Runnable action = scheduledActions.poll();
        while (action != null) {
            action.run();
            action = scheduledActions.poll();
        }
    }
}
