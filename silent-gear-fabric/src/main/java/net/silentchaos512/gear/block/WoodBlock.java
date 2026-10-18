package net.silentchaos512.gear.block;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.silentchaos512.lib.port.ItemAbilities;
import net.silentchaos512.lib.port.ItemAbility;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A log or wood block that can be stripped.
 *
 * <p>Port note: upstream overrides NeoForge's {@code IBlockExtension.getToolModifiedState}. Fabric has no
 * such hook, so vanilla axes use the pairs registered with Fabric's {@code StrippableBlockRegistry} in
 * {@code SgBlocks.registerStrippables()}. The method is kept, without {@code @Override}, for code that
 * asks a block for its stripped state directly.
 */
public class WoodBlock extends RotatedPillarBlock {
    private final Function<Block, Block> strippedBlock;

    public WoodBlock(Function<Block, Block> strippedBlock, Properties properties) {
        super(properties);
        this.strippedBlock = strippedBlock;
    }

    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (itemAbility == ItemAbilities.AXE_STRIP) {
            Block block = this.strippedBlock.apply(state.getBlock());
            if (block != null) {
                return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
            }
        }
        return null;
    }
}
