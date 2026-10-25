package net.silentchaos512.gear.gametest;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketSlotUtils;
import eu.pb4.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.silentchaos512.gear.setup.GearItemSets;
import net.silentchaos512.gear.setup.SgAttributes;
import net.silentchaos512.gear.setup.SgItems;
import net.silentchaos512.gear.util.Const;

import java.util.ArrayList;
import java.util.List;

/**
 * M8 checklist: Trinkets support (replaces upstream's Curios support). Trinkets is a dev runtime mod, so it is
 * loaded on the gametest server too.
 */
public class TrinketsTests {
    private static final String RING = "hand/ring";
    private static final String BRACELET = "hand/bracelet";
    private static final String NECKLACE = "chest/necklace";
    private static final String BACK = "chest/back";

    private static TrinketInventory inventory(GameTestHelper helper, TestPlayer player, String slotId) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        TrinketInventory inventory = attachment.getInventory(slotId);
        helper.assertTrue(inventory != null && inventory.getContainerSize() > 0,
                "player has no " + slotId + " slot; slots: " + attachment.getInventories().keySet());
        return inventory;
    }

    /**
     * Players have the ring, bracelet, necklace and back slots ({@code data/trinkets/entities/silentgear.json}).
     *
     * @param helper the test helper
     */
    @GameTest
    public void playerHasSilentGearSlots(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        for (String slot : List.of(RING, BRACELET, NECKLACE, BACK)) {
            inventory(helper, player, slot);
        }
        helper.succeed();
    }

    /**
     * Each gear trinket fits its own slot (the slot tags), and a ring does not fit the necklace slot.
     *
     * @param helper the test helper
     */
    @GameTest
    public void gearFitsItsSlot(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        assertFits(helper, player, RING, GearTestUtil.gear(GearItemSets.RING, "gold"), true);
        assertFits(helper, player, BRACELET, GearTestUtil.gear(GearItemSets.BRACELET, "gold"), true);
        assertFits(helper, player, NECKLACE, GearTestUtil.gear(GearItemSets.NECKLACE, "gold"), true);
        assertFits(helper, player, BACK, GearTestUtil.gear(GearItemSets.ELYTRA, "phantom_membrane"), true);
        assertFits(helper, player, NECKLACE, GearTestUtil.gear(GearItemSets.RING, "gold"), false);
        helper.succeed();
    }

    private static void assertFits(GameTestHelper helper, TestPlayer player, String slotId, ItemStack stack, boolean expected) {
        var access = inventory(helper, player, slotId).getSlotAccess(0);
        helper.assertValueEqual(TrinketSlotUtils.mayPlace(access, stack), expected, stack.getItem() + " fits " + slotId);
    }

    /**
     * Using a gear ring from the hand puts it in the ring slot.
     *
     * @param helper the test helper
     */
    @GameTest
    public void useEquipsRing(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack ring = GearTestUtil.gear(GearItemSets.RING, "gold");
        player.setItemInHand(InteractionHand.MAIN_HAND, ring);
        ring.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        ItemStack equipped = inventory(helper, player, RING).getItem(0);
        helper.assertTrue(equipped.is(GearItemSets.RING.gearItem()), "ring slot holds " + equipped + " after using the ring");
        helper.succeed();
    }

    /**
     * Gear trinkets stay with the player on death (drop rule KEEP, like upstream's Curios setting).
     *
     * @param helper the test helper
     */
    @GameTest
    public void trinketsAreKeptOnDeath(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack ring = GearTestUtil.gear(GearItemSets.RING, "gold");
        var access = inventory(helper, player, RING).getSlotAccess(0);
        access.set(ring);
        helper.assertValueEqual(TrinketsApi.getDropRule(ring, access, player, false), TrinketDropRule.KEEP, "ring drop rule");
        helper.succeed();
    }

    /**
     * A gold ring (Brilliant) in the ring slot keeps piglins neutral (Silent Gear's own {@code PiglinAiMixin}).
     *
     * @param helper the test helper
     */
    @GameTest
    public void goldRingCalmsPiglins(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack ring = GearTestUtil.gear(GearItemSets.RING, "gold");
        GearTestUtil.assertHasTrait(helper, ring, Const.Traits.BRILLIANT);
        inventory(helper, player, RING).getSlotAccess(0).set(ring);
        helper.assertTrue(PiglinAi.isWearingSafeArmor(player), "a gold ring should calm piglins");
        helper.succeed();
    }

    /**
     * A ring with a prismarine setting (Swift Swim) raises the wearer's swim speed once Trinkets applies its
     * modifiers on the player's tick.
     *
     * @param helper the test helper
     */
    @GameTest
    public void swiftSwimRingRaisesSwimSpeed(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack ring = GearTestUtil.gear(GearItemSets.RING, "gold", GearTestUtil.part(SgItems.SETTING, "prismarine"));
        GearTestUtil.assertHasTrait(helper, ring, Const.Traits.SWIFT_SWIM);
        var swimSpeed = SgAttributes.SWIM_SPEED.getDelegate();
        // getAttribute creates the instance (AttributeMapMixin); getAttributeValue alone throws if it is missing.
        double before = player.getAttribute(swimSpeed).getValue();

        inventory(helper, player, RING).getSlotAccess(0).set(ring);
        for (int i = 0; i < 3; i++) {
            player.doTick();
        }
        double after = player.getAttribute(swimSpeed).getValue();
        helper.assertTrue(after > before, "swim speed " + after + " should be above " + before);
        helper.succeed();
    }

    /**
     * With Trinkets installed, the ring tooltip does not say "Requires Trinkets to equip".
     *
     * @param helper the test helper
     */
    @GameTest
    public void ringTooltipHasNoMissingModWarning(GameTestHelper helper) {
        ItemStack ring = GearTestUtil.gear(GearItemSets.RING, "gold");
        List<Component> lines = new ArrayList<>();
        ring.getItem().appendHoverText(ring, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, lines::add, TooltipFlag.NORMAL);
        for (Component line : lines) {
            if (line.getContents() instanceof TranslatableContents translatable) {
                helper.assertFalse(translatable.getKey().contains("curiosNotInstalled"), "tooltip still warns: " + line.getString());
            }
        }
        helper.succeed();
    }
}
