package net.silentchaos512.gear.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.silentchaos512.gear.setup.SgAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Applies the {@code silentgear:swim_speed} attribute at the same two points where NeoForge applies
 * {@code neoforge:swim_speed}:
 * <ul>
 *     <li>{@code travelInWater}: the speed passed to {@code moveRelative} is multiplied by swim speed.</li>
 *     <li>{@code jumpInLiquid}: the upward push (0.04) is multiplied by swim speed.</li>
 * </ul>
 *
 * <p>Fabric port only. Entities that never got the attribute (see {@code AttributeMapMixin}) use 1.0, so
 * nothing changes for them.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySwimMixin {
    private double silentgear$swimSpeed() {
        LivingEntity self = (LivingEntity) (Object) this;
        Holder<Attribute> swimSpeed = SgAttributes.SWIM_SPEED.getDelegate();
        return self.getAttributes().hasAttribute(swimSpeed) ? self.getAttributeValue(swimSpeed) : 1.0;
    }

    @ModifyArg(
            method = "travelInWater",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"),
            index = 0
    )
    private float silentgear$swimSpeedInWater(float speed) {
        return speed * (float) silentgear$swimSpeed();
    }

    @ModifyArg(
            method = "jumpInLiquid",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"),
            index = 1
    )
    private double silentgear$swimSpeedJump(double y) {
        return y * silentgear$swimSpeed();
    }
}
