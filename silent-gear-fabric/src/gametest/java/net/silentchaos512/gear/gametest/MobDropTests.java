package net.silentchaos512.gear.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.silentchaos512.gear.item.CraftingItems;

/**
 * M8 checklist: Silent Gear's extra mob drops ({@code SgLootModifiers} adds them to the vanilla tables with
 * Fabric's {@code LootTableEvents.MODIFY}). Each mob's death loot is rolled many times as if a player killed it,
 * because the drops are random (2% to 20% per kill).
 */
public class MobDropTests {
    private static final int ROLLS = 1000;

    /**
     * Cows, pigs and sheep drop sinew.
     *
     * @param helper the test helper
     */
    @GameTest
    public void farmAnimalsDropSinew(GameTestHelper helper) {
        assertDrops(helper, EntityType.COW, CraftingItems.SINEW.asItem());
        assertDrops(helper, EntityType.PIG, CraftingItems.SINEW.asItem());
        assertDrops(helper, EntityType.SHEEP, CraftingItems.SINEW.asItem());
        helper.succeed();
    }

    /**
     * Zombies and husks drop leather scraps.
     *
     * @param helper the test helper
     */
    @GameTest
    public void zombiesDropLeatherScraps(GameTestHelper helper) {
        assertDrops(helper, EntityType.ZOMBIE, CraftingItems.LEATHER_SCRAP.asItem());
        assertDrops(helper, EntityType.HUSK, CraftingItems.LEATHER_SCRAP.asItem());
        helper.succeed();
    }

    /**
     * Spiders drop fine silk.
     *
     * @param helper the test helper
     */
    @GameTest
    public void spidersDropFineSilk(GameTestHelper helper) {
        assertDrops(helper, EntityType.SPIDER, CraftingItems.FINE_SILK.asItem());
        helper.succeed();
    }

    private static void assertDrops(GameTestHelper helper, EntityType<? extends Mob> type, Item expected) {
        TestPlayer player = TestPlayer.survival(helper);
        Mob mob = helper.spawnWithNoFreeWill(type, new BlockPos(4, 1, 4));
        mob.setLastHurtByPlayer(player, 100);
        var source = helper.getLevel().damageSources().playerAttack(player);
        var table = mob.getLootTable().orElseThrow();
        int[] found = {0};
        for (int i = 0; i < ROLLS; i++) {
            mob.dropFromLootTable(helper.getLevel(), source, true, table, (ItemStack stack) -> {
                if (stack.is(expected)) {
                    found[0] += stack.getCount();
                }
            });
        }
        mob.discard();
        helper.assertTrue(found[0] > 0, type.getDescriptionId() + " never dropped " + expected + " in " + ROLLS + " player kills");
    }
}
