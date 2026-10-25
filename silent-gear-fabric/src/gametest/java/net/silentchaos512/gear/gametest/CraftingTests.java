package net.silentchaos512.gear.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.silentchaos512.gear.api.item.GearItem;
import net.silentchaos512.gear.api.part.PartType;
import net.silentchaos512.gear.api.util.DataResource;
import net.silentchaos512.gear.crafting.ingredient.BlueprintIngredient;
import net.silentchaos512.gear.crafting.ingredient.GearPartIngredient;
import net.silentchaos512.gear.crafting.ingredient.PartMaterialIngredient;
import net.silentchaos512.gear.gear.material.MaterialInstance;
import net.silentchaos512.gear.item.CompoundPartItem;
import net.silentchaos512.gear.setup.GearItemSets;
import net.silentchaos512.gear.setup.SgItems;
import net.silentchaos512.gear.setup.gear.PartTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * M11 crafting checks, driven by the loaded recipes instead of a hand-written list.
 *
 * <p>Each recipe's ingredients are filled automatically: a blueprint ingredient gets its blueprint, a material
 * ingredient gets the first common item it accepts, a "part type" ingredient gets a default part (wood rod,
 * string cord, ...), and a main part item gets each candidate material in turn. The recipe must then match in a
 * crafting grid and produce its result.
 */
public class CraftingTests {
    /** Common items tried for material ingredients, in order. */
    private static final List<Item> MATERIAL_ITEMS = List.of(Items.IRON_INGOT, Items.OAK_PLANKS, Items.LEATHER,
            Items.PHANTOM_MEMBRANE, Items.GOLD_INGOT, Items.DIAMOND, Items.FLINT, Items.STRING, Items.FEATHER,
            Items.WHITE_WOOL, Items.COBBLESTONE);
    /** Materials tried for main parts (heads, plates, ...), in order. */
    private static final List<String> MAIN_MATERIALS = List.of("iron", "wood", "leather", "phantom_membrane", "gold", "diamond", "string");

    private static Map<PartType, Supplier<CompoundPartItem>> partItems() {
        return Map.of(
                PartTypes.ROD.get(), SgItems.ROD,
                PartTypes.CORD.get(), SgItems.CORD,
                PartTypes.BINDING.get(), SgItems.BINDING,
                PartTypes.SETTING.get(), SgItems.SETTING,
                PartTypes.FLETCHING.get(), SgItems.FLETCHING,
                PartTypes.LINING.get(), SgItems.LINING,
                PartTypes.GRIP.get(), SgItems.GRIP,
                PartTypes.TIP.get(), SgItems.TIP,
                PartTypes.COATING.get(), SgItems.COATING
        );
    }

    private static Map<PartType, String> partMaterials() {
        return Map.of(
                PartTypes.ROD.get(), "wood",
                PartTypes.CORD.get(), "string",
                PartTypes.BINDING.get(), "string",
                PartTypes.SETTING.get(), "diamond",
                PartTypes.FLETCHING.get(), "feather",
                PartTypes.LINING.get(), "wool",
                PartTypes.GRIP.get(), "wool",
                PartTypes.TIP.get(), "iron",
                PartTypes.COATING.get(), "gold"
        );
    }

    /**
     * Every Silent Gear gear recipe (blueprint parts, gear assembly, quick recipes, lined armor, jewelry with gems)
     * matches its own ingredients and produces its result. Covers all 34 gear types.
     *
     * @param helper the test helper
     */
    @GameTest(maxTicks = 100)
    public void everyGearRecipeCrafts(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        int checked = 0;
        for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
            var id = holder.id().identifier();
            if (!id.getNamespace().equals("silentgear") || !id.getPath().startsWith("gear/") || id.getPath().startsWith("gear/convert/")) {
                continue;
            }
            if (!(holder.value() instanceof CraftingRecipe recipe)) {
                continue;
            }
            checked++;
            String problem = tryCraft(helper, recipe);
            if (problem != null) {
                failures.add(id.getPath() + ": " + problem);
            }
        }
        helper.assertTrue(checked > 100, "only " + checked + " gear recipes found");
        helper.assertTrue(failures.isEmpty(), failures.size() + " of " + checked + " gear recipes failed: " + String.join("; ", failures));
        helper.succeed();
    }

    private static String tryCraft(GameTestHelper helper, CraftingRecipe recipe) {
        List<Ingredient> ingredients = recipe.placementInfo().ingredients();
        String lastProblem = "no ingredients";
        for (String mainMaterial : MAIN_MATERIALS) {
            List<ItemStack> stacks = new ArrayList<>();
            Item firstMaterialItem = null;
            boolean filled = true;
            for (Ingredient ingredient : ingredients) {
                CustomIngredient custom = ingredient.getCustomIngredient();
                ItemStack stack;
                if (custom instanceof PartMaterialIngredient) {
                    if (firstMaterialItem == null) {
                        firstMaterialItem = MATERIAL_ITEMS.stream().filter(i -> ingredient.test(new ItemStack(i))).findFirst().orElse(null);
                    }
                    stack = firstMaterialItem != null ? new ItemStack(firstMaterialItem) : ItemStack.EMPTY;
                } else if (custom instanceof GearPartIngredient partIngredient) {
                    var partItem = partItems().get(partIngredient.getPartType());
                    stack = partItem == null ? ItemStack.EMPTY : partItem.get().create(MaterialInstance.of(DataResource.material(partMaterials().get(partIngredient.getPartType()))));
                } else {
                    Optional<Holder<Item>> first = ingredient.items().findFirst();
                    stack = first.map(h -> new ItemStack(h.value())).orElse(ItemStack.EMPTY);
                    if (!(custom instanceof BlueprintIngredient) && stack.getItem() instanceof CompoundPartItem part) {
                        stack = part.create(MaterialInstance.of(DataResource.material(mainMaterial)), 3);
                    }
                }
                if (stack.isEmpty() || !ingredient.test(stack)) {
                    filled = false;
                    lastProblem = "no item found for an ingredient (" + (custom != null ? custom.getClass().getSimpleName() : "plain") + ")";
                    break;
                }
                stacks.add(stack);
            }
            if (!filled) {
                continue;
            }
            CraftingInput input = grid(stacks);
            if (!recipe.matches(input, helper.getLevel())) {
                lastProblem = "does not match its own ingredients (main material " + mainMaterial + ")";
                continue;
            }
            ItemStack result = recipe.assemble(input);
            if (result.isEmpty()) {
                lastProblem = "matched but made nothing";
                continue;
            }
            return null;
        }
        return lastProblem;
    }

    private static CraftingInput grid(List<ItemStack> stacks) {
        List<ItemStack> items = new ArrayList<>(stacks);
        while (items.size() < 9) {
            items.add(ItemStack.EMPTY);
        }
        return CraftingInput.of(3, 3, items);
    }

    /**
     * Every plain shaped and shapeless crafting recipe of Silent Gear (blueprints, templates, blocks, nuggets, ...)
     * crafts when each slot holds the first item its ingredient accepts. Recipes that only accept items from other
     * mods, or a custom alloy item, are skipped.
     *
     * @param helper the test helper
     */
    @GameTest(maxTicks = 100)
    public void everyPlainCraftingRecipeCrafts(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        int checked = 0;
        int skipped = 0;
        for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
            var id = holder.id().identifier();
            if (!id.getNamespace().equals("silentgear")) {
                continue;
            }
            CraftingInput input;
            if (holder.value() instanceof ShapedRecipe shaped && shaped.getClass() == ShapedRecipe.class) {
                List<ItemStack> items = new ArrayList<>();
                boolean ok = true;
                for (Optional<Ingredient> slot : shaped.getIngredients()) {
                    ItemStack stack = slot.map(CraftingTests::plainItemFor).orElse(ItemStack.EMPTY);
                    if (slot.isPresent() && stack.isEmpty()) ok = false;
                    items.add(stack);
                }
                if (!ok) { skipped++; continue; }
                input = CraftingInput.of(shaped.getWidth(), shaped.getHeight(), items);
            } else if (holder.value() instanceof ShapelessRecipe shapeless && shapeless.getClass() == ShapelessRecipe.class) {
                List<ItemStack> items = new ArrayList<>();
                boolean ok = true;
                for (Ingredient ingredient : shapeless.placementInfo().ingredients()) {
                    ItemStack stack = plainItemFor(ingredient);
                    if (stack.isEmpty()) ok = false;
                    items.add(stack);
                }
                if (!ok) { skipped++; continue; }
                input = grid(items);
            } else {
                continue;
            }
            checked++;
            CraftingRecipe recipe = (CraftingRecipe) holder.value();
            if (!recipe.matches(input, helper.getLevel()) || recipe.assemble(input).isEmpty()) {
                failures.add(id.getPath());
            }
        }
        helper.assertTrue(checked > 50, "only " + checked + " plain recipes found (" + skipped + " skipped)");
        helper.assertTrue(failures.isEmpty(), failures.size() + " of " + checked + " plain recipes failed: " + failures);
        helper.succeed();
    }

    /**
     * Picks an item for an ingredient of a plain recipe: the first common item a Silent Gear material ingredient
     * accepts, or the ingredient's first item. Other custom ingredients (a custom alloy gem) give nothing, so the
     * recipe is skipped.
     */
    private static ItemStack plainItemFor(Ingredient ingredient) {
        CustomIngredient custom = ingredient.getCustomIngredient();
        if (custom instanceof PartMaterialIngredient) {
            return MATERIAL_ITEMS.stream().map(ItemStack::new).filter(ingredient::test).findFirst().orElse(ItemStack.EMPTY);
        }
        if (custom != null && !(custom instanceof BlueprintIngredient)) {
            return ItemStack.EMPTY;
        }
        return ingredient.items().findFirst().map(h -> new ItemStack(h.value())).orElse(ItemStack.EMPTY);
    }

    /**
     * Every vanilla tool and armor piece converts to its Silent Gear version when placed alone in the grid
     * ({@code gear/convert/*} recipes).
     *
     * @param helper the test helper
     */
    @GameTest(maxTicks = 100)
    public void everyConversionRecipeWorks(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        int checked = 0;
        for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().getRecipes()) {
            var id = holder.id().identifier();
            if (!id.getNamespace().equals("silentgear") || !id.getPath().startsWith("gear/convert/") || !(holder.value() instanceof CraftingRecipe recipe)) {
                continue;
            }
            checked++;
            Item vanilla = recipe.placementInfo().ingredients().getFirst().items().findFirst().map(Holder::value).orElse(Items.AIR);
            CraftingInput input = grid(List.of(new ItemStack(vanilla)));
            var found = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
            ItemStack result = found.map(h -> h.value().assemble(input)).orElse(ItemStack.EMPTY);
            if (!(result.getItem() instanceof GearItem) || result.getMaxDamage() <= 0) {
                failures.add(id.getPath() + " -> " + result);
            }
        }
        helper.assertTrue(checked >= 40, "only " + checked + " conversion recipes found");
        helper.assertTrue(failures.isEmpty(), "conversions failed: " + failures);
        helper.succeed();
    }

    /**
     * A pickaxe head from 2 iron and 1 gold is rejected (upstream requires one material per part), while 3 iron
     * crafts. The blueprint stays in the grid; a template is used up.
     *
     * @param helper the test helper
     */
    @GameTest
    public void partRulesAndBlueprintRemainder(GameTestHelper helper) {
        ItemStack blueprint = new ItemStack(GearItemSets.PICKAXE.blueprint());
        ItemStack template = new ItemStack(GearItemSets.PICKAXE.template());
        var level = helper.getLevel();

        CraftingInput good = grid(List.of(blueprint.copy(), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT)));
        var goodRecipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, good, level);
        helper.assertTrue(goodRecipe.isPresent(), "blueprint + 3 iron should craft a pickaxe head");
        helper.assertTrue(goodRecipe.get().value().assemble(good).is(GearItemSets.PICKAXE.mainPart()), "result should be a pickaxe head");
        var remaining = goodRecipe.get().value().getRemainingItems(good);
        helper.assertTrue(remaining.stream().anyMatch(s -> s.is(blueprint.getItem())), "the blueprint should stay in the grid, remaining: " + remaining);

        CraftingInput withTemplate = grid(List.of(template, new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT)));
        var templateRecipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, withTemplate, level);
        helper.assertTrue(templateRecipe.isPresent(), "template + 3 iron should craft a pickaxe head");
        var templateRemaining = templateRecipe.get().value().getRemainingItems(withTemplate);
        helper.assertTrue(templateRemaining.stream().noneMatch(s -> s.is(template.getItem())), "the template should be used up");

        CraftingInput mixed = grid(List.of(blueprint.copy(), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT)));
        var mixedResult = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, mixed, level)
                .map(h -> h.value().assemble(mixed)).orElse(ItemStack.EMPTY);
        helper.assertTrue(mixedResult.isEmpty(), "2 iron + 1 gold should not craft a part, got " + mixedResult);
        helper.succeed();
    }
}
