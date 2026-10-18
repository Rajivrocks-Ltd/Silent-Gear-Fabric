package net.silentchaos512.lib.mixin;

import net.minecraft.world.entity.player.Player;
import net.silentchaos512.lib.port.menu.IPlayerExtension;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds {@link IPlayerExtension} to {@link Player} at runtime.
 *
 * <p>Fabric port only. The interface's default methods do all the work; this mixin has no body.
 * The compiler sees the interface through {@code silentlib.classtweaker}.
 */
@Mixin(Player.class)
public abstract class PlayerMixin implements IPlayerExtension {
}
