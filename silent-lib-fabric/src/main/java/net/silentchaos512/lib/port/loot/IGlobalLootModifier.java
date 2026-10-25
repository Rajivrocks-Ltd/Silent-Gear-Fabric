package net.silentchaos512.lib.port.loot;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * Changes the items a loot table produced, like NeoForge's {@code IGlobalLootModifier}.
 *
 * <p>Fabric port stand-in. NeoForge loads these from {@code loot_modifiers} JSON files; here they are
 * registered in code with {@link GlobalLootModifiers#register}. A modifier checks its own conditions.
 */
@FunctionalInterface
public interface IGlobalLootModifier {
    /**
     * Applies the modifier.
     *
     * @param generatedLoot the items the table produced, already split into stacks of legal size
     * @param context       the loot context the table was rolled with
     * @return the new list of items (may be the same list)
     */
    ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context);
}
