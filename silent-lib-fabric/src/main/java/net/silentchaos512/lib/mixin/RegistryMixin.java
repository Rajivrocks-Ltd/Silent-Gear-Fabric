package net.silentchaos512.lib.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.silentchaos512.lib.registry.DeferredHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Lets registry codecs save a {@link DeferredHolder}, as NeoForge does.
 *
 * <p>Fabric port only. Vanilla's {@code Registry.holderByNameCodec} only accepts a {@link Holder.Reference} and
 * fails with "Unregistered holder" for any other holder. NeoForge patches {@code safeCastToReference} to unwrap the
 * holder with {@code getDelegate()} first, so mods can put their {@code DeferredHolder} straight into data
 * components (Silent Gear's armor adds {@code SgAttributes.MAGIC_ARMOR} to its attribute modifiers). Without this,
 * saving or syncing such a stack crashes the game. This mixin does the same unwrap for Silent Lib's holder.
 */
@Mixin(Registry.class)
public interface RegistryMixin {
    @ModifyVariable(method = "safeCastToReference", at = @At("HEAD"), argsOnly = true)
    private Holder<Object> silentlib$unwrapDeferredHolder(Holder<Object> holder) {
        return holder instanceof DeferredHolder<Object, ?> deferred ? deferred.getDelegate() : holder;
    }
}
