package net.silentchaos512.gear.setup;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.silentchaos512.lib.registry.DeferredHolder;
import net.silentchaos512.lib.registry.DeferredRegister;
import net.silentchaos512.gear.SilentGear;

public class SgAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, SilentGear.MOD_ID);

    public static final DeferredHolder<Attribute, RangedAttribute> MAGIC_ARMOR = ATTRIBUTES.register(
            "magic_armor",
            () -> new RangedAttribute(
                    "magic_armor",
                    0.0,
                    0.0,
                    Integer.MAX_VALUE
            )
    );

    /**
     * How fast entities swim, as a multiplier (1.0 is normal speed). Used by the Swift Swim trait.
     *
     * <p>Port note: upstream uses NeoForge's own {@code neoforge:swim_speed} attribute, which Fabric does not
     * have. This attribute has the same base value, range and syncing. Silent Gear's {@code AttributeMapMixin}
     * gives it to every living entity and {@code LivingEntitySwimMixin} applies it while swimming.
     */
    public static final DeferredHolder<Attribute, Attribute> SWIM_SPEED = ATTRIBUTES.register(
            "swim_speed",
            () -> new RangedAttribute(
                    "attribute.name.silentgear.swim_speed",
                    1.0,
                    0.0,
                    1024.0
            ).setSyncable(true)
    );
}
