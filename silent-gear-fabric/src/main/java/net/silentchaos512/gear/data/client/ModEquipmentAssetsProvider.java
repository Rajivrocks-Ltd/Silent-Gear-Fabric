package net.silentchaos512.gear.data.client;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.item.equipment.SgEquipmentAssets;

import java.util.function.BiConsumer;

/**
 * Equipment asset definitions (worn armor layers) for Silent Gear's armor materials.
 *
 * <p>Port note: upstream extends vanilla's {@code EquipmentAssetProvider}, which NeoForge patches so subclasses
 * add their own assets through {@code registerModels}. Vanilla's provider only writes its own hard-coded vanilla
 * assets, so this is a plain {@link DataProvider} with vanilla's {@code run} logic.
 */
public class ModEquipmentAssetsProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public ModEquipmentAssetsProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "equipment");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Map<ResourceKey<EquipmentAsset>, EquipmentClientInfo> equipmentAssets = new HashMap<>();
        registerModels((id, asset) -> {
            if (equipmentAssets.putIfAbsent(id, asset) != null) {
                throw new IllegalStateException("Tried to register equipment asset twice for id: " + id);
            }
        });
        return DataProvider.saveAll(cache, EquipmentClientInfo.CODEC, this.pathProvider::json, equipmentAssets);
    }

    protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
        output.accept(
                SgEquipmentAssets.CRIMSON_STEEL,
                EquipmentClientInfo.builder()
                        .addHumanoidLayers(SilentGear.getId("crimson_steel"), false)
                        .build()
        );
        output.accept(
                SgEquipmentAssets.GENERIC_FLAT,
                EquipmentClientInfo.builder()
                        .addHumanoidLayers(SilentGear.getId("generic_flat"), true)
                        .build()
        );
        output.accept(
                SgEquipmentAssets.GENERIC_NETHERITE,
                EquipmentClientInfo.builder()
                        .addHumanoidLayers(SilentGear.getId("generic_netherite"), true)
                        .build()
        );
        output.accept(
                SgEquipmentAssets.GENERIC_SHINY,
                EquipmentClientInfo.builder()
                        .addHumanoidLayers(SilentGear.getId("generic_shiny"), true)
                        .addHumanoidLayers(SilentGear.getId("generic_shiny_overlay"), false)
                        .build()
        );
        output.accept(
                SgEquipmentAssets.GENERIC_TURTLE,
                EquipmentClientInfo.builder()
                        .addMainHumanoidLayer(SilentGear.getId("generic_turtle"), true)
                        .addMainHumanoidLayer(SilentGear.getId("generic_turtle_overlay"), false)
                        .build()
        );
    }

    @Override
    public String getName() {
        return "Silent Gear - Equipment Asset Definitions";
    }
}
