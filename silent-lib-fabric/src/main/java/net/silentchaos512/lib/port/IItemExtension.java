package net.silentchaos512.lib.port;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
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
     * <p>Hook: not called by vanilla yet (milestone M8).
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
     * <p>Hook: not called by vanilla yet (milestone M8).
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
     * <p>Hook: not called by vanilla yet (milestone M7). The mixin overrides this method.
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
     * <p>Hook: not called by vanilla yet (milestone M8).
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
     * <p>Hook: not called by vanilla yet (milestone M8).
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
     * <p>Hook: not called by vanilla yet (milestone M9).
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
     * <p>Hook: called by Silent Gear's own code; vanilla equivalents are wired in milestone M8.
     *
     * @param stack       the item
     * @param itemAbility the action
     * @return true if the item can do it
     */
    default boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
        if (itemAbility == ItemAbilities.SWORD_SWEEP) {
            return stack.is(ItemTags.SWORDS);
        }
        return false;
    }

    /**
     * Gets what stays in the crafting grid after this item is used in a recipe.
     *
     * <p>Hook: not called by vanilla yet (milestone M7).
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
     * <p>Hook: not called by vanilla yet (milestone M8).
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
     * <p>Hook: not called by vanilla yet (milestone M8).
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
     * <p>Hook: not called by vanilla yet (milestone M8).
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
     * <p>Hook: not called by vanilla yet (milestone M8).
     *
     * @param stack the stack
     * @return the modifiers, empty by default
     */
    default ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return ItemAttributeModifiers.EMPTY;
    }
}
