package net.silentchaos512.lib.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.silentchaos512.lib.port.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets items set their own first-person hand pose, as NeoForge does.
 *
 * <p>Fabric port only. In {@code renderArmWithItem}, for items that are not maps or crossbows, NeoForge asks
 * {@link IClientItemExtensions#applyForgeHandTransform} before vanilla's use, spin attack and swing transforms,
 * and skips them when it returns true. Silent Gear's trident uses this for its charge animation (vanilla's is
 * fixed to 10 ticks). The injection point is the start of that branch (the second {@code isUsingItem} call).
 * When the item handles the pose, this renders the item, pops the pose and returns, which is exactly what
 * vanilla does after those transforms.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract void renderItem(LivingEntity owner, ItemStack itemStack, ItemDisplayContext type, PoseStack poseStack,
                                    SubmitNodeCollector submitNodeCollector, int lightCoords);

    @Inject(
            method = "renderArmWithItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z", ordinal = 1),
            cancellable = true
    )
    private void silentlib$applyHandTransform(AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand,
                                              float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack,
                                              SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        IClientItemExtensions extensions = IClientItemExtensions.of(itemStack);
        if (extensions == IClientItemExtensions.DEFAULT || this.minecraft.player == null) {
            return;
        }
        // Same arm as vanilla computes at the start of the method.
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        if (extensions.applyForgeHandTransform(poseStack, this.minecraft.player, arm, itemStack, frameInterp, inverseArmHeight, attack)) {
            ItemDisplayContext context = arm == HumanoidArm.RIGHT ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            this.renderItem(player, itemStack, context, poseStack, submitNodeCollector, lightCoords);
            poseStack.popPose();
            ci.cancel();
        }
    }
}
