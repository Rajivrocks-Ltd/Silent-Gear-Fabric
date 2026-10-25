package net.silentchaos512.gear.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import net.minecraft.data.CachedOutput;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.data.client.ModEquipmentAssetsProvider;
import net.silentchaos512.gear.data.client.ModModelProvider;
import net.silentchaos512.gear.data.loot.ModLootTables;
import net.silentchaos512.gear.data.recipes.ModRecipesProvider;
import net.silentchaos512.gear.data.tags.ModBlockTagsProvider;
import net.silentchaos512.gear.data.tags.ModDamageTypeTagsProvider;
import net.silentchaos512.gear.data.tags.ModEntityTypeTagsProvider;
import net.silentchaos512.gear.data.tags.ModItemTagsProvider;
import net.silentchaos512.gear.data.trait.TraitsProvider;
import net.silentchaos512.lib.data.recipe.LibRecipeProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Silent Gear's data generators.
 *
 * <p>Port note: upstream listens to NeoForge's {@code GatherDataEvent.Client}. On Fabric this is the
 * {@code fabric-datagen} entrypoint, run by {@code .\gradlew.bat :silent-gear-fabric:runDatagen}. The providers
 * are the same, except two NeoForge-only ones that are not ported: {@code ModDataMapProvider} (furnace fuels and
 * compostables, done in code on Fabric) and {@code ModLootModifierProvider} (global loot modifiers, done in code by
 * {@code SgLootModifiers}).
 */
public final class DataGenerators implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        // Tags
        ModBlockTagsProvider blocks = pack.addProvider(ModBlockTagsProvider::new);
        pack.addProvider((FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) -> new ModItemTagsProvider(output, lookupProvider, blocks));
        pack.addProvider(ModDamageTypeTagsProvider::new);
        pack.addProvider(ModEntityTypeTagsProvider::new);

        // Gear data
        pack.addProvider((FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) -> new TraitsProvider(lookupProvider, output));
        pack.addProvider((FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) -> new MaterialsProvider(lookupProvider, output));
        pack.addProvider((FabricPackOutput output) -> new PartsProvider(output));

        // Others
        pack.addProvider(ModLootTables::new);
        pack.addProvider((FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) -> LibRecipeProvider.createRunner(output, lookupProvider, "Silent Gear Recipes", ModRecipesProvider::new));
        pack.addProvider(ModAdvancementProvider::new);
//        ModWorldGen.init(generator, existingFileHelper); //FIXME

        // Client
        pack.addProvider((FabricPackOutput output) -> new ModModelProvider(output));
        pack.addProvider(ModSoundDefinitionsProvider::new);
        pack.addProvider((FabricPackOutput output) -> new ModEquipmentAssetsProvider(output));
    }

    public static CompletableFuture<?> saveStable(CachedOutput p_253653_, JsonElement p_254542_, Path p_254467_) {
        // Slightly modified version of DataProvider.saveStable. Only difference is that this one does not sort keys!
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                HashingOutputStream hashingoutputstream = new HashingOutputStream(Hashing.sha1(), bytearrayoutputstream);

                try (JsonWriter jsonwriter = new JsonWriter(new OutputStreamWriter(hashingoutputstream, StandardCharsets.UTF_8))) {
                    jsonwriter.setSerializeNulls(false);
                    jsonwriter.setIndent("  ");
                    GsonHelper.writeValue(jsonwriter, p_254542_, null);
                }

                p_253653_.writeIfNeeded(p_254467_, bytearrayoutputstream.toByteArray(), hashingoutputstream.hash());
            } catch (IOException ioexception) {
                SilentGear.LOGGER.error("Failed to save file to {}", p_254467_, ioexception);
            }

        }, Util.backgroundExecutor());
    }
}
