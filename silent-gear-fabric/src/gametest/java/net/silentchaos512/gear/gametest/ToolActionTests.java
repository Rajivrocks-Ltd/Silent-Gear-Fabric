package net.silentchaos512.gear.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.silentchaos512.gear.setup.GearItemSets;

/**
 * M8 checklist: shears, axe, shovel and hoe actions, sword sweep, and fishing. These need the Silent Lib
 * mixins that turn vanilla's "is this exactly shears / a fishing rod" checks into item ability checks.
 */
public class ToolActionTests {
    private static final BlockPos TARGET = new BlockPos(2, 1, 1);

    private static ItemStack broken(ItemStack stack) {
        stack.setDamageValue(stack.getMaxDamage() - 1);
        return stack;
    }

    /**
     * Gear shears shear a sheep ({@code ShearableMobsMixin}).
     *
     * @param helper the test helper
     */
    @GameTest
    public void shearsShearSheep(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, GearTestUtil.gear(GearItemSets.SHEARS, "iron"));
        Sheep sheep = helper.spawnWithNoFreeWill(EntityType.SHEEP, TARGET);
        player.interactOn(sheep, InteractionHand.MAIN_HAND, sheep.position());
        helper.assertTrue(sheep.isSheared(), "sheep was not sheared");
        helper.succeed();
    }

    /**
     * Broken gear shears do not shear a sheep.
     *
     * @param helper the test helper
     */
    @GameTest
    public void brokenShearsDoNothing(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, broken(GearTestUtil.gear(GearItemSets.SHEARS, "iron")));
        Sheep sheep = helper.spawnWithNoFreeWill(EntityType.SHEEP, TARGET);
        player.interactOn(sheep, InteractionHand.MAIN_HAND, sheep.position());
        helper.assertFalse(sheep.isSheared(), "broken shears sheared the sheep");
        helper.succeed();
    }

    /**
     * Gear shears cut a leash ({@code EntityShearsMixin}).
     *
     * @param helper the test helper
     */
    @GameTest
    public void shearsCutLeash(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, TARGET);
        cow.setLeashedTo(player, true);
        helper.assertTrue(cow.isLeashed(), "setup: cow should be leashed");
        player.setItemInHand(InteractionHand.MAIN_HAND, GearTestUtil.gear(GearItemSets.SHEARS, "iron"));
        player.interactOn(cow, InteractionHand.MAIN_HAND, cow.position());
        helper.assertFalse(cow.isLeashed(), "leash was not cut");
        helper.succeed();
    }

    /**
     * Gear shears collect honeycomb from a full beehive ({@code BeehiveBlockMixin}).
     *
     * @param helper the test helper
     */
    @GameTest
    public void shearsCollectHoneycomb(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, GearTestUtil.gear(GearItemSets.SHEARS, "iron"));
        helper.setBlock(TARGET, Blocks.BEEHIVE.defaultBlockState().setValue(BeehiveBlock.HONEY_LEVEL, BeehiveBlock.MAX_HONEY_LEVELS));
        helper.useBlock(TARGET, player);
        helper.assertBlockProperty(TARGET, BeehiveBlock.HONEY_LEVEL, 0);
        helper.assertItemEntityPresent(Items.HONEYCOMB);
        helper.succeed();
    }

    /**
     * Gear shears carve a pumpkin ({@code PumpkinBlockMixin}).
     *
     * @param helper the test helper
     */
    @GameTest
    public void shearsCarvePumpkin(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, GearTestUtil.gear(GearItemSets.SHEARS, "iron"));
        helper.setBlock(TARGET, Blocks.PUMPKIN);
        helper.useBlock(TARGET, player);
        helper.assertBlockPresent(Blocks.CARVED_PUMPKIN, TARGET);
        helper.succeed();
    }

    /**
     * A gear axe strips a log.
     *
     * @param helper the test helper
     */
    @GameTest
    public void axeStripsLog(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, GearTestUtil.gear(GearItemSets.AXE, "iron"));
        helper.setBlock(TARGET, Blocks.OAK_LOG);
        helper.useBlock(TARGET, player);
        helper.assertBlockPresent(Blocks.STRIPPED_OAK_LOG, TARGET);
        helper.succeed();
    }

    /**
     * A broken gear axe does not strip a log.
     *
     * @param helper the test helper
     */
    @GameTest
    public void brokenAxeDoesNothing(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, broken(GearTestUtil.gear(GearItemSets.AXE, "iron")));
        helper.setBlock(TARGET, Blocks.OAK_LOG);
        helper.useBlock(TARGET, player);
        helper.assertBlockPresent(Blocks.OAK_LOG, TARGET);
        helper.succeed();
    }

    /**
     * A gear shovel makes a dirt path.
     *
     * @param helper the test helper
     */
    @GameTest
    public void shovelMakesPath(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, GearTestUtil.gear(GearItemSets.SHOVEL, "iron"));
        helper.setBlock(TARGET, Blocks.GRASS_BLOCK);
        helper.useBlock(TARGET, player);
        helper.assertBlockPresent(Blocks.DIRT_PATH, TARGET);
        helper.succeed();
    }

    /**
     * A gear hoe tills dirt.
     *
     * @param helper the test helper
     */
    @GameTest
    public void hoeTillsDirt(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, GearTestUtil.gear(GearItemSets.HOE, "iron"));
        helper.setBlock(TARGET, Blocks.DIRT);
        helper.useBlock(TARGET, player);
        helper.assertBlockPresent(Blocks.FARMLAND, TARGET);
        helper.succeed();
    }

    /**
     * A full strength attack with a gear sword also hurts a mob next to the target (gear swords are in
     * {@code minecraft:swords}, which is what vanilla's sweep check uses).
     *
     * @param helper the test helper
     */
    @GameTest
    public void swordSweeps(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, GearTestUtil.gear(GearItemSets.SWORD, "iron"));
        Pig target = helper.spawnWithNoFreeWill(EntityType.PIG, TARGET);
        Pig neighbor = helper.spawnWithNoFreeWill(EntityType.PIG, TARGET.south());
        player.setOnGround(true);
        player.fullyChargeAttack();
        player.attack(target);
        helper.assertTrue(target.getHealth() < target.getMaxHealth(), "the target was not hurt");
        helper.assertTrue(neighbor.getHealth() < neighbor.getMaxHealth(), "the neighbor was not hit by the sweep");
        helper.succeed();
    }

    /**
     * A cast gear fishing rod keeps its hook out ({@code FishingHookMixin}; without it the hook is removed on
     * the next tick because vanilla only accepts {@code minecraft:fishing_rod}).
     *
     * @param helper the test helper
     */
    @GameTest(maxTicks = 60)
    public void fishingHookStaysOut(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack rod = GearTestUtil.gear(GearItemSets.FISHING_ROD, "iron");
        player.setItemInHand(InteractionHand.MAIN_HAND, rod);
        rod.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.fishing != null, "no fishing hook was cast");

        helper.runAfterDelay(20, () -> {
            helper.assertTrue(player.fishing != null && !player.fishing.isRemoved(), "the fishing hook was removed");
            helper.succeed();
        });
    }
}
