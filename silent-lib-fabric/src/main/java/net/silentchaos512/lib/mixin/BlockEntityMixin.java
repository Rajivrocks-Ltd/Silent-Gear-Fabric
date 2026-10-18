package net.silentchaos512.lib.mixin;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.silentchaos512.lib.port.IBlockEntityExtension;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds {@link IBlockEntityExtension} to {@link BlockEntity} at runtime.
 *
 * <p>Fabric port only. The interface's default methods do all the work; this mixin has no body.
 * The compiler sees the interface through {@code silentlib.classtweaker}.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements IBlockEntityExtension {
}
