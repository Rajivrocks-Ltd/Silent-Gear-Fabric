package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.silentchaos512.lib.port.CommonHooks;
import net.silentchaos512.lib.port.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Posts {@link net.silentchaos512.lib.port.event.enchanting.GetEnchantmentLevelEvent} from the same
 * {@link EnchantmentHelper} methods that NeoForge's patch changes:
 * <ul>
 *     <li>{@code getItemEnchantmentLevel}: the level of one enchantment (efficiency, fortune and so on).</li>
 *     <li>{@code runIterationOnItem} (both variants): the enchantment effects that run on an item.</li>
 *     <li>{@code hasTag}: whether an item has an enchantment from a tag.</li>
 * </ul>
 *
 * <p>Fabric port only. The two methods without an entity use {@link CommonHooks#resolveLookup} and skip the
 * event when it finds no registry, as NeoForge does. The variant with an entity uses the entity's registries
 * and always posts the event, also for items without enchantments, as NeoForge does.
 */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @ModifyReturnValue(method = "getItemEnchantmentLevel", at = @At("RETURN"))
    private static int silentlib$postLevel(int level, @Local(argsOnly = true) Holder<Enchantment> enchantment, @Local(argsOnly = true) ItemInstance item) {
        return EventHooks.getEnchantmentLevelSpecific(level, item, enchantment);
    }

    @WrapOperation(
            method = {
                    "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentVisitor;)V",
                    "hasTag"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private static Object silentlib$postAllLevels(ItemStack stack, DataComponentType<?> type, Object defaultValue, Operation<Object> original) {
        Object value = original.call(stack, type, defaultValue);
        HolderLookup.RegistryLookup<Enchantment> lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
        if (lookup != null && value instanceof ItemEnchantments enchantments) {
            return EventHooks.getAllEnchantmentLevels(enchantments, stack, lookup);
        }
        return value;
    }

    @WrapOperation(
            method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;")
    )
    private static Object silentlib$postAllLevelsInSlot(ItemStack stack, DataComponentType<?> type, Operation<Object> original, @Local(argsOnly = true) LivingEntity entity) {
        Object value = original.call(stack, type);
        ItemEnchantments enchantments = value instanceof ItemEnchantments e ? e : ItemEnchantments.EMPTY;
        return EventHooks.getAllEnchantmentLevels(enchantments, stack, entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT));
    }
}
