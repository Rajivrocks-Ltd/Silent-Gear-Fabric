package net.silentchaos512.gear.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.silentchaos512.gear.setup.GearItemSets;
import net.silentchaos512.gear.setup.SgItems;
import net.silentchaos512.gear.util.Const;
import net.silentchaos512.gear.util.TraitHelper;
import net.silentchaos512.lib.port.event.NeoForge;
import net.silentchaos512.lib.port.event.level.BlockDropsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * M8 checklist: traits that change mining speed, experience, drops and fuel.
 */
public class TraitTests {
    private static final BlockPos TARGET = new BlockPos(2, 1, 1);

    /**
     * Lustrous (diamond) mines faster in light than in the dark ({@code BlockBehaviourMixin} posts
     * {@code BreakSpeed} from {@code getDestroyProgress}). The dark spot is a stone box with no light inside;
     * the light that counts is at the player, not at the mined block.
     *
     * @param helper the test helper
     */
    @GameTest(maxTicks = 100)
    public void lustrousIsFasterInLight(GameTestHelper helper) {
        ItemStack pickaxe = GearTestUtil.gear(GearItemSets.PICKAXE, "diamond");
        GearTestUtil.assertHasTrait(helper, pickaxe, Const.Traits.LUSTROUS);

        BlockPos lit = new BlockPos(1, 1, 1);
        helper.setBlock(new BlockPos(1, 1, 2), Blocks.GLOWSTONE);
        BlockPos dark = new BlockPos(5, 2, 5);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        helper.setBlock(dark.offset(x, y, z), Blocks.STONE);
                    }
                }
            }
        }

        // Give the light engine time to update after placing blocks.
        helper.runAfterDelay(20, () -> {
            BlockState stone = Blocks.STONE.defaultBlockState();
            BlockPos minedPos = helper.absolutePos(TARGET);
            TestPlayer litPlayer = TestPlayer.create(helper, net.minecraft.world.level.GameType.SURVIVAL, lit);
            litPlayer.setItemInHand(InteractionHand.MAIN_HAND, pickaxe.copy());
            TestPlayer darkPlayer = TestPlayer.create(helper, net.minecraft.world.level.GameType.SURVIVAL, dark);
            darkPlayer.setItemInHand(InteractionHand.MAIN_HAND, pickaxe.copy());

            float litSpeed = stone.getDestroyProgress(litPlayer, helper.getLevel(), minedPos);
            float darkSpeed = stone.getDestroyProgress(darkPlayer, helper.getLevel(), minedPos);
            helper.assertTrue(litSpeed > darkSpeed, "lit speed " + litSpeed + " should be above dark speed " + darkSpeed
                    + " (light lit=" + helper.getLevel().getMaxLocalRawBrightness(litPlayer.blockPosition())
                    + ", dark=" + helper.getLevel().getMaxLocalRawBrightness(darkPlayer.blockPosition()) + ")");
            helper.succeed();
        });
    }

    /**
     * Ancient (stone) adds 25% per trait level to the experience a block drops. The event is posted directly with a
     * known amount, so the result does not depend on random ore experience.
     *
     * @param helper the test helper
     */
    @GameTest
    public void ancientBoostsBlockExperience(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack pickaxe = GearTestUtil.gear(GearItemSets.PICKAXE, "stone");
        GearTestUtil.assertHasTrait(helper, pickaxe, Const.Traits.ANCIENT);
        BlockPos pos = helper.absolutePos(TARGET);

        BlockDropsEvent event = new BlockDropsEvent(helper.getLevel(), pos, Blocks.COAL_ORE.defaultBlockState(), null, new ArrayList<>(), player, pickaxe);
        int level = TraitHelper.getTraitLevel(pickaxe, Const.Traits.ANCIENT);
        event.setDroppedExperience(100);
        NeoForge.EVENT_BUS.post(event);
        helper.assertValueEqual(event.getDroppedExperience(), 100 + (int) (100 * Const.Traits.ANCIENT_XP_BOOST * level),
                "experience with Ancient " + level);
        helper.succeed();
    }

    /**
     * Breaking an ore really drops experience orbs through Silent Lib's block drops hook.
     *
     * @param helper the test helper
     */
    @GameTest
    public void minedOreDropsExperience(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, GearTestUtil.gear(GearItemSets.PICKAXE, "stone"));
        for (int i = 0; i < 4; i++) {
            BlockPos pos = new BlockPos(2 + i, 1, 3);
            helper.setBlock(pos, Blocks.LAPIS_ORE);
            player.gameMode.destroyBlock(helper.absolutePos(pos));
        }
        int xp = GearTestUtil.totalExperience(helper);
        helper.assertTrue(xp > 0, "4 lapis ores dropped no experience");
        helper.succeed();
    }

    /**
     * Ancient on the killer's tool adds experience to a mob kill ({@code LivingEntityMixin} posts
     * {@code LivingExperienceDropEvent}). An adult zombie with no equipment drops exactly 5, plus 25% per level.
     *
     * @param helper the test helper
     */
    @GameTest(maxTicks = 60)
    public void ancientBoostsMobExperience(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack pickaxe = GearTestUtil.gear(GearItemSets.PICKAXE, "stone");
        GearTestUtil.assertHasTrait(helper, pickaxe, Const.Traits.ANCIENT);
        player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 4));
        zombie.setBaby(false);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            zombie.setItemSlot(slot, ItemStack.EMPTY);
        }
        zombie.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1000f);

        int level = TraitHelper.getTraitLevel(pickaxe, Const.Traits.ANCIENT);
        int expected = 5 + (int) (5 * Const.Traits.ANCIENT_XP_BOOST * level);
        helper.runAfterDelay(40, () -> {
            int xp = GearTestUtil.totalExperience(helper);
            helper.assertValueEqual(xp, expected, "zombie experience with Ancient " + level + " (5 without)");
            helper.succeed();
        });
    }

    /**
     * Gold Digger (dimerald) gives extra gold nuggets from nether gold ore (the {@code bonus_drops_trait} loot
     * modifier through Silent Lib's {@code LootTableMixin}). Compared with an iron pickaxe over many rolls.
     *
     * @param helper the test helper
     */
    @GameTest
    public void goldDiggerGivesExtraNuggets(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack goldDigger = GearTestUtil.gear(GearItemSets.PICKAXE, "dimerald");
        GearTestUtil.assertHasTrait(helper, goldDigger, Const.Traits.GOLD_DIGGER);
        ItemStack plain = GearTestUtil.gear(GearItemSets.PICKAXE, "iron");

        int withTrait = countDrops(helper, player, Blocks.NETHER_GOLD_ORE.defaultBlockState(), goldDigger, Items.GOLD_NUGGET, 400);
        int without = countDrops(helper, player, Blocks.NETHER_GOLD_ORE.defaultBlockState(), plain, Items.GOLD_NUGGET, 400);
        helper.assertTrue(withTrait > without * 1.05, "nuggets with Gold Digger " + withTrait + " should clearly beat " + without);
        helper.succeed();
    }

    /**
     * Magmatic (a crimson steel tip) smelts iron ore drops into iron ingots.
     *
     * @param helper the test helper
     */
    @GameTest
    public void magmaticSmeltsDrops(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack pickaxe = GearTestUtil.gear(GearItemSets.PICKAXE, "iron", GearTestUtil.part(SgItems.TIP, "crimson_steel"));
        GearTestUtil.assertHasTrait(helper, pickaxe, Const.Traits.MAGMATIC);

        List<ItemStack> drops = drops(helper, player, Blocks.IRON_ORE.defaultBlockState(), pickaxe);
        helper.assertTrue(drops.stream().anyMatch(s -> s.is(Items.IRON_INGOT)), "iron ore drops should be smelted, got " + drops);
        helper.assertFalse(drops.stream().anyMatch(s -> s.is(Items.RAW_IRON)), "raw iron should be gone, got " + drops);
        helper.succeed();
    }

    /**
     * Silky (brass) mines glass like Silk Touch (the trait adds the enchantment through Silent Lib's
     * {@code EnchantmentHelperMixin}). An iron pickaxe gets nothing from glass.
     *
     * @param helper the test helper
     */
    @GameTest
    public void silkyActsLikeSilkTouch(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack silky = GearTestUtil.gear(GearItemSets.PICKAXE, "brass");
        GearTestUtil.assertHasTrait(helper, silky, Const.Traits.SILKY);

        List<ItemStack> withTrait = drops(helper, player, Blocks.GLASS.defaultBlockState(), silky);
        List<ItemStack> plain = drops(helper, player, Blocks.GLASS.defaultBlockState(), GearTestUtil.gear(GearItemSets.PICKAXE, "iron"));
        helper.assertTrue(withTrait.stream().anyMatch(s -> s.is(Items.GLASS)), "Silky should drop glass, got " + withTrait);
        helper.assertTrue(plain.isEmpty(), "an iron pickaxe should get nothing from glass, got " + plain);
        helper.succeed();
    }

    /**
     * Flammable (wood) gear burns in a furnace ({@code FuelValuesMixin} posts {@code FurnaceFuelBurnTimeEvent});
     * iron gear does not.
     *
     * @param helper the test helper
     */
    @GameTest
    public void woodGearIsFuel(GameTestHelper helper) {
        ItemStack wood = GearTestUtil.gear(GearItemSets.SWORD, "wood");
        GearTestUtil.assertHasTrait(helper, wood, Const.Traits.FLAMMABLE);
        var fuel = helper.getLevel().fuelValues();
        helper.assertTrue(fuel.isFuel(wood), "a wood sword should be fuel");
        helper.assertTrue(fuel.burnDuration(wood) > 0, "wood sword burn time is " + fuel.burnDuration(wood));
        helper.assertFalse(fuel.isFuel(GearTestUtil.gear(GearItemSets.SWORD, "iron")), "an iron sword should not be fuel");
        helper.succeed();
    }

    private static List<ItemStack> drops(GameTestHelper helper, TestPlayer player, BlockState state, ItemStack tool) {
        return Block.getDrops(state, helper.getLevel(), helper.absolutePos(TARGET), null, player, tool);
    }

    private static int countDrops(GameTestHelper helper, TestPlayer player, BlockState state, ItemStack tool, net.minecraft.world.item.Item item, int rolls) {
        int count = 0;
        for (int i = 0; i < rolls; i++) {
            for (ItemStack stack : drops(helper, player, state, tool)) {
                if (stack.is(item)) {
                    count += stack.getCount();
                }
            }
        }
        return count;
    }
}
