package net.silentchaos512.gear.loot.modifier;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.storage.loot.LootContext;


/**
 * Controls the smelting ability of the Magmatic trait. Pretty much a direct copy of the smelting
 * enchantment example provided by Forge.
 */
public class MagmaticTraitLootModifier {
    // Port note: upstream extends NeoForge's LootModifier and is registered as a global loot modifier with a
    // codec. Fabric has no such registry; SgLootModifiers registers doApply with Silent Lib's GlobalLootModifiers and
    // checks the conditions that the upstream JSON file declared.

    public ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ObjectArrayList<ItemStack> ret = new ObjectArrayList<>();
        generatedLoot.forEach(s -> ret.add(smelt(s, context)));
        return ret;
    }

    private static ItemStack smelt(ItemStack stack, LootContext context) {
        return context.getLevel().recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), context.getLevel())
                .map(r -> r.value().assemble(new SingleRecipeInput(stack)))
                .filter(s -> !s.isEmpty())
                .map(s -> s.copyWithCount(stack.getCount() * s.getCount()))
                .orElse(stack);
    }

}
