package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.silentchaos512.lib.port.ItemAbilities;
import net.silentchaos512.lib.port.internal.ItemAbilityChecks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lets any shears-like item shear sheep, mooshrooms, snow golems, bogged and copper golems, not only vanilla
 * shears.
 *
 * <p>Fabric port only. NeoForge removes the {@code is(Items.SHEARS)} branch from these mobs and shears them
 * from {@code ShearsItem.interactLivingEntity} instead, so every {@code ShearsItem} works, including Silent
 * Gear's. Here the vanilla branch stays and its check becomes {@code canPerformAction(SHEARS_HARVEST)}, which
 * is true for vanilla shears and for Silent Gear shears that are not broken.
 */
@Mixin({Sheep.class, MushroomCow.class, SnowGolem.class, Bogged.class, CopperGolem.class})
public abstract class ShearableMobsMixin {
    @WrapOperation(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
    private boolean silentlib$shearsAbility(ItemStack stack, Object item, Operation<Boolean> original) {
        return ItemAbilityChecks.isOrCanPerform(stack, item, original, Items.SHEARS, ItemAbilities.SHEARS_HARVEST);
    }
}
