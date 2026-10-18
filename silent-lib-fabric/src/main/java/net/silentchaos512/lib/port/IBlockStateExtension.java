package net.silentchaos512.lib.port;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

/**
 * Block state shortcuts for {@link IBlockExtension}, as NeoForge adds them to every block state.
 *
 * <p>Injected into {@link BlockBehaviour.BlockStateBase} by Silent Lib. Each method calls the block's
 * version with this state.
 */
public interface IBlockStateExtension {
    private BlockState self() {
        return (BlockState) this;
    }

    /** @see IBlockExtension#canHarvestBlock */
    default boolean canHarvestBlock(BlockGetter level, BlockPos pos, Player player) {
        return self().getBlock().canHarvestBlock(self(), level, pos, player);
    }

    /** @see IBlockExtension#onDestroyedByPlayer */
    default boolean onDestroyedByPlayer(Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        return self().getBlock().onDestroyedByPlayer(self(), level, pos, player, toolStack, willHarvest, fluid);
    }

    /** @see IBlockExtension#getExpDrop */
    default int getExpDrop(LevelAccessor level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity breaker, ItemStack tool) {
        return self().getBlock().getExpDrop(self(), level, pos, blockEntity, breaker, tool);
    }
}
