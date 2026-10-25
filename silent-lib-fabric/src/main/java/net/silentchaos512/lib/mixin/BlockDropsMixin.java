package net.silentchaos512.lib.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.silentchaos512.lib.port.internal.BlockDropsHook;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Posts {@link net.silentchaos512.lib.port.event.level.BlockDropsEvent} when a player or entity breaks a block,
 * as NeoForge does. See {@link BlockDropsHook} for how the drops and experience are handled.
 *
 * <p>Fabric port only. Only the {@code dropResources} variant with a breaker and tool is changed; the
 * variants without them (explosions, pistons and so on) are vanilla.
 */
@Mixin(Block.class)
public abstract class BlockDropsMixin {
    @Inject(
            method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void silentlib$dropWithEvent(BlockState state, Level level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity breaker, ItemStack tool, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            BlockDropsHook.dropResources(state, serverLevel, pos, blockEntity, breaker, tool);
            ci.cancel();
        }
    }

    @Inject(method = "popExperience", at = @At("HEAD"), cancellable = true)
    private void silentlib$captureExperience(ServerLevel level, BlockPos pos, int amount, CallbackInfo ci) {
        if (BlockDropsHook.captureExperience(amount)) {
            ci.cancel();
        }
    }
}
