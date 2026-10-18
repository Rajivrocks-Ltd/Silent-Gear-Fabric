package net.silentchaos512.lib.port.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.silentchaos512.lib.port.event.CancellableEvent;

/**
 * Base class for events about a living entity.
 *
 * <p>Fabric port stand-in. Unlike NeoForge, it carries the cancel flag for all subclasses; only
 * subclasses whose javadoc says they can be canceled act on it.
 */
public abstract class LivingEvent extends CancellableEvent {
    private final LivingEntity entity;

    protected LivingEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }
}
