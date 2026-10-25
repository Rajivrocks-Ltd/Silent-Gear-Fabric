package net.silentchaos512.gear.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.silentchaos512.gear.setup.GearItemSets;
import net.silentchaos512.gear.setup.SgItems;
import net.silentchaos512.gear.util.Const;

/**
 * M8 checklist: armor traits and the gear elytra.
 */
public class ArmorTests {
    private static final double FALL_DISTANCE = 10.0;

    /**
     * A gold gear helmet (Brilliant) keeps piglins neutral (Silent Lib's {@code PiglinAiMixin} asks
     * {@code makesPiglinsNeutral}).
     *
     * @param helper the test helper
     */
    @GameTest
    public void goldArmorCalmsPiglins(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        ItemStack helmet = GearTestUtil.gear(GearItemSets.HELMET, "gold");
        GearTestUtil.assertHasTrait(helper, helmet, Const.Traits.BRILLIANT);
        helper.assertFalse(PiglinAi.isWearingSafeArmor(player), "setup: no armor should not calm piglins");
        player.setItemSlot(EquipmentSlot.HEAD, helmet);
        helper.assertTrue(PiglinAi.isWearingSafeArmor(player), "a gold gear helmet should calm piglins");
        helper.succeed();
    }

    /**
     * Leather gear boots (Snow Walker) let the wearer walk on powder snow (Silent Lib's
     * {@code PowderSnowBlockMixin} asks {@code canWalkOnPowderedSnow}). Vanilla leather boots are checked too,
     * so a failure here can be told apart from a broken mixin.
     *
     * <p>Upstream never made this work (its check asks {@code getEquipmentSlot}, which returns no slot for
     * armor); fixed in the port as a "Port fix" in {@code GearArmorItem}, approved 2026-10-25.
     *
     * @param helper the test helper
     */
    @GameTest
    public void snowWalkerBootsWalkOnPowderSnow(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
        helper.assertTrue(PowderSnowBlock.canEntityWalkOnPowderSnow(player), "vanilla leather boots should walk on powder snow");

        ItemStack boots = GearTestUtil.gear(GearItemSets.BOOTS, "leather");
        GearTestUtil.assertHasTrait(helper, boots, Const.Traits.SNOW_WALKER);
        player.setItemSlot(EquipmentSlot.FEET, boots);
        helper.assertTrue(PowderSnowBlock.canEntityWalkOnPowderSnow(player), "leather gear boots (Snow Walker) should walk on powder snow");
        helper.succeed();
    }

    /**
     * Moonwalker boots (azure silver) take less fall damage than plain iron boots (Silent Lib's
     * {@code LivingEntityMixin} posts {@code LivingFallEvent}).
     *
     * @param helper the test helper
     */
    @GameTest
    public void moonwalkerReducesFallDamage(GameTestHelper helper) {
        ItemStack boots = GearTestUtil.gear(GearItemSets.BOOTS, "azure_silver");
        GearTestUtil.assertHasTrait(helper, boots, Const.Traits.MOONWALKER);
        float plainLoss = fallDamage(helper, GearTestUtil.gear(GearItemSets.BOOTS, "iron"));
        float moonLoss = fallDamage(helper, boots);
        helper.assertTrue(moonLoss < plainLoss, "Moonwalker fall damage " + moonLoss + " should be below " + plainLoss);
        helper.succeed();
    }

    /**
     * Boots with a slime lining (Bounce) cancel fall damage.
     *
     * @param helper the test helper
     */
    @GameTest
    public void bounceCancelsFallDamage(GameTestHelper helper) {
        ItemStack boots = GearTestUtil.gear(GearItemSets.BOOTS, "iron", GearTestUtil.part(SgItems.LINING, "slime"));
        GearTestUtil.assertHasTrait(helper, boots, Const.Traits.BOUNCE);
        float plainLoss = fallDamage(helper, GearTestUtil.gear(GearItemSets.BOOTS, "iron"));
        float bounceLoss = fallDamage(helper, boots);
        helper.assertTrue(plainLoss > 0, "setup: a 10 block fall should hurt, lost " + plainLoss + " (" + TestPlayer.survival(helper).describe() + ")");
        helper.assertValueEqual(bounceLoss, 0f, "fall damage with Bounce boots");
        helper.succeed();
    }

    /**
     * A gear elytra can be used to glide from the chest slot.
     *
     * @param helper the test helper
     */
    @GameTest
    public void elytraCanGlide(GameTestHelper helper) {
        ItemStack elytra = GearTestUtil.gear(GearItemSets.ELYTRA, "phantom_membrane");
        helper.assertTrue(elytra.getMaxDamage() > 0, "elytra should have durability");
        helper.assertTrue(LivingEntity.canGlideUsing(elytra, EquipmentSlot.CHEST), "a gear elytra should glide from the chest slot");
        helper.succeed();
    }

    private static float fallDamage(GameTestHelper helper, ItemStack boots) {
        TestPlayer player = TestPlayer.survival(helper);
        player.setItemSlot(EquipmentSlot.FEET, boots);
        float before = player.getHealth();
        player.causeFallDamage(FALL_DISTANCE, 1.0f, helper.getLevel().damageSources().fall());
        return before - player.getHealth();
    }
}
