package net.silentchaos512.lib.port;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Extra item methods that NeoForge adds to every {@link Item}, for the Fabric port.
 *
 * <p>Silent Lib injects this interface into {@link Item} (a mixin plus Loom interface injection),
 * so every item has these methods and subclasses can override them and call {@code super}, as on
 * NeoForge. The defaults here behave like vanilla. Silent Lib mixins make vanilla code call the
 * methods; until a hook exists for a method, an override has no effect. Each method says which
 * hook calls it.
 *
 * <p>This is an independent implementation with NeoForge's names, not a copy of NeoForge code.
 */
public interface IItemExtension {
    private Item self() {
        return (Item) this;
    }

    /**
     * Gets the current damage of a stack of this item.
     *
     * <p>Hook: not wired. No Silent Gear item overrides it, and the default reads the same component as vanilla.
     *
     * @param stack the stack
     * @return the damage, between 0 and the max damage
     */
    default int getDamage(ItemStack stack) {
        return Mth.clamp(stack.getOrDefault(DataComponents.DAMAGE, 0), 0, stack.getMaxDamage());
    }

    /**
     * Gets the maximum damage of a stack of this item.
     *
     * <p>Hook: called by vanilla's {@code ItemStack.getMaxDamage} (Silent Lib's {@code ItemStackMixin}).
     *
     * @param stack the stack
     * @return the max damage, 0 if the item cannot be damaged
     */
    default int getMaxDamage(ItemStack stack) {
        return stack.getOrDefault(DataComponents.MAX_DAMAGE, 0);
    }

    /**
     * Checks whether a stack of this item has any damage.
     *
     * @param stack the stack
     * @return true if damaged
     */
    default boolean isDamaged(ItemStack stack) {
        return stack.getDamageValue() > 0;
    }

    /**
     * Checks whether two damaged copies of this item may be combined into one in a crafting grid.
     * False for items made with {@link IItemPropertiesExtension#setNoCombineRepair()}.
     *
     * <p>Hook: called by vanilla's {@code RepairItemRecipe} (Silent Lib's {@code RepairItemRecipeMixin}). Silent
     * Lib's {@code ItemMixin} overrides this method to return the stored flag.
     *
     * @param stack the stack
     * @return true if combining is allowed
     */
    default boolean isCombineRepairable(ItemStack stack) {
        return true;
    }

    /**
     * Sets the damage of a stack of this item.
     *
     * <p>Hook: called by vanilla's {@code ItemStack.setDamageValue} (Silent Lib's {@code ItemStackMixin}).
     *
     * @param stack  the stack
     * @param damage the new damage
     */
    default void setDamage(ItemStack stack, int damage) {
        stack.set(DataComponents.DAMAGE, Mth.clamp(damage, 0, stack.getMaxDamage()));
    }

    /**
     * Called before a stack of this item takes damage. The return value is the damage actually
     * applied.
     *
     * <p>Hook: called by vanilla's {@code ItemStack.hurtAndBreak} (Silent Lib's {@code ItemStackMixin}).
     *
     * @param stack    the stack
     * @param amount   the damage about to be applied
     * @param entity   the entity using the item, or null
     * @param onBroken called if the item breaks
     * @param <T>      the entity type
     * @return the damage to apply
     */
    default <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        return amount;
    }

    /**
     * Checks whether the item in hand should play the re-equip animation when its stack changes.
     *
     * <p>Hook: called from {@code ItemInHandRenderer.tick} through Fabric API's
     * {@code FabricItem.allowComponentsUpdateAnimation}, which Silent Lib's {@code ItemMixin} forwards here. Fabric
     * only asks when the old and new stacks are the same item, and always passes {@code slotChanged = false}.
     *
     * @param oldStack     the previous stack
     * @param newStack     the new stack
     * @param slotChanged  true if the player selected a different slot
     * @return true to play the animation
     */
    default boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack != newStack;
    }

    /**
     * Checks whether this item can perform the given tool action.
     *
     * <p>Hook: Silent Lib mixins call it where NeoForge's patches do and vanilla checks for one exact item:
     * shears on mobs, leashes, equipment, beehives, pumpkins and tripwire, and fishing rods in
     * {@code FishingHook}. See {@code ShearableMobsMixin}, {@code EntityShearsMixin} and the other
     * mixins that use {@code ItemAbilityChecks}.
     *
     * <p>Port note: NeoForge overrides this in {@code AxeItem}, {@code HoeItem}, {@code ShovelItem},
     * {@code ShearsItem}, {@code FishingRodItem}, {@code TridentItem}, {@code BrushItem},
     * {@code FlintAndSteelItem}, {@code FireChargeItem} and {@code SpyglassItem}, each returning its
     * {@code ItemAbilities.DEFAULT_*_ACTIONS} set. Those overrides are reproduced here with {@code instanceof}
     * checks, so subclasses that call {@code super} (like Silent Gear's axe and shears) get the same answer.
     *
     * @param stack       the item
     * @param itemAbility the action
     * @return true if the item can do it
     */
    default boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
        Object item = this;
        if (item instanceof AxeItem) return ItemAbilities.DEFAULT_AXE_ACTIONS.contains(itemAbility);
        if (item instanceof HoeItem) return ItemAbilities.DEFAULT_HOE_ACTIONS.contains(itemAbility);
        if (item instanceof ShovelItem) return ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(itemAbility);
        if (item instanceof ShearsItem) return ItemAbilities.DEFAULT_SHEARS_ACTIONS.contains(itemAbility);
        if (item instanceof FishingRodItem) return ItemAbilities.DEFAULT_FISHING_ROD_ACTIONS.contains(itemAbility);
        if (item instanceof TridentItem) return ItemAbilities.DEFAULT_TRIDENT_ACTIONS.contains(itemAbility);
        if (item instanceof BrushItem) return ItemAbilities.DEFAULT_BRUSH_ACTIONS.contains(itemAbility);
        if (item instanceof FlintAndSteelItem) return ItemAbilities.DEFAULT_FLINT_ACTIONS.contains(itemAbility);
        if (item instanceof FireChargeItem) return ItemAbilities.DEFAULT_FIRECHARGE_ACTIONS.contains(itemAbility);
        if (item instanceof SpyglassItem) return ItemAbilities.DEFAULT_SPYGLASS_ACTIONS.contains(itemAbility);

        if (itemAbility == ItemAbilities.SWORD_SWEEP) {
            return stack.is(ItemTags.SWORDS);
        }
        return false;
    }

    /**
     * Gets what stays in the crafting grid after this item is used in a recipe.
     *
     * <p>Hook: Fabric API calls its stack-aware remainder from crafting, furnaces and brewing; Silent Lib's
     * {@code ItemMixin} forwards that to this method.
     *
     * @param instance the item used
     * @return the remaining item, or null for none
     */
    default @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        return self().getCraftingRemainder();
    }

    /**
     * Checks whether wearing this item keeps piglins neutral.
     *
     * <p>Hook: called by vanilla's {@code PiglinAi.isWearingSafeArmor} (Silent Lib's {@code PiglinAiMixin}).
     *
     * @param stack  the worn stack
     * @param wearer the wearer
     * @return true if piglins stay neutral
     */
    default boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        return stack.is(ItemTags.PIGLIN_SAFE_ARMOR);
    }

    /**
     * Checks whether wearing this item lets the wearer walk on powder snow.
     *
     * <p>Hook: called by vanilla's {@code PowderSnowBlock.canEntityWalkOnPowderSnow} (Silent Lib's
     * {@code PowderSnowBlockMixin}).
     *
     * @param stack  the worn stack
     * @param wearer the wearer
     * @return true if the wearer can walk on powder snow
     */
    default boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return stack.is(Items.LEATHER_BOOTS);
    }

    /**
     * Gets the equipment slot this item goes into, or null to let vanilla decide.
     *
     * <p>Hook: not wired. NeoForge reads it when equipping; in 26.1 the {@code EQUIPPABLE} component decides the
     * slot, and Silent Gear sets that component on its elytra and armor.
     *
     * @param stack the stack
     * @return the slot, or null
     */
    default @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return null;
    }

    /**
     * Gets attribute modifiers that apply on top of the stack's attribute modifier component.
     *
     * <p>Hook: not wired. Only Silent Gear's curio item overrides it, and it returns the default.
     *
     * @param stack the stack
     * @return the modifiers, empty by default
     */
    default ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return ItemAttributeModifiers.EMPTY;
    }
}
