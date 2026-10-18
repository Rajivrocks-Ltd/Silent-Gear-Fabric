package net.silentchaos512.gear.client;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.silentchaos512.gear.setup.SgRecipes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps the recipes that the server sends to the client, for screens and recipe viewers.
 *
 * <p>Port notes: vanilla does not send full recipes to clients. Upstream asks NeoForge to send the
 * recipes of chosen recipe types ({@code OnDatapackSyncEvent.sendRecipes}), including all crafting
 * recipes, then keeps only gear crafting recipes from those. Fabric syncs recipes by serializer instead,
 * so {@link #registerRecipeSync()} syncs every Silent Gear serializer, which covers the same recipes.
 * The client side filter is unchanged. {@link #onRecipesReceived} is called from Fabric's
 * {@code ClientRecipeSynchronizedEvent}.
 */
public class ClientRecipeHelper {
    private static final Multimap<RecipeType<?>, RecipeHolder<?>> MAP_BY_TYPE = ArrayListMultimap.create();
    private static final Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> MAP_BY_KEY = new HashMap<>();

    public static Collection<RecipeHolder<?>> getRecipes(RecipeType<?> recipeType) {
        return MAP_BY_TYPE.get(recipeType);
    }

    public static Optional<RecipeHolder<?>> getRecipe(ResourceKey<Recipe<?>> key) {
        return Optional.ofNullable(MAP_BY_KEY.getOrDefault(key, null));
    }

    /**
     * Asks Fabric to send all recipes made by Silent Gear's serializers to clients. Call once during
     * common init, after the serializers are registered.
     */
    public static void registerRecipeSync() {
        SgRecipes.RECIPE_SERIALIZERS.getEntries().forEach(holder -> RecipeSynchronization.synchronizeRecipeSerializer(holder.get()));
    }

    public static void onRecipesReceived(SynchronizedRecipes recipes) {
        clearRecipeMaps();

        for (RecipeHolder<?> recipeHolder : recipes.recipes()) {
            if (recipeHolder.value().getType().equals(RecipeType.CRAFTING)) {
                // Filter only gear-related crafting recipes
                if (isGearCraftingRecipe(recipeHolder)) {
                    addRecipe(recipeHolder);
                }
            } else {
                // Keep all other types of recipes
                addRecipe(recipeHolder);
            }
        }
    }

    private static void clearRecipeMaps() {
        MAP_BY_TYPE.clear();
        MAP_BY_KEY.clear();
    }

    private static void addRecipe(RecipeHolder<?> recipe) {
        MAP_BY_TYPE.put(recipe.value().getType(), recipe);
        MAP_BY_KEY.put(recipe.id(), recipe);
    }

    private static boolean isGearCraftingRecipe(RecipeHolder<?> recipeHolder) {
        var serializer = recipeHolder.value().getSerializer();
        return serializer == SgRecipes.SHAPED_GEAR.get()
                || serializer == SgRecipes.SHAPELESS_GEAR.get()
                || serializer == SgRecipes.COMPOUND_PART.get();
    }
}
