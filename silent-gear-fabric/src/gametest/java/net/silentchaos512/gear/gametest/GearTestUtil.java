package net.silentchaos512.gear.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.silentchaos512.gear.api.item.GearItem;
import net.silentchaos512.gear.api.util.DataResource;
import net.silentchaos512.gear.gear.material.MaterialInstance;
import net.silentchaos512.gear.gear.part.PartInstance;
import net.silentchaos512.gear.gear.trait.Trait;
import net.silentchaos512.gear.item.CompoundPartItem;
import net.silentchaos512.gear.item.GearItemSet;
import net.silentchaos512.gear.setup.SgItems;
import net.silentchaos512.gear.setup.gear.PartTypes;
import net.silentchaos512.gear.util.TraitHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Shared helpers for Silent Gear gametests.
 */
public final class GearTestUtil {
    private GearTestUtil() {
    }

    /**
     * Builds a gear item from a main material and optional extra parts, the way crafting does.
     *
     * <p>The main part is made of three of the main material, like a crafted head. Parts the gear type requires
     * but that are not given use the creative tab defaults: a wood rod, a string cord or binding, and a diamond
     * setting.
     *
     * @param set          the gear item set, for example {@code GearItemSets.PICKAXE}
     * @param materialPath the main material path in the {@code silentgear} namespace, for example "iron"
     * @param extraParts   extra parts, for example a tip made with {@link #part}
     * @return the gear stack, with its properties and traits calculated
     */
    public static ItemStack gear(GearItemSet<?> set, String materialPath, PartInstance... extraParts) {
        GearItem item = set.gearItem();
        List<PartInstance> parts = new ArrayList<>();
        parts.add(PartInstance.from(set.mainPart().create(MaterialInstance.of(DataResource.material(materialPath)), 3)));
        List<PartInstance> extras = List.of(extraParts);
        parts.addAll(extras);
        addDefault(item, parts, extras, SgItems.ROD, "wood");
        addDefault(item, parts, extras, SgItems.CORD, "string");
        addDefault(item, parts, extras, SgItems.BINDING, "string");
        addDefault(item, parts, extras, SgItems.SETTING, "diamond");
        return item.construct(parts);
    }

    private static void addDefault(GearItem item, List<PartInstance> parts, List<PartInstance> extras,
                                   Supplier<CompoundPartItem> partItem, String materialPath) {
        var partType = partItem.get().getPartType();
        boolean given = extras.stream().anyMatch(p -> p.getType() == partType);
        if (!given && item.requiresPartOfType(partType)) {
            parts.add(part(partItem, materialPath));
        }
    }

    /**
     * Makes a part from one material.
     *
     * @param partItem     the part item, for example {@code SgItems.TIP}
     * @param materialPath the material path in the {@code silentgear} namespace
     * @return the part
     */
    public static PartInstance part(Supplier<CompoundPartItem> partItem, String materialPath) {
        return PartInstance.from(partItem.get().create(MaterialInstance.of(DataResource.material(materialPath))));
    }

    /**
     * Checks that two stacks save to the same data. Comparing the stacks directly is not enough: a gear tool's
     * mining rules hold block sets made from a separate registry lookup, which never compare equal to the ones read
     * back, even though they save the same.
     *
     * @param helper the test helper
     * @param a      one stack
     * @param b      the other stack
     * @return true if both save to the same data
     */
    public static boolean sameWhenSaved(GameTestHelper helper, ItemStack a, ItemStack b) {
        var ops = helper.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
        return ItemStack.OPTIONAL_CODEC.encodeStart(ops, a).getOrThrow().equals(ItemStack.OPTIONAL_CODEC.encodeStart(ops, b).getOrThrow());
    }

    /**
     * Fails the test unless the stack has the trait, so a later failure is not blamed on the wrong thing.
     *
     * @param helper the test helper
     * @param stack  the gear item
     * @param trait  the trait
     */
    public static void assertHasTrait(GameTestHelper helper, ItemStack stack, DataResource<Trait> trait) {
        helper.assertTrue(TraitHelper.getTraitLevel(stack, trait) > 0,
                stack.getItem() + " should have trait " + trait.getId() + ", traits: " + TraitHelper.getTraits(stack));
    }

    /**
     * Reads how many orbs were merged into this one (vanilla keeps it private and has no getter).
     */
    private static int orbCount(ExperienceOrb orb) {
        try {
            Field field = ExperienceOrb.class.getDeclaredField("count");
            field.setAccessible(true);
            return field.getInt(orb);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("ExperienceOrb.count not found", e);
        }
    }

    /**
     * Adds up the experience in all orbs inside the test area.
     *
     * @param helper the test helper
     * @return the total experience
     */
    public static int totalExperience(GameTestHelper helper) {
        int total = 0;
        for (ExperienceOrb orb : helper.getLevel().getEntitiesOfClass(ExperienceOrb.class, helper.getBoundsWithPadding())) {
            total += orb.getValue() * orbCount(orb);
        }
        return total;
    }
}
