package net.silentchaos512.lib.port.crafting;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Objects;

/**
 * The codecs of a custom ingredient, registered under an id.
 *
 * <p>Fabric port stand-in for NeoForge's {@code IngredientType}. It is a Fabric
 * {@link CustomIngredientSerializer}, so in JSON the ingredient is written as
 * {@code {"fabric:type": "<id>", ...fields}} where NeoForge used {@code "neoforge:ingredient_type"}.
 *
 * @param <T> the ingredient class
 */
public final class IngredientType<T extends CustomIngredient> implements CustomIngredientSerializer<T> {
    private final Identifier id;
    private final MapCodec<T> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    private IngredientType(Identifier id, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        this.id = Objects.requireNonNull(id);
        this.codec = Objects.requireNonNull(codec);
        this.streamCodec = Objects.requireNonNull(streamCodec);
    }

    /**
     * Creates an ingredient type and registers it with Fabric.
     *
     * @param id          the type id, used as the {@code "fabric:type"} value in JSON
     * @param codec       reads and writes the ingredient fields in JSON
     * @param streamCodec sends the ingredient over the network
     * @param <T>         the ingredient class
     * @return the registered type
     */
    public static <T extends CustomIngredient> IngredientType<T> register(Identifier id, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        IngredientType<T> type = new IngredientType<>(id, codec, streamCodec);
        CustomIngredientSerializer.register(type);
        return type;
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @Override
    public MapCodec<T> getCodec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> getStreamCodec() {
        return streamCodec;
    }
}
