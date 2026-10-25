package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * Lets items control the durability of their stacks, as NeoForge does.
 *
 * <p>Fabric port only. NeoForge's {@code ItemStack} asks the item for these values; vanilla reads the data
 * components directly. The defaults in {@link net.silentchaos512.lib.port.IItemExtension} do exactly what
 * vanilla does, so items that do not override them behave the same. Only the methods that Silent Gear
 * overrides are routed:
 * <ul>
 *     <li>{@code getMaxDamage()} returns {@code IItemExtension.getMaxDamage(stack)}.</li>
 *     <li>{@code setDamageValue(int)} calls {@code IItemExtension.setDamage(stack, damage)}.</li>
 *     <li>{@code hurtAndBreak} first passes the damage through {@code IItemExtension.damageItem}, before
 *     enchantments reduce it, like NeoForge. Vanilla 26.1 only knows the player at this point (null for
 *     other entities); Silent Gear does not use the entity.</li>
 * </ul>
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean isDamageableItem();

    private ItemStack silentlib$self() {
        return (ItemStack) (Object) this;
    }

    @ModifyReturnValue(method = "getMaxDamage", at = @At("RETURN"))
    private int silentlib$itemMaxDamage(int original) {
        return this.getItem().getMaxDamage(silentlib$self());
    }

    @Inject(method = "setDamageValue", at = @At("HEAD"), cancellable = true)
    private void silentlib$itemSetDamage(int value, CallbackInfo ci) {
        this.getItem().setDamage(silentlib$self(), value);
        ci.cancel();
    }

    @ModifyVariable(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private int silentlib$itemDamageItem(int amount, int originalAmount, ServerLevel level, @Nullable ServerPlayer player, Consumer<Item> onBreak) {
        if (!this.isDamageableItem()) {
            return amount;
        }
        return this.getItem().damageItem(silentlib$self(), amount, player, onBreak);
    }
}
