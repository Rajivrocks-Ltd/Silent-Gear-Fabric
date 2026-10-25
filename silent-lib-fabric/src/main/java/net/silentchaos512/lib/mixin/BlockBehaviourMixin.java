package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.silentchaos512.lib.port.event.NeoForge;
import net.silentchaos512.lib.port.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

/**
 * Posts {@link PlayerEvent.BreakSpeed} when the game computes how fast a player breaks a block.
 *
 * <p>Fabric port only. NeoForge posts it from {@code Player.getDestroySpeed}, which also receives the block
 * position. Vanilla's {@code Player.getDestroySpeed} has no position, so the call is wrapped in its caller,
 * {@code BlockBehaviour.getDestroyProgress}, which has it. A canceled event means a speed of 0, so the block
 * cannot be broken (NeoForge returns -1, with the same effect).
 */
@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    @WrapOperation(
            method = "getDestroyProgress",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F")
    )
    private float silentlib$postBreakSpeed(Player player, BlockState state, Operation<Float> original, @Local(argsOnly = true) BlockPos pos) {
        float speed = original.call(player, state);
        var event = NeoForge.EVENT_BUS.post(new PlayerEvent.BreakSpeed(player, state, speed, Optional.of(pos)));
        return event.isCanceled() ? 0.0F : event.getNewSpeed();
    }
}
