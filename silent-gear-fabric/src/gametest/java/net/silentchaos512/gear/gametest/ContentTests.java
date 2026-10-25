package net.silentchaos512.gear.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.storage.loot.LootTable;
import net.silentchaos512.gear.setup.SgRegistries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * M11 content and data checks: names, models, textures, loot, ore generation, and loaded data counts.
 *
 * <p>Asset files are read from the Silent Gear mod's own resources (the gametest server does not load assets,
 * but the files are in the mod).
 */
public class ContentTests {
    private static final String MOD_ID = "silentgear";

    private static Optional<Path> modFile(String path) {
        return FabricLoader.getInstance().getModContainer(MOD_ID).flatMap(mod -> mod.findPath(path)).filter(Files::exists);
    }

    private static JsonObject readJson(Path path) {
        try {
            return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + path, e);
        }
    }

    private static Set<String> langKeys() {
        Path lang = modFile("assets/silentgear/lang/en_us.json").orElseThrow(() -> new IllegalStateException("en_us.json not found"));
        return readJson(lang).keySet();
    }

    private static List<Item> modItems() {
        return BuiltInRegistries.ITEM.stream().filter(i -> BuiltInRegistries.ITEM.getKey(i).getNamespace().equals(MOD_ID)).toList();
    }

    private static List<Block> modBlocks() {
        return BuiltInRegistries.BLOCK.stream().filter(b -> BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals(MOD_ID)).toList();
    }

    /**
     * Blocks without an English name in upstream too. The wall-mounted stone torch has no item, so its name only
     * shows in places like the F3 screen (see PORTING_NOTES, upstream issues).
     */
    private static final Set<String> KNOWN_UNNAMED_BLOCKS = Set.of("block.silentgear.wall_stone_torch");

    /**
     * Every Silent Gear item and block has an English name.
     *
     * @param helper the test helper
     */
    @GameTest
    public void everyItemAndBlockHasAName(GameTestHelper helper) {
        Set<String> keys = langKeys();
        List<String> missing = new ArrayList<>();
        for (Item item : modItems()) {
            // The shown name, not getDescriptionId: blueprints and parts build their names from other keys.
            collectMissingKeys(item.getName(new ItemStack(item)), keys, missing);
        }
        for (Block block : modBlocks()) {
            if (!keys.contains(block.getDescriptionId()) && !KNOWN_UNNAMED_BLOCKS.contains(block.getDescriptionId())) {
                missing.add(block.getDescriptionId());
            }
        }
        helper.assertTrue(missing.isEmpty(), missing.size() + " names missing from en_us.json: " + missing);
        helper.succeed();
    }

    private static void collectMissingKeys(Component component, Set<String> keys, List<String> missing) {
        if (component.getContents() instanceof TranslatableContents translatable) {
            if (!keys.contains(translatable.getKey()) && translatable.getFallback() == null) {
                missing.add(translatable.getKey());
            }
            for (Object arg : translatable.getArgs()) {
                if (arg instanceof Component argComponent) collectMissingKeys(argComponent, keys, missing);
            }
        }
        component.getSiblings().forEach(sibling -> collectMissingKeys(sibling, keys, missing));
    }

    /**
     * Every Silent Gear item has an item model definition, every model it points to exists, and every Silent Gear
     * texture those models name exists.
     *
     * @param helper the test helper
     */
    @GameTest
    public void everyItemHasAModelAndTextures(GameTestHelper helper) {
        List<String> problems = new ArrayList<>();
        Set<String> checkedModels = new HashSet<>();
        for (Item item : modItems()) {
            String path = BuiltInRegistries.ITEM.getKey(item).getPath();
            Optional<Path> definition = modFile("assets/silentgear/items/" + path + ".json");
            if (definition.isEmpty()) {
                problems.add("no items/" + path + ".json");
                continue;
            }
            List<String> models = new ArrayList<>();
            collectModels(readJson(definition.get()), models);
            for (String model : models) {
                Identifier id = Identifier.parse(model);
                if (!id.getNamespace().equals(MOD_ID) || !checkedModels.add(model)) continue;
                Optional<Path> modelFile = modFile("assets/silentgear/models/" + id.getPath() + ".json");
                if (modelFile.isEmpty()) {
                    problems.add(path + ": missing model " + model);
                    continue;
                }
                JsonObject json = readJson(modelFile.get());
                if (json.has("textures")) {
                    for (var entry : json.getAsJsonObject("textures").entrySet()) {
                        String texture = entry.getValue().getAsString();
                        if (texture.startsWith("#")) continue;
                        Identifier tex = Identifier.parse(texture);
                        if (tex.getNamespace().equals(MOD_ID) && modFile("assets/silentgear/textures/" + tex.getPath() + ".png").isEmpty()) {
                            problems.add(model + ": missing texture " + texture);
                        }
                    }
                }
            }
        }
        helper.assertTrue(problems.isEmpty(), problems.size() + " model problems: " + problems.subList(0, Math.min(20, problems.size())));
        helper.succeed();
    }

    private static void collectModels(JsonElement element, List<String> out) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("model") && obj.get("model").isJsonPrimitive()) {
                out.add(obj.get("model").getAsString());
            }
            obj.entrySet().forEach(e -> collectModels(e.getValue(), out));
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(e -> collectModels(e, out));
        }
    }

    /**
     * Blocks with no blockstate file and no drops in upstream too: the phantom light is invisible and drops nothing
     * (see PORTING_NOTES, upstream issues).
     */
    private static final Set<String> KNOWN_NO_STATE_OR_DROPS = Set.of("phantom_light");

    /**
     * Every Silent Gear block has a blockstate file and, unless it drops nothing on purpose, a loot table that
     * gives something when mined with a netherite pickaxe (or shears for leaves).
     *
     * @param helper the test helper
     */
    @GameTest
    public void everyBlockHasStateAndDrops(GameTestHelper helper) {
        TestPlayer player = TestPlayer.survival(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        var lootTables = helper.getLevel().getServer().reloadableRegistries();
        List<String> problems = new ArrayList<>();
        for (Block block : modBlocks()) {
            String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
            if (KNOWN_NO_STATE_OR_DROPS.contains(path)) continue;
            if (modFile("assets/silentgear/blockstates/" + path + ".json").isEmpty()) {
                problems.add(path + ": no blockstate");
            }
            Optional<ResourceKey<LootTable>> tableKey = block.getLootTable();
            if (tableKey.isEmpty()) continue; // drops nothing on purpose
            if (lootTables.getLootTable(tableKey.get()) == LootTable.EMPTY) {
                problems.add(path + ": loot table " + tableKey.get().identifier() + " missing");
                continue;
            }
            ItemStack tool = new ItemStack(path.contains("leaves") ? Items.SHEARS : Items.NETHERITE_PICKAXE);
            // Crops are checked fully grown; young crops drop nothing on purpose.
            BlockState state = block instanceof CropBlock crop ? crop.getStateForAge(crop.getMaxAge()) : block.defaultBlockState();
            List<ItemStack> drops = Block.getDrops(state, helper.getLevel(), pos, null, player, tool);
            if (drops.isEmpty()) {
                problems.add(path + ": dropped nothing");
            }
        }
        helper.assertTrue(problems.isEmpty(), problems.size() + " block problems: " + problems);
        helper.succeed();
    }

    /**
     * Silent Gear's ores and wild plants are added to overworld, nether and end biomes (Fabric biome modifications
     * in {@code SgBiomeModifiers}, upstream's NeoForge biome modifier files).
     *
     * @param helper the test helper
     */
    @GameTest
    public void oresAreAddedToBiomes(GameTestHelper helper) {
        assertFeature(helper, Biomes.PLAINS, "bort_ore");
        assertFeature(helper, Biomes.PLAINS, "wild_flax");
        assertFeature(helper, Biomes.PLAINS, "wild_fluffy");
        assertFeature(helper, Biomes.NETHER_WASTES, "crimson_iron_ore");
        assertFeature(helper, Biomes.END_HIGHLANDS, "azure_silver_ore");
        helper.succeed();
    }

    private static void assertFeature(GameTestHelper helper, ResourceKey<Biome> biomeKey, String feature) {
        Holder<Biome> biome = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biomeKey);
        var features = biome.value().getGenerationSettings().features();
        var step = features.get(GenerationStep.Decoration.UNDERGROUND_ORES.ordinal());
        ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(MOD_ID, feature));
        boolean found = step.stream().anyMatch(h -> h.is(key));
        helper.assertTrue(found, biomeKey.identifier() + " should have " + key.identifier() + " in underground ores");
    }

    /**
     * The same number of traits, materials and parts load as on NeoForge (reference log: 77, 131, 48).
     *
     * @param helper the test helper
     */
    @GameTest
    public void gearDataLoads(GameTestHelper helper) {
        helper.assertValueEqual(SgRegistries.TRAIT.stream().count(), 77L, "traits");
        helper.assertValueEqual(SgRegistries.MATERIAL.stream().count(), 131L, "materials");
        helper.assertValueEqual(SgRegistries.PART.stream().count(), 48L, "parts");
        helper.succeed();
    }
}
