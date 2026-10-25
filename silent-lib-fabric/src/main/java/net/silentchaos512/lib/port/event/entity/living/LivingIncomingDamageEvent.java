package net.silentchaos512.lib.port.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Fired on the server when an entity is about to be hurt, before armor and other reductions.
 * Listeners can change the amount. Canceling it prevents the damage.
 *
 * <p>Fabric: posted by {@code LivingEntityMixin} in {@code LivingEntity.hurtServer}, after the invulnerability
 * checks.
 */
public class LivingIncomingDamageEvent extends LivingEvent {
    private final DamageSource source;
    private final float originalAmount;
    private float amount;

    public LivingIncomingDamageEvent(LivingEntity entity, DamageSource source, float amount) {
        super(entity);
        this.source = source;
        this.originalAmount = amount;
        this.amount = amount;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getOriginalAmount() {
        return originalAmount;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
