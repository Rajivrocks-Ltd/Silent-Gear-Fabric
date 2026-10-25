package net.silentchaos512.gear.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.silentchaos512.gear.setup.GearItemSets;

/**
 * Checks that the gametest setup itself works: data is loaded, gear can be built, and the test player behaves
 * like a survival player with vanilla items. If a vanilla control fails, the other test results mean nothing.
 */
public class SmokeTests {
    /**
     * An iron pickaxe built in code has durability, so materials and parts loaded from data.
     *
     * @param helper the test helper
     */
    @GameTest
    public void ironPickaxeHasDurability(GameTestHelper helper) {
        var pickaxe = GearTestUtil.gear(GearItemSets.PICKAXE, "iron");
        helper.assertTrue(pickaxe.getMaxDamage() > 0, "iron pickaxe max damage is " + pickaxe.getMaxDamage());
        helper.succeed();
    }

    /**
     * Control: a vanilla iron pickaxe loses durability when the test player mines stone.
     *
     * @param helper the test helper
     */
    @GameTest
    public void vanillaPickaxeLosesDurability(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
        BlockPos pos = new BlockPos(2, 1, 1);
        helper.setBlock(pos, Blocks.STONE);
        player.gameMode.destroyBlock(helper.absolutePos(pos));
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 1,
                "vanilla pickaxe damage " + player.getMainHandItem().getDamageValue() + " (" + player.describe() + ")");
        helper.succeed();
    }

    /**
     * Control: a player standing inside powder snow with no armor freezes and takes damage (7 seconds to freeze,
     * then damage every 2 seconds). Checks that Silent Lib's damage hooks do not block freeze damage.
     *
     * @param helper the test helper
     */
    @GameTest(maxTicks = 300)
    public void playerFreezesInPowderSnow(GameTestHelper helper) {
        BlockPos feet = new BlockPos(1, 1, 1);
        helper.setBlock(feet, Blocks.POWDER_SNOW);
        helper.setBlock(feet.above(), Blocks.POWDER_SNOW);
        TestPlayer player = TestPlayer.create(helper, net.minecraft.world.level.GameType.SURVIVAL, feet);
        float before = player.getHealth();
        helper.onEachTick(player::doTick);
        helper.runAfterDelay(250, () -> {
            helper.assertTrue(player.isFullyFrozen(), "player is not frozen after 250 ticks, frozen ticks " + player.getTicksFrozen());
            helper.assertTrue(player.getHealth() < before, "frozen player took no damage, health " + player.getHealth());
            helper.succeed();
        });
    }

    /**
     * Control: the test player takes fall damage with no boots.
     *
     * @param helper the test helper
     */
    @GameTest
    public void playerTakesFallDamage(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        float before = player.getHealth();
        boolean hurt = player.causeFallDamage(10.0, 1.0f, helper.getLevel().damageSources().fall());
        helper.assertTrue(player.getHealth() < before, "no fall damage: hurt=" + hurt + " health " + player.getHealth()
                + " (" + player.describe() + ", difficulty " + helper.getLevel().getDifficulty() + ")");
        helper.succeed();
    }
}
