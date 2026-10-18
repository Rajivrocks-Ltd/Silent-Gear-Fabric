package net.silentchaos512.gear.setup;

import net.fabricmc.fabric.api.registry.CompostableRegistry;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.fabricmc.fabric.api.registry.VillagerInteractionRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.silentchaos512.gear.SilentGear;

/**
 * Furnace fuel values and composting chances.
 *
 * <p>Fabric port only. Upstream generates these as NeoForge data maps,
 * {@code data/neoforge/data_maps/item/furnace_fuels.json} and {@code compostables.json}.
 * Fabric reads neither, so the same values are registered here with Fabric's content registries.
 * Items are looked up by id so the values can be compared with the upstream files one to one.
 */
public final class SgDataMaps {
    private SgDataMaps() {}

    /**
     * Registers fuels and compostables. Call once during common init, after items are registered.
     */
    public static void register() {
        // furnace_fuels.json
        FuelValueEvents.BUILD.register((builder, context) -> {
            builder.add(item("netherwood_charcoal"), 2400);
            builder.add(item("netherwood_charcoal_block"), 24000);
            builder.add(item("netherwood_stick"), 150);
        });

        // compostables.json (every entry has "can_villager_compost": true)
        compostable("flax_fiber", 0.5f);
        compostable("flax_seeds", 0.3f);
        compostable("fluffy_puff", 0.5f);
        compostable("fluffy_seeds", 0.3f);
        compostable("golden_nether_banana", 1.0f);
        compostable("nether_banana", 0.85f);
        compostable("netherwood_sapling", 0.3f);
    }

    private static void compostable(String name, float chance) {
        Item item = item(name);
        CompostableRegistry.INSTANCE.add(item, chance);
        VillagerInteractionRegistries.registerCompostable(item);
    }

    private static Item item(String name) {
        return BuiltInRegistries.ITEM.getValue(SilentGear.getId(name));
    }
}
