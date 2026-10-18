package net.silentchaos512.lib.port;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

/**
 * Extra block methods that NeoForge adds to every {@link Block}, for the Fabric port.
 *
 * <p>Injected into {@link Block} by Silent Lib, like {@link IItemExtension}. The defaults behave
 * like vanilla. These are used by code that breaks blocks itself (area mining tools), so they
 * work without hooks into vanilla.
 */
public interface IBlockExtension {
    /**
     * Checks whether the player can harvest this block, meaning it would drop its items.
     *
     * @param state  the block state
     * @param level  the level
     * @param pos    the position
     * @param player the player
     * @return true if the block would drop items
     */
    default boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return player.hasCorrectToolForDrops(state);
    }

    /**
     * Removes the block after a player broke it. Same as what vanilla does on each side.
     *
     * @param state       the block state
     * @param level       the level
     * @param pos         the position
     * @param player      the player
     * @param toolStack   the tool used
     * @param willHarvest true if the block will drop items
     * @param fluid       the fluid left in its place
     * @return true if the block was removed
     */
    default boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        if (level.isClientSide()) {
            // Vanilla's client sets the block, see MultiPlayerGameMode.destroyBlock
            return level.setBlock(pos, fluid.createLegacyBlock(), 11);
        }
        // Vanilla's server removes the block, see ServerPlayerGameMode.destroyBlock
        return level.removeBlock(pos, false);
    }

    /**
     * Gets extra experience to drop when the block is broken.
     *
     * <p>Returns 0. On Fabric, blocks that drop experience (such as ores) already do so from
     * vanilla's {@code spawnAfterBreak} when their drops are created, so nothing extra is needed.
     *
     * @param state       the block state
     * @param level       the level
     * @param pos         the position
     * @param blockEntity the block entity, or null
     * @param breaker     the entity that broke the block, or null
     * @param tool        the tool used
     * @return the extra experience
     */
    default int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity breaker, ItemStack tool) {
        return 0;
    }
}
