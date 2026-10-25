package net.silentchaos512.gear.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.silentchaos512.gear.item.CraftingItems;
import net.silentchaos512.gear.setup.GearItemSets;
import net.silentchaos512.gear.util.Const;
import net.silentchaos512.gear.util.GearHelper;
import net.silentchaos512.gear.util.TraitHelper;

/**
 * M8 checklist: durability, broken gear, and anvil repair and upgrades.
 */
public class DurabilityTests {
    private static final BlockPos TARGET = new BlockPos(2, 1, 1);

    /** Blocks mined per test. Iron (Malleable) and the wood rod (Flexible) sometimes skip durability loss. */
    private static final int BLOCKS = 10;

    /**
     * A survival player mining stone with an iron pickaxe damages it (Silent Lib's {@code ItemStackMixin} hook
     * for {@code damageItem}). Several blocks are mined because iron's Malleable and the wood rod's Flexible
     * randomly skip some of the damage.
     *
     * @param helper the test helper
     */
    @GameTest
    public void pickaxeLosesDurabilityWhenMining(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, GearTestUtil.gear(GearItemSets.PICKAXE, "iron"));
        for (int i = 0; i < BLOCKS; i++) {
            helper.setBlock(TARGET, Blocks.STONE);
            helper.assertTrue(player.gameMode.destroyBlock(helper.absolutePos(TARGET)), "stone was not broken");
        }
        int damage = player.getMainHandItem().getDamageValue();
        helper.assertTrue(damage > 0 && damage <= BLOCKS, "damage after " + BLOCKS + " blocks should be 1 to " + BLOCKS
                + ", was " + damage + " (" + player.describe() + ")");
        helper.succeed();
    }

    /**
     * At its last point of durability a gear tool becomes broken: it stays in the hand instead of disappearing.
     *
     * @param helper the test helper
     */
    @GameTest
    public void brokenToolStaysInHand(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack pickaxe = GearTestUtil.gear(GearItemSets.PICKAXE, "iron");
        pickaxe.setDamageValue(pickaxe.getMaxDamage() - 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        for (int i = 0; i < BLOCKS && !GearHelper.isBroken(player.getMainHandItem()); i++) {
            helper.setBlock(TARGET, Blocks.STONE);
            player.gameMode.destroyBlock(helper.absolutePos(TARGET));
        }
        ItemStack held = player.getMainHandItem();
        helper.assertFalse(held.isEmpty(), "the broken pickaxe disappeared");
        helper.assertTrue(GearHelper.isBroken(held), "pickaxe should be broken, damage " + held.getDamageValue() + "/" + held.getMaxDamage());
        helper.succeed();
    }

    /**
     * The break callback (sound and particles) runs exactly once, on the use that really breaks the tool, even
     * when Malleable or Flexible cancel some of the damage on the way.
     *
     * <p>Port fix (2026-10-25): upstream ran the callback when the damage before traits would break the tool, so a
     * cancelled point played the sound without breaking it, and the sound played again on the next use.
     *
     * @param helper the test helper
     */
    @GameTest
    public void breakCallbackRunsOnceWhenReallyBroken(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        for (int attempt = 0; attempt < 20; attempt++) {
            ItemStack pickaxe = GearTestUtil.gear(GearItemSets.PICKAXE, "iron");
            pickaxe.setDamageValue(pickaxe.getMaxDamage() - 3);
            int[] breaks = {0};
            for (int use = 0; use < 50 && !GearHelper.isBroken(pickaxe); use++) {
                int breaksBefore = breaks[0];
                pickaxe.hurtAndBreak(1, helper.getLevel(), player, item -> breaks[0]++);
                helper.assertTrue(breaks[0] == breaksBefore || GearHelper.isBroken(pickaxe),
                        "break callback ran but the pickaxe is not broken (damage " + pickaxe.getDamageValue() + "/" + pickaxe.getMaxDamage() + ")");
            }
            helper.assertTrue(GearHelper.isBroken(pickaxe), "pickaxe never broke");
            helper.assertValueEqual(breaks[0], 1, "break callback count");
        }
        helper.succeed();
    }

    /**
     * A broken gear axe mines logs much slower than a working one ({@code GearHelper.getDestroySpeed}).
     *
     * @param helper the test helper
     */
    @GameTest
    public void brokenAxeMinesSlowly(GameTestHelper helper) {
        assertBrokenIsSlow(helper, GearTestUtil.gear(GearItemSets.AXE, "iron"), Blocks.OAK_LOG.defaultBlockState());
        helper.succeed();
    }

    /**
     * A broken gear pickaxe, and the hammer that extends it, mine stone much slower than a working one.
     *
     * <p>Port fix (2026-10-25): upstream's broken pickaxe kept full speed; see {@code GearPickaxeItem}.
     *
     * @param helper the test helper
     */
    @GameTest
    public void brokenPickaxeMinesSlowly(GameTestHelper helper) {
        BlockState stone = Blocks.STONE.defaultBlockState();
        assertBrokenIsSlow(helper, GearTestUtil.gear(GearItemSets.PICKAXE, "iron"), stone);
        assertBrokenIsSlow(helper, GearTestUtil.gear(GearItemSets.HAMMER, "iron"), stone);
        helper.succeed();
    }

    private static void assertBrokenIsSlow(GameTestHelper helper, ItemStack tool, BlockState state) {
        float workingSpeed = tool.getDestroySpeed(state);
        tool.setDamageValue(tool.getMaxDamage() - 1);
        helper.assertTrue(GearHelper.isBroken(tool), "setup: " + tool.getItem() + " should be broken");
        float brokenSpeed = tool.getDestroySpeed(state);
        helper.assertTrue(brokenSpeed < 1f && brokenSpeed < workingSpeed,
                tool.getItem() + ": broken speed " + brokenSpeed + " should be far below working speed " + workingSpeed);
    }

    /**
     * Anvil: a damaged iron pickaxe plus iron ingots on the right is repaired and costs XP
     * ({@code AnvilMenuMixin} posts {@code AnvilUpdateEvent}, Silent Gear's {@code RepairHandler} answers).
     *
     * @param helper the test helper
     */
    @GameTest
    public void anvilRepairsGear(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack pickaxe = GearTestUtil.gear(GearItemSets.PICKAXE, "iron");
        int damage = pickaxe.getMaxDamage() / 2;
        pickaxe.setDamageValue(damage);

        AnvilMenu menu = new AnvilMenu(1, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(TARGET)));
        menu.getSlot(0).set(pickaxe);
        menu.getSlot(1).set(new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 4));

        ItemStack result = menu.getSlot(2).getItem();
        helper.assertFalse(result.isEmpty(), "anvil gave no result");
        helper.assertTrue(result.getDamageValue() < damage, "result damage " + result.getDamageValue() + " should be below " + damage);
        helper.assertTrue(menu.getCost() > 0, "anvil XP cost should be above 0, was " + menu.getCost());
        helper.succeed();
    }

    /**
     * Anvil: a spoon upgrade on the right is added to the pickaxe, which then mines dirt fast.
     *
     * @param helper the test helper
     */
    @GameTest
    public void anvilAppliesUpgrade(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack pickaxe = GearTestUtil.gear(GearItemSets.PICKAXE, "iron");
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        float dirtSpeedBefore = pickaxe.getDestroySpeed(dirt);

        AnvilMenu menu = new AnvilMenu(1, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(TARGET)));
        menu.getSlot(0).set(pickaxe);
        menu.getSlot(1).set(new ItemStack(CraftingItems.SPOON_UPGRADE));

        ItemStack result = menu.getSlot(2).getItem();
        helper.assertFalse(result.isEmpty(), "anvil gave no result for the spoon upgrade");
        helper.assertTrue(TraitHelper.hasTrait(result, Const.Traits.SPOON), "result has no Spoon trait: " + TraitHelper.getTraits(result));
        float dirtSpeedAfter = result.getDestroySpeed(dirt);
        helper.assertTrue(dirtSpeedAfter > dirtSpeedBefore, "dirt speed " + dirtSpeedAfter + " should be above " + dirtSpeedBefore);
        helper.succeed();
    }
}
