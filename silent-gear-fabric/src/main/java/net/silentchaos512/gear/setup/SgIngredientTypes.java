package net.silentchaos512.gear.setup;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.crafting.ingredient.*;
import net.silentchaos512.lib.port.crafting.ICustomIngredient;
import net.silentchaos512.lib.port.crafting.IngredientType;

/**
 * Silent Gear's custom ingredient types.
 *
 * <p>Port note: upstream registers these in NeoForge's ingredient type registry through a
 * {@code DeferredRegister}, and the fields are holders. Fabric keeps custom ingredient serializers
 * in its own lookup instead of a registry, so the fields are the types themselves, registered by
 * {@link #register()}. The ids are unchanged, so the {@code "fabric:type"} values in recipe JSON match.
 */
public class SgIngredientTypes {
    public static IngredientType<BlueprintIngredient> BLUEPRINT;
    public static IngredientType<CustomAlloyIngredient> CUSTOM_ALLOY;
    public static IngredientType<GearTypeIngredient> GEAR_TYPE;
    public static IngredientType<PartMaterialIngredient> MATERIAL;
    public static IngredientType<OptionalTagIngredient> OPTIONAL_TAG;
    public static IngredientType<GearPartIngredient> PART;

    /**
     * Registers all custom ingredient types. Call once during common init.
     */
    public static void register() {
        BLUEPRINT = register("blueprint", BlueprintIngredient.CODEC, BlueprintIngredient.STREAM_CODEC);
        CUSTOM_ALLOY = register("custom_compound", CustomAlloyIngredient.CODEC, CustomAlloyIngredient.STREAM_CODEC);
        GEAR_TYPE = register("gear_type", GearTypeIngredient.CODEC, GearTypeIngredient.STREAM_CODEC);
        MATERIAL = register("material", PartMaterialIngredient.CODEC, PartMaterialIngredient.STREAM_CODEC);
        OPTIONAL_TAG = register("optional_tag", OptionalTagIngredient.CODEC, OptionalTagIngredient.STREAM_CODEC);
        PART = register("part_type", GearPartIngredient.CODEC, GearPartIngredient.STREAM_CODEC);
    }

    private static <T extends ICustomIngredient> IngredientType<T> register(
            String name,
            MapCodec<T> codec,
            StreamCodec<RegistryFriendlyByteBuf, T> streamCodec
    ) {
        return IngredientType.register(SilentGear.getId(name), codec, streamCodec);
    }
}
