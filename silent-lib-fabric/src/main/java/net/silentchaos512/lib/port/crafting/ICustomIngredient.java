package net.silentchaos512.lib.port.crafting;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

/**
 * A custom ingredient written against NeoForge's method names, running on Fabric's custom
 * ingredient API.
 *
 * <p>Fabric port stand-in for NeoForge's {@code ICustomIngredient}. The two APIs are almost the
 * same; only two methods have different names:
 * <ul>
 *     <li>NeoForge {@link #isSimple()} is the opposite of Fabric {@link #requiresTesting()}.</li>
 *     <li>NeoForge {@link #getType()} is Fabric {@link #getSerializer()}.</li>
 * </ul>
 * Implement the NeoForge methods; the Fabric ones are derived from them.
 */
public interface ICustomIngredient extends CustomIngredient {
    /**
     * Whether this ingredient can be matched from its item list alone, without calling
     * {@link #test(net.minecraft.world.item.ItemStack)}.
     *
     * @return true if the item list is enough to match
     */
    boolean isSimple();

    /**
     * Gets the type that reads and writes this ingredient.
     *
     * @return the ingredient type
     */
    IngredientType<?> getType();

    @Override
    default boolean requiresTesting() {
        return !isSimple();
    }

    @Override
    default CustomIngredientSerializer<?> getSerializer() {
        return getType();
    }
}
