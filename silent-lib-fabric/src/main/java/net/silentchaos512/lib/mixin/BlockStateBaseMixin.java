package net.silentchaos512.lib.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.silentchaos512.lib.port.IBlockStateExtension;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds {@link IBlockStateExtension} to {@link BlockBehaviour.BlockStateBase} at runtime.
 *
 * <p>Fabric port only. The interface's default methods do all the work; this mixin has no body.
 * The compiler sees the interface through {@code silentlib.classtweaker}.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin implements IBlockStateExtension {
}
