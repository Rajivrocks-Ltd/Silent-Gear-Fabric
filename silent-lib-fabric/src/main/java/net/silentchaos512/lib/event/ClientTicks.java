package net.silentchaos512.lib.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.silentchaos512.lib.SilentLib;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Can schedule actions to run at the end of each client tick, which is mainly useful for
 * handling packets. Also tracks some tick-related variables useful for rendering.
 *
 * <p>Port notes:
 * <ul>
 *     <li>Upstream registers NeoForge listeners in the constructor. On Fabric the client tick API
 *     must never be touched on a dedicated server, and common code may load this class there
 *     (through {@link #scheduleAction(Runnable)}). So the listener is registered by
 *     {@link #init()}, which only {@link net.silentchaos512.lib.SilentLibClient} calls.</li>
 *     <li>Upstream updates {@code partialTicks} from NeoForge's {@code RenderFrameEvent.Pre}.
 *     Fabric has no start-of-frame event, so the value is read from vanilla's
 *     {@link net.minecraft.client.DeltaTracker} instead, which is the same source that event used.</li>
 * </ul>
 *
 * @since 2.3.12
 */
public final class ClientTicks {
    private static final ClientTicks INSTANCE = new ClientTicks();
    private static final int QUEUE_OVERFLOW_LIMIT = 200;

    @SuppressWarnings("FieldMayBeFinal")
    private volatile Queue<Runnable> scheduledActions = new ConcurrentLinkedDeque<>();

    public int ticksInGame = 0;
    public float partialTicks = 0f;
    public float deltaTicks = 0f;
    public float totalTicks = 0f;

    private ClientTicks() {}

    /**
     * Registers the client tick listener. Must only be called on the physical client, once,
     * from {@link net.silentchaos512.lib.SilentLibClient#onInitializeClient()}.
     */
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> INSTANCE.clientTickEnd());
    }

    public static void scheduleAction(Runnable action) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            INSTANCE.scheduledActions.add(action);
        else
            SilentLib.LOGGER.error("Tried to add client tick action on server side? {}", action);

        if (INSTANCE.scheduledActions.size() >= QUEUE_OVERFLOW_LIMIT) {
            // Queue overflow?
            SilentLib.LOGGER.warn("Too many client tick actions queued! Currently at {} items. Would have added '{}'.",
                    INSTANCE.scheduledActions.size(), action);
            SilentLib.LOGGER.error("ClientTicks queue overflow", new IllegalStateException("ClientTicks queue overflow"));
            INSTANCE.scheduledActions.clear();
        }
    }

    private void clientTickEnd() {
        runScheduledActions();
        // Stands in for upstream's per-frame update, which ran before this tick ended.
        updatePartialTicks();
        updateTickCounters();
    }

    /**
     * Copies the current partial tick from vanilla's delta tracker. Upstream did the same from
     * NeoForge's {@code RenderFrameEvent.Pre}.
     */
    private void updatePartialTicks() {
        partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
    }

    private void runScheduledActions() {
        Runnable action = scheduledActions.poll();
        while (action != null) {
            action.run();
            action = scheduledActions.poll();
        }
    }

    private void updateTickCounters() {
        Screen gui = Minecraft.getInstance().screen;
        if (gui == null || !gui.isPauseScreen()) {
            ++ticksInGame;
            partialTicks = 0;
        }

        float oldTotal = totalTicks;
        totalTicks = ticksInGame + partialTicks;
        deltaTicks = totalTicks - oldTotal;
    }

    public static int ticksInGame() {
        return INSTANCE.ticksInGame;
    }

    /**
     * Gets the partial tick for the frame being rendered.
     *
     * <p>Port note: upstream returns a field that NeoForge refreshed at the start of every frame.
     * Reading the delta tracker directly gives the same value without a mixin.
     *
     * @return the partial tick, between 0 and 1
     */
    public static float partialTicks() {
        // On a dedicated server the field is never updated and stays 0, like upstream.
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            INSTANCE.updatePartialTicks();
        }
        return INSTANCE.partialTicks;
    }

    public static float deltaTicks() {
        return INSTANCE.deltaTicks;
    }

    public static float totalTicks() {
        return INSTANCE.totalTicks;
    }
}
