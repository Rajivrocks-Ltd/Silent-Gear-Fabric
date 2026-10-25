package net.silentchaos512.gear.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.silentchaos512.gear.setup.SgAttributes;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

/**
 * Gives every living entity the {@code silentgear:swim_speed} attribute.
 *
 * <p>Fabric port only. Upstream uses NeoForge's {@code neoforge:swim_speed}, which NeoForge adds to
 * {@code LivingEntity.createLivingAttributes}. Vanilla builds those attribute sets during bootstrap, before
 * mods register their attributes, so it cannot be added there. Instead, when an entity's attribute map is
 * asked for swim speed and does not have it, the instance is created on the spot with the attribute's
 * default value (1.0). That covers equipment modifiers, saving, loading and syncing, which all go through
 * {@code getInstance}.
 */
@Mixin(AttributeMap.class)
public abstract class AttributeMapMixin {
    @Shadow
    @Final
    private Map<Holder<Attribute>, AttributeInstance> attributes;

    @Shadow
    private void onAttributeModified(AttributeInstance instance) {
        throw new AssertionError();
    }

    @ModifyReturnValue(method = "getInstance", at = @At("RETURN"))
    private @Nullable AttributeInstance silentgear$createSwimSpeed(@Nullable AttributeInstance instance, @Local(argsOnly = true) Holder<Attribute> attribute) {
        if (instance == null && attribute.is(SgAttributes.SWIM_SPEED.getKey())) {
            instance = new AttributeInstance(attribute, this::onAttributeModified);
            this.attributes.put(attribute, instance);
        }
        return instance;
    }
}
