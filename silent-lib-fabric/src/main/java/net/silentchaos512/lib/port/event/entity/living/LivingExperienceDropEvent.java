package net.silentchaos512.lib.port.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * Fired when a dying entity is about to drop experience. Listeners can change the amount.
 *
 * <p>Fabric: posted by {@code LivingEntityMixin} in {@code LivingEntity.dropExperience}.
 */
public class LivingExperienceDropEvent extends LivingEvent {
    private final @Nullable Player attackingPlayer;
    private final int originalExperience;
    private int droppedExperience;

    public LivingExperienceDropEvent(LivingEntity entity, @Nullable Player attackingPlayer, int originalExperience) {
        super(entity);
        this.attackingPlayer = attackingPlayer;
        this.originalExperience = originalExperience;
        this.droppedExperience = originalExperience;
    }

    public @Nullable Player getAttackingPlayer() {
        return attackingPlayer;
    }

    public int getOriginalExperience() {
        return originalExperience;
    }

    public int getDroppedExperience() {
        return droppedExperience;
    }

    public void setDroppedExperience(int droppedExperience) {
        this.droppedExperience = droppedExperience;
    }
}
