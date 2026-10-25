package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.silentchaos512.lib.port.event.NeoForge;
import net.silentchaos512.lib.port.event.entity.living.LivingDamageEvent;
import net.silentchaos512.lib.port.event.entity.living.LivingExperienceDropEvent;
import net.silentchaos512.lib.port.event.entity.living.LivingFallEvent;
import net.silentchaos512.lib.port.event.entity.living.LivingIncomingDamageEvent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Posts NeoForge's living entity events from vanilla code, at the points where NeoForge posts them.
 *
 * <p>Fabric port only.
 * <ul>
 *     <li>{@link LivingIncomingDamageEvent}: in {@code hurtServer}, after the invulnerability checks and before
 *     shield blocking. A changed amount continues through vanilla; canceling it means no damage.</li>
 *     <li>{@link LivingDamageEvent.Pre}: in {@code actuallyHurt}, after armor, enchantments and absorption, right
 *     before health drops. {@code Player} has its own {@code actuallyHurt}, see {@code PlayerMixin}.</li>
 *     <li>{@link LivingFallEvent}: at the start of {@code causeFallDamage}. Canceling it means no fall damage.</li>
 *     <li>{@link LivingExperienceDropEvent}: in {@code dropExperience}, for the amount about to be dropped.</li>
 * </ul>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    protected @Nullable EntityReference<Player> lastHurtByPlayer;

    private LivingEntity silentlib$self() {
        return (LivingEntity) (Object) this;
    }

    @Inject(
            method = "hurtServer",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;noActionTime:I", opcode = org.objectweb.asm.Opcodes.PUTFIELD, ordinal = 0),
            cancellable = true
    )
    private void silentlib$postIncomingDamage(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) LocalFloatRef damageRef) {
        var event = NeoForge.EVENT_BUS.post(new LivingIncomingDamageEvent(silentlib$self(), source, damageRef.get()));
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            return;
        }
        damageRef.set(event.getAmount());
    }

    @ModifyVariable(
            method = "actuallyHurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setAbsorptionAmount(F)V", ordinal = 0, shift = At.Shift.AFTER),
            argsOnly = true
    )
    private float silentlib$postDamagePre(float damage, @Local(argsOnly = true) DamageSource source) {
        var event = NeoForge.EVENT_BUS.post(new LivingDamageEvent.Pre(silentlib$self(), source, damage));
        return event.getNewDamage();
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void silentlib$postFall(double fallDistance, float damageModifier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir,
                                    @Local(argsOnly = true) LocalDoubleRef distanceRef, @Local(argsOnly = true) LocalFloatRef multiplierRef) {
        var event = NeoForge.EVENT_BUS.post(new LivingFallEvent(silentlib$self(), distanceRef.get(), multiplierRef.get()));
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            return;
        }
        distanceRef.set(event.getDistance());
        multiplierRef.set(event.getDamageMultiplier());
    }

    @WrapOperation(
            method = "dropExperience",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V")
    )
    private void silentlib$postExperienceDrop(ServerLevel level, Vec3 pos, int amount, Operation<Void> original) {
        Player attackingPlayer = this.lastHurtByPlayer != null ? this.lastHurtByPlayer.getEntity(level, Player.class) : null;
        var event = NeoForge.EVENT_BUS.post(new LivingExperienceDropEvent(silentlib$self(), attackingPlayer, amount));
        if (!event.isCanceled()) {
            original.call(level, pos, event.getDroppedExperience());
        }
    }
}
