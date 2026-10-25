package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.silentchaos512.lib.port.event.NeoForge;
import net.silentchaos512.lib.port.event.entity.living.LivingDamageEvent;
import net.silentchaos512.lib.port.event.tick.PlayerTickEvent;
import net.silentchaos512.lib.port.menu.IPlayerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds {@link IPlayerExtension} to {@link Player} at runtime, and posts player events where NeoForge posts them.
 *
 * <p>Fabric port only. The compiler sees the interface through {@code silentlib.classtweaker}.
 * <ul>
 *     <li>{@link PlayerTickEvent.Pre} and {@link PlayerTickEvent.Post}: at the start and end of {@code tick}, on
 *     both sides.</li>
 *     <li>{@link LivingDamageEvent.Pre}: players have their own {@code actuallyHurt}; same point as in
 *     {@code LivingEntityMixin}.</li>
 * </ul>
 */
@Mixin(Player.class)
public abstract class PlayerMixin implements IPlayerExtension {
    private Player silentlib$self() {
        return (Player) (Object) this;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void silentlib$postTickPre(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(silentlib$self()));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void silentlib$postTickPost(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(silentlib$self()));
    }

    @ModifyVariable(
            method = "actuallyHurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setAbsorptionAmount(F)V", ordinal = 0, shift = At.Shift.AFTER),
            argsOnly = true
    )
    private float silentlib$postDamagePre(float damage, @Local(argsOnly = true) DamageSource source) {
        var event = NeoForge.EVENT_BUS.post(new LivingDamageEvent.Pre(silentlib$self(), source, damage));
        return event.getNewDamage();
    }
}
