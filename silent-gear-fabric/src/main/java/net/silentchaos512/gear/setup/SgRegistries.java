package net.silentchaos512.gear.setup;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.material.MaterialSerializer;
import net.silentchaos512.gear.api.material.modifier.IMaterialModifierType;
import net.silentchaos512.gear.api.part.PartSerializer;
import net.silentchaos512.gear.api.part.PartType;
import net.silentchaos512.gear.api.property.GearProperty;
import net.silentchaos512.gear.api.property.GearPropertyValue;
import net.silentchaos512.gear.api.traits.TraitConditionSerializer;
import net.silentchaos512.gear.api.traits.TraitEffectType;
import net.silentchaos512.gear.gear.material.MaterialManager;
import net.silentchaos512.gear.gear.part.PartManager;
import net.silentchaos512.gear.gear.trait.TraitManager;
import org.jetbrains.annotations.NotNull;

/**
 * Silent Gear's custom registries and data managers.
 *
 * <p>Port note: upstream builds the registries with NeoForge's {@code RegistryBuilder} and adds them in
 * {@code NewRegistryEvent}. Fabric's {@link FabricRegistryBuilder} creates and adds them in one step,
 * when this class loads. {@code sync(true)} becomes {@link RegistryAttribute#SYNCED}.
 */
public class SgRegistries {
    public static final ResourceKey<Registry<GearType>> GEAR_TYPE_KEY = createRegistryKey("gear_type");
    public static final ResourceKey<Registry<PartType>> PART_TYPE_KEY = createRegistryKey("part_type");
    public static final ResourceKey<Registry<GearProperty<?, ? extends GearPropertyValue<?>>>> GEAR_PROPERTY_KEY = createRegistryKey("property");
    public static final ResourceKey<Registry<TraitConditionSerializer<?>>> TRAIT_CONDITION_KEY = createRegistryKey("trait_condition");
    public static final ResourceKey<Registry<TraitEffectType<?>>> TRAIT_EFFECT_TYPE_KEY = createRegistryKey("trait_effect_type");
    public static final ResourceKey<Registry<MaterialSerializer<?>>> MATERIAL_SERIALIZER_KEY = createRegistryKey("material_serializer");
    public static final ResourceKey<Registry<IMaterialModifierType<?>>> MATERIAL_MODIFIER_TYPE_KEY = createRegistryKey("material_modifier_type");
    public static final ResourceKey<Registry<PartSerializer<?>>> PART_SERIALIZER_KEY = createRegistryKey("part_serializer");

    public static final Registry<GearType> GEAR_TYPE = FabricRegistryBuilder.createDefaulted(GEAR_TYPE_KEY, SilentGear.getId("none"))
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();
    public static final Registry<PartType> PART_TYPE = FabricRegistryBuilder.createDefaulted(PART_TYPE_KEY, SilentGear.getId("none"))
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();
    public static final Registry<GearProperty<?, ? extends GearPropertyValue<?>>> GEAR_PROPERTY = FabricRegistryBuilder.create(GEAR_PROPERTY_KEY)
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();
    public static final Registry<TraitConditionSerializer<?>> TRAIT_CONDITION = FabricRegistryBuilder.create(TRAIT_CONDITION_KEY)
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();
    public static final Registry<TraitEffectType<?>> TRAIT_EFFECT_TYPE = FabricRegistryBuilder.create(TRAIT_EFFECT_TYPE_KEY)
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();
    public static final Registry<MaterialSerializer<?>> MATERIAL_SERIALIZER = FabricRegistryBuilder.create(MATERIAL_SERIALIZER_KEY)
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();
    public static final Registry<IMaterialModifierType<?>> MATERIAL_MODIFIER_TYPE = FabricRegistryBuilder.create(MATERIAL_MODIFIER_TYPE_KEY)
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();
    public static final Registry<PartSerializer<?>> PART_SERIALIZER = FabricRegistryBuilder.create(PART_SERIALIZER_KEY)
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();

    public static final TraitManager TRAIT = new TraitManager();
    public static final MaterialManager MATERIAL = new MaterialManager();
    public static final PartManager PART = new PartManager();

    private static @NotNull <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(SilentGear.getId(name));
    }
}
