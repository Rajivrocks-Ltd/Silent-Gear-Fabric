package net.silentchaos512.gear.gear.util;

import net.silentchaos512.lib.port.event.NeoForge;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.silentchaos512.lib.port.event.tick.LevelTickEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MagnetPullTracker {
    /**
     * Registers the level tick handler. Called once from common init.
     * Port note: replaces upstream's @EventBusSubscriber.
     */
    public static void register() {
        NeoForge.EVENT_BUS.addListener(LevelTickEvent.Post.class, MagnetPullTracker::onLevelTick);
    }

    private static final Map<ItemEntity, Vec3> ITEMS_TO_PUSH = new ConcurrentHashMap<>();

    public static void pushItem(ItemEntity item, Vec3 force) {
        synchronized (ITEMS_TO_PUSH) {
            var currentForce = ITEMS_TO_PUSH.getOrDefault(item, Vec3.ZERO);
            if (force.lengthSqr() > currentForce.lengthSqr()) {
                ITEMS_TO_PUSH.put(item, force);
            }
        }
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        synchronized (ITEMS_TO_PUSH) {
            ITEMS_TO_PUSH.forEach((item, force) -> {
                item.push(force.x, force.y, force.z);
            });
            ITEMS_TO_PUSH.clear();
        }
    }
}
