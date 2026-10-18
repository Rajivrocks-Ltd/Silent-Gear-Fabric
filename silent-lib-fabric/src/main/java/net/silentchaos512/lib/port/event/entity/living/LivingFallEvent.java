package net.silentchaos512.lib.port.event.entity.living;

import net.minecraft.world.entity.LivingEntity;

/**
 * Fired when an entity lands after a fall. Listeners can change the distance and damage
 * multiplier. Canceling it means no fall damage.
 *
 * <p>Fabric: not fired yet. Needs a mixin at the start of {@code LivingEntity.causeFallDamage}
 * (milestone M8).
 */
public class LivingFallEvent extends LivingEvent {
    private double distance;
    private float damageMultiplier;

    public LivingFallEvent(LivingEntity entity, double distance, float damageMultiplier) {
        super(entity);
        this.distance = distance;
        this.damageMultiplier = damageMultiplier;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(float damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }
}
