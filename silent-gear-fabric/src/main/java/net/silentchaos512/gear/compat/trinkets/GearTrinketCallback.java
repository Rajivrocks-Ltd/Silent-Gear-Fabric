package net.silentchaos512.gear.compat.trinkets;

import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.silentchaos512.gear.util.GearHelper;
import net.silentchaos512.gear.util.TraitHelper;

import java.util.function.BiConsumer;

/**
 * How Silent Gear items behave in trinket slots. Fabric counterpart of upstream's
 * {@code CurioGearItemCapability.GearCurio}.
 *
 * <p>Fabric port only. Same behavior as upstream:
 * <ul>
 *     <li>Ticks the gear like an equipped item (server only), when {@code tickGear} is set.</li>
 *     <li>Attribute modifiers from traits. Each modifier id gets the slot as a suffix, as Trinkets does for
 *     its own modifiers, so two rings with the same trait do not replace each other. Curios does the same
 *     per slot.</li>
 *     <li>Kept on death, can be equipped by using it, and plays the gold armor equip sound.</li>
 * </ul>
 * Upstream returns 0 for the fortune and looting levels (not done yet upstream), and Trinkets has no such
 * hook, so they are left out. The Brilliant trait's piglin check is in
 * {@link TrinketsCompat#makesPiglinsNeutral}.
 */
public class GearTrinketCallback implements TrinketCallback {
    private final boolean tickGear;

    /**
     * @param tickGear whether to tick the gear while it is in a trinket slot (false for the elytra, like
     *                 upstream)
     */
    public GearTrinketCallback(boolean tickGear) {
        this.tickGear = tickGear;
    }

    @Override
    public void tick(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        if (this.tickGear && entity.level() instanceof ServerLevel serverLevel) {
            GearHelper.inventoryTick(stack, serverLevel, entity, true);
        }
    }

    @Override
    public void forEachTrinketModifier(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity, Identifier slotIdentifier, BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        String suffix = "/" + slot.getAsIdentifierPath();
        TraitHelper.addAttributeModifiersFromTraits(stack, (attribute, modifier) ->
                consumer.accept(attribute, new AttributeModifier(modifier.id().withSuffix(suffix), modifier.amount(), modifier.operation())));
    }

    @Override
    public TrinketDropRule getDropRule(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        return TrinketDropRule.KEEP;
    }

    @Override
    public boolean canEquipFromUse(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public Holder<SoundEvent> getEquipSound(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        return SoundEvents.ARMOR_EQUIP_GOLD;
    }
}
