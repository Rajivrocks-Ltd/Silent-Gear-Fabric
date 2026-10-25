package net.silentchaos512.gear.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.silentchaos512.gear.compat.trinkets.TrinketsCompat;
import net.silentchaos512.gear.util.Const;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Makes piglins neutral to players wearing a Silent Gear trinket with the Brilliant trait.
 *
 * <p>Fabric port only. Upstream's curio answers Curios' {@code makesPiglinsNeutral} hook. Trinkets has no such
 * hook; it only checks the {@code piglin_safe_armor} tag for items in trinket slots. Armor slots are handled
 * by Silent Lib's {@code PiglinAiMixin}.
 */
@Mixin(PiglinAi.class)
public abstract class PiglinAiMixin {
    @ModifyReturnValue(method = "isWearingSafeArmor", at = @At("RETURN"))
    private static boolean silentgear$brilliantTrinket(boolean wearingSafeArmor, @Local(argsOnly = true) LivingEntity entity) {
        if (wearingSafeArmor || !FabricLoader.getInstance().isModLoaded(Const.TRINKETS)) {
            return wearingSafeArmor;
        }
        return TrinketsCompat.makesPiglinsNeutral(entity);
    }
}
