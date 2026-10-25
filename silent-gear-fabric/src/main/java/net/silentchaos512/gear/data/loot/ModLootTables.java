package net.silentchaos512.gear.data.loot;

import com.google.common.collect.ImmutableList;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import java.util.concurrent.CompletableFuture;

import java.util.Collections;
import java.util.List;

/**
 * Silent Gear's loot tables: block drops, chest injections, mob drops and gifts.
 *
 * <p>Port note: NeoForge patches {@link LootTableProvider} so subclasses can override {@code getTables()} and
 * {@code validate(...)}. Upstream passes vanilla's table list to the constructor, returns its own from
 * {@code getTables()}, and turns validation off. Vanilla takes the list in its constructor, so Silent Gear's list
 * is passed there. Vanilla's validation is left on.
 */
public class ModLootTables extends LootTableProvider {
    public ModLootTables(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Collections.emptySet(), getTables(), lookupProvider);
    }

    public static List<SubProviderEntry> getTables() {
        return ImmutableList.of(
                new SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK),
                new SubProviderEntry(ModChestLootTables::new, LootContextParamSets.CHEST),
                new SubProviderEntry(ModEntityLootTables::new, LootContextParamSets.ENTITY),
                new SubProviderEntry(ModGiftLootTables::new, LootContextParamSets.GIFT)
        );
    }
}
