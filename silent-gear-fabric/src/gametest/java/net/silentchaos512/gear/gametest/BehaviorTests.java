package net.silentchaos512.gear.gametest;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.silentchaos512.gear.Config;
import net.silentchaos512.gear.item.GearItemSet;
import net.silentchaos512.gear.setup.GearItemSets;
import net.silentchaos512.gear.setup.SgItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * M11 behavior checks: tool tiers against vanilla, ranged weapons, saving and syncing gear, repair kits, and a
 * config value taking effect.
 */
public class BehaviorTests {
    /**
     * Gear pickaxes of each vanilla tier (wood, stone, iron, diamond, and crimson steel for netherite) are the correct
     * tool for exactly the same blocks as the vanilla pickaxe of that tier.
     *
     * @param helper the test helper
     */
    @GameTest
    public void pickaxeTiersMatchVanilla(GameTestHelper helper) {
        Map<String, Item> tiers = Map.of(
                "wood", Items.WOODEN_PICKAXE,
                "stone", Items.STONE_PICKAXE,
                "iron", Items.IRON_PICKAXE,
                "diamond", Items.DIAMOND_PICKAXE,
                "crimson_steel", Items.NETHERITE_PICKAXE); // netherite is only a coating; crimson steel is the netherite tier
        List<BlockState> blocks = List.of(Blocks.STONE.defaultBlockState(), Blocks.IRON_ORE.defaultBlockState(),
                Blocks.GOLD_ORE.defaultBlockState(), Blocks.DIAMOND_ORE.defaultBlockState(), Blocks.OBSIDIAN.defaultBlockState(),
                Blocks.ANCIENT_DEBRIS.defaultBlockState(), Blocks.DIRT.defaultBlockState());
        List<String> problems = new ArrayList<>();
        tiers.forEach((material, vanillaItem) -> {
            ItemStack gear = GearTestUtil.gear(GearItemSets.PICKAXE, material);
            ItemStack vanilla = new ItemStack(vanillaItem);
            for (BlockState state : blocks) {
                if (gear.isCorrectToolForDrops(state) != vanilla.isCorrectToolForDrops(state)) {
                    problems.add(material + " on " + state.getBlock() + ": gear " + gear.isCorrectToolForDrops(state) + ", vanilla " + vanilla.isCorrectToolForDrops(state));
                }
            }
        });
        helper.assertTrue(problems.isEmpty(), "tier differences: " + problems);
        helper.succeed();
    }

    /**
     * A fully drawn gear bow shoots an arrow from the player's inventory.
     *
     * @param helper the test helper
     */
    @GameTest
    public void bowShootsArrow(GameTestHelper helper) {
        assertShoots(helper, GearTestUtil.gear(GearItemSets.BOW, "wood"), new ItemStack(Items.ARROW, 16));
        helper.succeed();
    }

    /**
     * A fully drawn gear slingshot shoots a pebble.
     *
     * @param helper the test helper
     */
    @GameTest
    public void slingshotShootsPebble(GameTestHelper helper) {
        assertShoots(helper, GearTestUtil.gear(GearItemSets.SLINGSHOT, "wood"), new ItemStack(SgItems.PEBBLE.get(), 16));
        helper.succeed();
    }

    private static void assertShoots(GameTestHelper helper, ItemStack weapon, ItemStack ammo) {
        TestPlayer player = TestPlayer.survival(helper);
        player.getInventory().setItem(9, ammo);
        player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        int before = helper.getLevel().getEntitiesOfClass(AbstractArrow.class, helper.getBoundsWithPadding().inflate(8)).size();
        weapon.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        weapon.releaseUsing(helper.getLevel(), player, weapon.getUseDuration(player) - 40);
        int after = helper.getLevel().getEntitiesOfClass(AbstractArrow.class, helper.getBoundsWithPadding().inflate(8)).size();
        helper.assertTrue(after > before, weapon.getItem() + " did not shoot");
    }

    /**
     * A gear crossbow loads after a full charge and then shoots.
     *
     * @param helper the test helper
     */
    @GameTest
    public void crossbowLoadsAndShoots(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack crossbow = GearTestUtil.gear(GearItemSets.CROSSBOW, "wood");
        player.getInventory().setItem(9, new ItemStack(Items.ARROW, 16));
        player.setItemInHand(InteractionHand.MAIN_HAND, crossbow);
        crossbow.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        // In 26.1 the crossbow loads during its use ticks once fully charged, not on release.
        crossbow.onUseTick(helper.getLevel(), player, crossbow.getUseDuration(player) - 100);
        crossbow.releaseUsing(helper.getLevel(), player, crossbow.getUseDuration(player) - 100);
        helper.assertTrue(net.minecraft.world.item.CrossbowItem.isCharged(crossbow), "crossbow did not load");
        int before = helper.getLevel().getEntitiesOfClass(AbstractArrow.class, helper.getBoundsWithPadding().inflate(8)).size();
        crossbow.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        int after = helper.getLevel().getEntitiesOfClass(AbstractArrow.class, helper.getBoundsWithPadding().inflate(8)).size();
        helper.assertTrue(after > before, "loaded crossbow did not shoot");
        helper.succeed();
    }

    /**
     * Gear items survive saving (the codec used for worlds and player data) and network syncing unchanged, for one
     * item of every gear type.
     *
     * @param helper the test helper
     */
    @GameTest
    public void gearSurvivesSaveAndSync(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        List<String> problems = new ArrayList<>();
        GearItemSets.getIterator().forEachRemaining((GearItemSet<?> set) -> {
            ItemStack stack = GearTestUtil.gear(set, "iron");
            var saved = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
            ItemStack loaded = ItemStack.CODEC.parse(ops, saved).getOrThrow();
            if (!GearTestUtil.sameWhenSaved(helper, stack, loaded)) problems.add(set.name() + " save");

            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
            ItemStack.STREAM_CODEC.encode(buf, stack);
            ItemStack synced = ItemStack.STREAM_CODEC.decode(buf);
            if (!GearTestUtil.sameWhenSaved(helper, stack, synced)) problems.add(set.name() + " sync");
        });
        helper.assertTrue(problems.isEmpty(), "gear changed after save or sync: " + problems);
        helper.succeed();
    }

    /**
     * A repair kit is filled with iron in the crafting grid, then repairs a damaged iron pickaxe in the grid.
     *
     * @param helper the test helper
     */
    @GameTest
    public void repairKitFillsAndRepairs(GameTestHelper helper) {
        var level = helper.getLevel();
        ItemStack kit = new ItemStack(SgItems.STURDY_REPAIR_KIT.get());
        CraftingInput fillInput = grid(kit, new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT));
        ItemStack filled = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, fillInput, level)
                .map(h -> h.value().assemble(fillInput)).orElse(ItemStack.EMPTY);
        helper.assertTrue(filled.is(kit.getItem()) && !ItemStack.isSameItemSameComponents(filled, kit), "kit was not filled: " + filled);

        ItemStack pickaxe = GearTestUtil.gear(GearItemSets.PICKAXE, "iron");
        int damage = pickaxe.getMaxDamage() / 2;
        pickaxe.setDamageValue(damage);
        CraftingInput repairInput = grid(pickaxe, filled);
        ItemStack repaired = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, repairInput, level)
                .map(h -> h.value().assemble(repairInput)).orElse(ItemStack.EMPTY);
        helper.assertTrue(repaired.is(pickaxe.getItem()), "quick repair gave " + repaired);
        helper.assertTrue(repaired.getDamageValue() < damage, "damage " + repaired.getDamageValue() + " should be below " + damage);
        helper.succeed();
    }

    /**
     * Changing a config value takes effect: doubling the anvil repair factor repairs about twice as much.
     *
     * @param helper the test helper
     */
    @GameTest
    public void configChangeTakesEffect(GameTestHelper helper) {
        var factor = Config.Common.repairFactorAnvil;
        double original = factor.get();
        try {
            int normal = anvilRepairAmount(helper);
            factor.set(original * 2);
            int doubled = anvilRepairAmount(helper);
            helper.assertTrue(doubled > normal, "repair with factor " + (original * 2) + " (" + doubled + ") should beat factor " + original + " (" + normal + ")");
        } finally {
            factor.set(original);
        }
        helper.succeed();
    }

    private static int anvilRepairAmount(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack pickaxe = GearTestUtil.gear(GearItemSets.PICKAXE, "iron");
        int damage = pickaxe.getMaxDamage() - 2;
        pickaxe.setDamageValue(damage);
        AnvilMenu menu = new AnvilMenu(1, player.getInventory(), ContainerLevelAccess.NULL);
        menu.getSlot(0).set(pickaxe);
        menu.getSlot(1).set(new ItemStack(Items.IRON_INGOT, 1));
        return damage - menu.getSlot(2).getItem().getDamageValue();
    }

    private static CraftingInput grid(ItemStack... stacks) {
        List<ItemStack> items = new ArrayList<>(List.of(stacks));
        while (items.size() < 9) items.add(ItemStack.EMPTY);
        return CraftingInput.of(3, 3, items);
    }
}
