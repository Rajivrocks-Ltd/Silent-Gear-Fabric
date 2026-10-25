package net.silentchaos512.lib.port.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.silentchaos512.lib.port.CommonHooks;
import net.silentchaos512.lib.port.event.entity.living.LivingDamageEvent;
import net.silentchaos512.lib.port.event.entity.living.LivingDeathEvent;
import net.silentchaos512.lib.port.event.entity.player.PlayerEvent;
import net.silentchaos512.lib.port.event.level.block.BreakBlockEvent;
import net.silentchaos512.lib.port.event.tick.LevelTickEvent;

/**
 * Posts events on {@link NeoForge#EVENT_BUS} from the Fabric API callbacks that match them.
 *
 * <p>Fabric port only. Events without a matching Fabric callback are posted by Silent Lib mixins
 * instead, or not yet; see the javadoc of each event class.
 */
public final class FabricEventBridge {
    private static boolean initialized = false;

    private FabricEventBridge() {}

    /**
     * Registers the bridges for common and server events. Called once from Silent Lib's common init.
     */
    public static void init() {
        if (initialized) return;
        initialized = true;

        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
                !NeoForge.EVENT_BUS.post(new BreakBlockEvent(level, pos, state, player)).isCanceled());

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) ->
                !NeoForge.EVENT_BUS.post(new LivingDeathEvent(entity, source)).isCanceled());

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) ->
                NeoForge.EVENT_BUS.post(new LivingDamageEvent.Post(entity, source, baseDamageTaken, damageTaken)));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(handler.getPlayer())));

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) ->
                NeoForge.EVENT_BUS.post(new OnDatapackSyncEvent(player.level().getServer().getPlayerList(), player)));

        ServerLifecycleEvents.SERVER_STARTING.register(CommonHooks::setCurrentServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> CommonHooks.setCurrentServer(null));

        ServerTickEvents.START_LEVEL_TICK.register(level ->
                NeoForge.EVENT_BUS.post(new LevelTickEvent.Pre(level)));
        ServerTickEvents.END_LEVEL_TICK.register(level ->
                NeoForge.EVENT_BUS.post(new LevelTickEvent.Post(level)));
    }
}
