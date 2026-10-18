package net.silentchaos512.lib.port.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Events fired while damage is applied to an entity, after armor and other reductions.
 */
public abstract class LivingDamageEvent extends LivingEvent {
    private final DamageSource source;
    private final float originalDamage;

    protected LivingDamageEvent(LivingEntity entity, DamageSource source, float originalDamage) {
        super(entity);
        this.source = source;
        this.originalDamage = originalDamage;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getOriginalDamage() {
        return originalDamage;
    }

    /**
     * Fired just before the final damage amount is subtracted from health. Listeners can change it.
     *
     * <p>Fabric: not fired yet. Needs a mixin in {@code LivingEntity.actuallyHurt} (milestone M8).
     */
    public static class Pre extends LivingDamageEvent {
        private float newDamage;

        public Pre(LivingEntity entity, DamageSource source, float originalDamage) {
            super(entity, source, originalDamage);
            this.newDamage = originalDamage;
        }

        public float getNewDamage() {
            return newDamage;
        }

        public void setNewDamage(float newDamage) {
            this.newDamage = newDamage;
        }
    }

    /**
     * Fired after damage was applied. Read only.
     *
     * <p>Fabric: fired from {@code ServerLivingEntityEvents.AFTER_DAMAGE}.
     */
    public static class Post extends LivingDamageEvent {
        private final float newDamage;

        public Post(LivingEntity entity, DamageSource source, float originalDamage, float newDamage) {
            super(entity, source, originalDamage);
            this.newDamage = newDamage;
        }

        public float getNewDamage() {
            return newDamage;
        }
    }
}
