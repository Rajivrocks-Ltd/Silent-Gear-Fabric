package net.silentchaos512.lib.crafting.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;

/**
 * Checks whether a set of items satisfies a shapeless list of ingredients, where each ingredient
 * must be matched by a different item.
 *
 * <p>Fabric port only. Upstream uses NeoForge's {@code RecipeMatcher.findMatches} for shapeless
 * recipes that contain custom ingredients. That class is not available on Fabric, so this is an
 * independent implementation of the same idea: a bipartite matching between items and
 * ingredients, found with simple augmenting paths. Crafting grids hold at most 9 items, so the
 * cost is negligible.
 */
final class ShapelessIngredientMatcher {
    private ShapelessIngredientMatcher() {}

    /**
     * Tests whether every ingredient can be paired with its own item.
     *
     * @param items       the non-empty items in the crafting grid
     * @param ingredients the recipe ingredients
     * @return true if the counts are equal and a full one-to-one pairing exists
     */
    static boolean matches(List<ItemStack> items, List<Ingredient> ingredients) {
        int count = items.size();
        if (count != ingredients.size()) {
            return false;
        }

        // fits[i][j] is true when item i is accepted by ingredient j
        boolean[][] fits = new boolean[count][count];
        for (int i = 0; i < count; ++i) {
            for (int j = 0; j < count; ++j) {
                fits[i][j] = ingredients.get(j).test(items.get(i));
            }
        }

        // ingredientOwner[j] is the item currently paired with ingredient j, or -1
        int[] ingredientOwner = new int[count];
        Arrays.fill(ingredientOwner, -1);

        for (int item = 0; item < count; ++item) {
            if (!tryAssign(item, fits, ingredientOwner, new boolean[count])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tries to pair an item with a free ingredient, moving earlier pairings aside if needed.
     *
     * @param item            index of the item to place
     * @param fits            item to ingredient compatibility table
     * @param ingredientOwner current pairing, updated in place on success
     * @param visited         ingredients already tried in this search
     * @return true if the item was paired
     */
    private static boolean tryAssign(int item, boolean[][] fits, int[] ingredientOwner, boolean[] visited) {
        for (int ingredient = 0; ingredient < ingredientOwner.length; ++ingredient) {
            if (fits[item][ingredient] && !visited[ingredient]) {
                visited[ingredient] = true;
                if (ingredientOwner[ingredient] < 0
                        || tryAssign(ingredientOwner[ingredient], fits, ingredientOwner, visited)) {
                    ingredientOwner[ingredient] = item;
                    return true;
                }
            }
        }
        return false;
    }
}
