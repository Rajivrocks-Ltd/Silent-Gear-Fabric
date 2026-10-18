package net.silentchaos512.lib.mixin;

import net.minecraft.world.level.block.Block;
import net.silentchaos512.lib.port.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds {@link IBlockExtension} to {@link Block} at runtime.
 *
 * <p>Fabric port only. The interface's default methods do all the work; this mixin has no body.
 * The compiler sees the interface through {@code silentlib.classtweaker}.
 */
@Mixin(Block.class)
public abstract class BlockMixin implements IBlockExtension {
}
