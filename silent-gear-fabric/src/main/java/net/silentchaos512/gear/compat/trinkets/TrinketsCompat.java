package net.silentchaos512.gear.compat.trinkets;

import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.silentchaos512.gear.api.item.GearItem;
import net.silentchaos512.gear.gear.trait.Trait;
import net.silentchaos512.gear.setup.GearItemSets;
import net.silentchaos512.gear.util.Const;
import net.silentchaos512.gear.api.util.DataResource;
import net.silentchaos512.gear.util.TraitHelper;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Trinkets support, replacing upstream's {@code compat.curios.CuriosCompat}.
 *
 * <p>Fabric port only. Curios does not exist on Fabric; Trinkets Updated fills the same role. Only call this
 * class after checking that Trinkets is loaded ({@code Const.TRINKETS}), otherwise its classes are missing.
 * The check must not be in this class: loading this class needs the Trinkets classes.
 * <ul>
 *     <li>Slots: the ring, bracelet, necklace and elytra go in the Trinkets slots {@code hand/ring},
 *     {@code hand/bracelet} (added by Silent Gear), {@code chest/necklace} and {@code chest/back}. The slot
 *     assignments are data files in {@code data/trinkets}, like upstream's {@code data/silentgear/curios}.</li>
 *     <li>Item behavior: {@link GearTrinketCallback}, the counterpart of upstream's {@code GearCurio}.</li>
 * </ul>
 */
public final class TrinketsCompat {
    private TrinketsCompat() {}

    /**
     * Registers the trinket callbacks for Silent Gear's ring, bracelet, necklace and elytra. Call once during
     * mod init, after the items are registered, and only when Trinkets is loaded. Replaces upstream's
     * {@code CurioGearItemCapability.registerCapabilities}.
     */
    public static void init() {
        GearTrinketCallback accessory = new GearTrinketCallback(true);
        TrinketCallback.setCallback(GearItemSets.BRACELET.gearItem(), accessory);
        TrinketCallback.setCallback(GearItemSets.RING.gearItem(), accessory);
        TrinketCallback.setCallback(GearItemSets.NECKLACE.gearItem(), accessory);
        // Upstream's elytra curio only damages the elytra while gliding. Trinkets already lets glider items in
        // trinket slots work and damages them while gliding, so the elytra gets no tick of its own here.
        TrinketCallback.setCallback(GearItemSets.ELYTRA.gearItem(), new GearTrinketCallback(false));
    }

    /**
     * Gets the highest level of a trait on any gear item in the entity's trinket slots.
     *
     * @param entity the wearer
     * @param trait  the trait
     * @return the highest level, or 0
     */
    public static int getHighestTraitLevel(LivingEntity entity, DataResource<Trait> trait) {
        int max = 0;
        for (ItemStack stack : getEquippedTrinkets(entity)) {
            max = Math.max(max, TraitHelper.getTraitLevel(stack, trait));
        }
        return max;
    }

    /**
     * Gets the gear items in the entity's trinket slots. Cosmetic slots are left out, as Curios does.
     *
     * @param entity the wearer
     * @return the equipped gear items
     */
    public static Collection<ItemStack> getEquippedTrinkets(LivingEntity entity) {
        Collection<ItemStack> ret = new ArrayList<>();
        TrinketsApi.getAttachment(entity).forEach((slot, stack) -> {
            if (!slot.cosmetic() && stack.getItem() instanceof GearItem) {
                ret.add(stack);
            }
        });
        return ret;
    }

    /**
     * Checks whether the entity wears a gear trinket with the Brilliant trait. Upstream's {@code GearCurio}
     * answers Curios' piglin check the same way; Trinkets itself only checks the {@code piglin_safe_armor} tag.
     *
     * @param entity the wearer
     * @return true if piglins should be neutral
     */
    public static boolean makesPiglinsNeutral(LivingEntity entity) {
        for (ItemStack stack : getEquippedTrinkets(entity)) {
            if (TraitHelper.hasTrait(stack, Const.Traits.BRILLIANT)) {
                return true;
            }
        }
        return false;
    }
}
