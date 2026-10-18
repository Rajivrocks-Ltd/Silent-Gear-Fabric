package net.silentchaos512.lib.port.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Fired on the server when an entity is about to die. Canceling it keeps the entity alive.
 *
 * <p>Fabric: fired from {@code ServerLivingEntityEvents.ALLOW_DEATH}.
 */
public class LivingDeathEvent extends LivingEvent {
    private final DamageSource source;

    public LivingDeathEvent(LivingEntity entity, DamageSource source) {
        super(entity);
        this.source = source;
    }

    public DamageSource getSource() {
        return source;
    }
}
