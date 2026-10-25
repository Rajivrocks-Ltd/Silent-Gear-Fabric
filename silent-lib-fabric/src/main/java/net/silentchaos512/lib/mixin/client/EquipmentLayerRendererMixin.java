package net.silentchaos512.lib.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import net.silentchaos512.lib.port.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lets items choose the tint of each layer of their worn equipment, as NeoForge does.
 *
 * <p>Fabric port only. Vanilla colors a layer with the stack's dye color only when the equipment asset marks
 * the layer as dyeable. NeoForge asks {@link IClientItemExtensions#getArmorLayerTintColor} instead, with the
 * layer's index and the dye color. Silent Gear uses it to tint the first layer of its armor and elytra with the
 * gear's material color, also on assets whose layer is not dyeable (crimson steel armor, the elytra wings).
 * Items without registered extensions get vanilla's color from the default method.
 */
@Mixin(EquipmentLayerRenderer.class)
public abstract class EquipmentLayerRendererMixin {
    @WrapOperation(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;getColorForLayer(Lnet/minecraft/client/resources/model/EquipmentClientInfo$Layer;I)I")
    )
    private int silentlib$armorLayerTint(EquipmentClientInfo.Layer layer, int dyeColor, Operation<Integer> original,
                                         @Local(argsOnly = true) ItemStack itemStack, @Share("layerIndex") LocalIntRef layerIndex) {
        int index = layerIndex.get();
        layerIndex.set(index + 1);
        IClientItemExtensions extensions = IClientItemExtensions.of(itemStack);
        if (extensions == IClientItemExtensions.DEFAULT) {
            return original.call(layer, dyeColor);
        }
        return extensions.getArmorLayerTintColor(itemStack, layer, index, dyeColor);
    }
}
