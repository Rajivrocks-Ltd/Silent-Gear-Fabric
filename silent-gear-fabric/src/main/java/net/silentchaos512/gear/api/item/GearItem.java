package net.silentchaos512.gear.api.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ItemLike;
import net.silentchaos512.gear.api.part.PartType;
import net.silentchaos512.gear.api.property.NumberProperty;
import net.silentchaos512.gear.api.traits.TraitActionContext;
import net.silentchaos512.gear.core.component.GearConstructionData;
import net.silentchaos512.gear.core.component.GearPropertiesData;
import net.silentchaos512.gear.gear.part.PartInstance;
import net.silentchaos512.gear.setup.gear.PartTypes;
import net.silentchaos512.gear.util.GearData;
import net.silentchaos512.gear.util.TraitHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Interface for all equipment items, including tools and armor.
 */
public interface GearItem extends ItemLike {
    Supplier<Collection<PartType>> REQUIRED_PARTS = Suppliers.memoize(() -> ImmutableList.of(
            PartTypes.MAIN.get()
    ));

    //region Item properties and construction

    default ItemStack construct(Collection<PartInstance> parts) {
        ItemStack result = new ItemStack(this);
        GearData.writeConstructionParts(result, parts);
        GearData.recalculateGearData(result, null);
        parts.forEach(p -> {
            if (p != null) {
                p.onAddToGear(result);
            }
        });
        // Allow traits to make any needed changes (must be done after a recalculate)
        TraitHelper.activateTraits(result, 0, (trait, value) -> {
            trait.getTrait().onGearCrafted(new TraitActionContext(null, trait, result));
            return 0;
        });
        return result;
    }

    @Override
    default Item asItem() {
        return (Item) this;
    }

    GearType getGearType();

    default boolean requiresPartOfType(PartType type) {
        return getRequiredParts().contains(type);
    }

    default boolean supportsPart(ItemStack gear, PartInstance part) {
        if (!part.isValid()) return false;
        boolean canAdd = part.get().canAddToGear(gear, part);
        return (requiresPartOfType(part.getType()) && canAdd) || canAdd;
    }

    default Collection<PartType> getRequiredParts() {
        return REQUIRED_PARTS.get();
    }

    default void onRecalculatePre(ItemStack gear, @Nullable Player player, @Nullable GearPropertiesData oldProperties, GearConstructionData constructionData) {
    }

    default void onRecalculatePost(ItemStack gear, @Nullable Player player, GearPropertiesData finalProperties) {
    }

    /**
     * Adds the item's own attribute modifiers (for example armor values). Trait modifiers are added afterwards by
     * {@code GearData.setGearAttributeModifiers}, for every gear item.
     *
     * <p>Port fix (2026-10-25): upstream's default also added every trait's modifiers here, so each
     * trait modifier was stored twice (same id) and its tooltip line showed twice, for example "-60% Gravity" on
     * Moonwalker boots. Only one copy ever applied in play. The default now adds nothing.
     *
     * @param gear    the gear item
     * @param builder receives the modifiers
     */
    default void buildAttributes(ItemStack gear, ItemAttributeModifiers.Builder builder) {
    }

    //endregion

    //region Stats and config

    default Supplier<NumberProperty> getDurabilityStat() {
        return getGearType().durabilityStat();
    }

    default float getRepairModifier(ItemInstance instance) {
        return 1f;
    }

    //endregion
}
