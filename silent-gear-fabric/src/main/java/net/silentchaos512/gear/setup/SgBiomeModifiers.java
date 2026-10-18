package net.silentchaos512.gear.setup;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.silentchaos512.gear.SilentGear;

/**
 * Adds Silent Gear's ores and wild plants to world generation.
 *
 * <p>Fabric port only. Upstream does this with three NeoForge biome modifier files in
 * {@code data/silentgear/neoforge/biome_modifier} ({@code overworld_ores}, {@code nether_ores},
 * {@code end_ores}), all of type {@code neoforge:add_features}. Fabric has no data-driven
 * equivalent, so the same biome tags, features and generation step are applied here in code.
 * The placed features themselves are still the JSON files in {@code data/silentgear/worldgen}.
 */
public final class SgBiomeModifiers {
    private SgBiomeModifiers() {}

    /**
     * Registers the biome modifications. Call once during common init.
     */
    public static void register() {
        // overworld_ores.json
        addUndergroundOres(BiomeTags.IS_OVERWORLD, "bort_ore", "wild_flax", "wild_fluffy");
        // nether_ores.json
        addUndergroundOres(BiomeTags.IS_NETHER, "crimson_iron_ore");
        // end_ores.json
        addUndergroundOres(BiomeTags.IS_END, "azure_silver_ore");
    }

    private static void addUndergroundOres(net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> biomes, String... features) {
        for (String feature : features) {
            BiomeModifications.addFeature(
                    BiomeSelectors.tag(biomes),
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    ResourceKey.create(Registries.PLACED_FEATURE, SilentGear.getId(feature))
            );
        }
    }
}
