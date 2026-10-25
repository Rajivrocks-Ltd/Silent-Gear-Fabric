package net.silentchaos512.gear.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.silentchaos512.gear.block.SgContainerBlockEntity;
import net.silentchaos512.gear.block.alloymaker.AlloyMakerBlockEntity;
import net.silentchaos512.gear.setup.GearItemSets;
import net.silentchaos512.gear.setup.SgBlocks;
import net.silentchaos512.gear.item.CraftingItems;

import java.util.ArrayList;
import java.util.List;

/**
 * M11 crafting station checks: every machine opens its menu and survives shift-clicking, keeps its contents
 * through a save and load, and the alloy forge and salvager do their work. Hoppers feed and empty a machine.
 */
public class StationTests {
    private static final BlockPos MACHINE = new BlockPos(2, 2, 2);

    private static List<Block> containerBlocks(GameTestHelper helper) {
        List<Block> blocks = new ArrayList<>();
        BlockPos pos = helper.absolutePos(MACHINE);
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("silentgear")) continue;
            if (block instanceof net.minecraft.world.level.block.EntityBlock entityBlock
                    && entityBlock.newBlockEntity(pos, block.defaultBlockState()) instanceof SgContainerBlockEntity) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    /**
     * Every Silent Gear machine (alloy makers, grader, press, paint mixer, salvager, charger) opens its menu, and
     * shift-clicking every slot, with an item in the player's inventory, does not crash.
     *
     * @param helper the test helper
     */
    @GameTest(maxTicks = 100)
    public void everyMachineMenuOpensAndShiftClicks(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        List<String> problems = new ArrayList<>();
        List<Block> blocks = containerBlocks(helper);
        helper.assertTrue(blocks.size() >= 10, "only " + blocks.size() + " machines found");
        for (Block block : blocks) {
            String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
            helper.setBlock(MACHINE, block);
            BlockEntity be = helper.getLevel().getBlockEntity(helper.absolutePos(MACHINE));
            if (!(be instanceof SgContainerBlockEntity container)) {
                problems.add(name + ": no block entity");
                continue;
            }
            try {
                AbstractContainerMenu menu = container.createMenu(1, player.getInventory(), player);
                if (menu == null) {
                    problems.add(name + ": no menu");
                    continue;
                }
                player.getInventory().setItem(9, new ItemStack(Items.IRON_INGOT, 16));
                for (int i = 0; i < menu.slots.size(); i++) {
                    menu.quickMoveStack(player, i);
                }
                menu.removed(player);
            } catch (Exception e) {
                problems.add(name + ": " + e);
            }
            helper.setBlock(MACHINE, Blocks.AIR);
        }
        helper.assertTrue(problems.isEmpty(), "menu problems: " + problems);
        helper.succeed();
    }

    /**
     * Every machine keeps the item in its first slot through a save and load of its block entity (world save and
     * reload, chunk unload).
     *
     * @param helper the test helper
     */
    @GameTest(maxTicks = 100)
    public void everyMachineKeepsItsContents(GameTestHelper helper) {
        List<String> problems = new ArrayList<>();
        var registries = helper.getLevel().registryAccess();
        for (Block block : containerBlocks(helper)) {
            String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
            helper.setBlock(MACHINE, block);
            BlockPos pos = helper.absolutePos(MACHINE);
            SgContainerBlockEntity be = (SgContainerBlockEntity) helper.getLevel().getBlockEntity(pos);
            ItemStack stored = GearTestUtil.gear(GearItemSets.PICKAXE, "iron");
            be.setItem(0, stored.copy());
            var tag = be.saveWithFullMetadata(registries);
            BlockEntity loaded = BlockEntity.loadStatic(pos, be.getBlockState(), tag, registries);
            if (!(loaded instanceof SgContainerBlockEntity copy)) {
                problems.add(name + ": did not load");
            } else if (!GearTestUtil.sameWhenSaved(helper, copy.getItem(0), stored)) {
                problems.add(name + ": slot 0 became " + copy.getItem(0));
            }
            be.clearContent();
            helper.setBlock(MACHINE, Blocks.AIR);
        }
        helper.assertTrue(problems.isEmpty(), "save/load problems: " + problems);
        helper.succeed();
    }

    /**
     * The alloy forge makes azure electrum from an azure silver block, two gold ingots and an ender pearl.
     *
     * @param helper the test helper
     */
    @GameTest(maxTicks = 400)
    public void alloyForgeMakesAlloy(GameTestHelper helper) {
        helper.setBlock(MACHINE, SgBlocks.ALLOY_FORGE.get());
        var forge = (AlloyMakerBlockEntity<?>) helper.getLevel().getBlockEntity(helper.absolutePos(MACHINE));
        forge.setItem(0, new ItemStack(SgBlocks.AZURE_SILVER_BLOCK.get()));
        forge.setItem(1, new ItemStack(Items.GOLD_INGOT));
        forge.setItem(2, new ItemStack(Items.GOLD_INGOT));
        forge.setItem(3, new ItemStack(Items.ENDER_PEARL));
        helper.succeedWhen(() -> {
            ItemStack output = forge.getItem(forge.getOutputSlotIndex());
            helper.assertTrue(output.is(CraftingItems.AZURE_ELECTRUM_INGOT.asItem()) && output.getCount() == 3,
                    "output is " + output);
        });
    }

    /**
     * The salvager breaks a gear pickaxe back into items.
     *
     * @param helper the test helper
     */
    @GameTest(maxTicks = 400)
    public void salvagerSalvagesGear(GameTestHelper helper) {
        helper.setBlock(MACHINE, SgBlocks.SALVAGER.get());
        var salvager = (SgContainerBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(MACHINE));
        salvager.setItem(0, GearTestUtil.gear(GearItemSets.PICKAXE, "iron"));
        helper.succeedWhen(() -> {
            helper.assertTrue(salvager.getItem(0).isEmpty(), "the pickaxe is still in the input slot");
            boolean anyOutput = false;
            for (int i = 1; i < salvager.getContainerSize(); i++) {
                anyOutput |= !salvager.getItem(i).isEmpty();
            }
            helper.assertTrue(anyOutput, "no salvaged items");
        });
    }

    /**
     * A hopper above a machine puts items in, and a hopper below takes items out (vanilla container rules; upstream's
     * NeoForge item handlers are commented out, so NeoForge uses the same rules).
     *
     * @param helper the test helper
     */
    @GameTest(maxTicks = 200)
    public void hoppersFeedAndEmptyMachines(GameTestHelper helper) {
        helper.setBlock(MACHINE, SgBlocks.ALLOY_FORGE.get());
        helper.setBlock(MACHINE.above(), Blocks.HOPPER);
        helper.setBlock(MACHINE.below(), Blocks.HOPPER);
        var forge = (AlloyMakerBlockEntity<?>) helper.getLevel().getBlockEntity(helper.absolutePos(MACHINE));
        var above = (HopperBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(MACHINE.above()));
        var below = (HopperBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(MACHINE.below()));
        above.setItem(0, new ItemStack(Items.GOLD_INGOT));
        forge.setItem(forge.getOutputSlotIndex(), new ItemStack(CraftingItems.AZURE_ELECTRUM_INGOT.asItem()));
        helper.succeedWhen(() -> {
            // A hopper below a plain container can also pull inputs, so gold that went in may already be below.
            boolean goldDelivered = above.getItem(0).isEmpty()
                    && (containsItem(forge, 0, forge.getInputSlotCount(), Items.GOLD_INGOT)
                    || containsItem(below, 0, below.getContainerSize(), Items.GOLD_INGOT));
            boolean outputTaken = containsItem(below, 0, below.getContainerSize(), CraftingItems.AZURE_ELECTRUM_INGOT.asItem());
            helper.assertTrue(goldDelivered, "the hopper above did not put the gold ingot in");
            helper.assertTrue(outputTaken, "the hopper below did not take the output");
        });
    }

    private static boolean containsItem(net.minecraft.world.Container container, int from, int to, net.minecraft.world.item.Item item) {
        for (int i = from; i < to; i++) {
            if (container.getItem(i).is(item)) return true;
        }
        return false;
    }
}
