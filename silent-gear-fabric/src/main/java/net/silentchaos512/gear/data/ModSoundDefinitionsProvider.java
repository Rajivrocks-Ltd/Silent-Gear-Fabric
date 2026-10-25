package net.silentchaos512.gear.data;

import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricSoundsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.setup.SgSounds;

import java.util.concurrent.CompletableFuture;

/**
 * Writes {@code assets/silentgear/sounds.json}.
 *
 * <p>Port note: upstream extends NeoForge's {@code SoundDefinitionsProvider} ({@code definition()},
 * {@code sound(...)}, {@code with(...)}). This uses Fabric's {@link FabricSoundsProvider} with the same sound
 * events, files and subtitles.
 */
public class ModSoundDefinitionsProvider extends FabricSoundsProvider {
    protected ModSoundDefinitionsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(HolderLookup.Provider registryLookup, SoundExporter exporter) {
        exporter.add(
                SgSounds.GEAR_DAMAGED.getId(),
                SoundTypeBuilder.of()
                        .subtitle("subtitles.item.silentgear.gear_damaged")
                        .sound(SoundTypeBuilder.RegistrationBuilder.ofFile(SilentGear.getId("kachink")))
                        .sound(SoundTypeBuilder.RegistrationBuilder.ofFile(SilentGear.getId("kachink_long")))
                        .sound(SoundTypeBuilder.RegistrationBuilder.ofFile(SilentGear.getId("snap")))
        );
        exporter.add(
                SgSounds.STONE_ANVIL_HAMMER.getId(),
                SoundTypeBuilder.of()
                        .subtitle("subtitles.block.silentgear.stone_anvil.hammer")
                        .sound(SoundTypeBuilder.RegistrationBuilder.ofFile(SilentGear.getId("hammer")))
        );
        exporter.add(
                SgSounds.STONE_ANVIL_KNIFE.getId(),
                SoundTypeBuilder.of()
                        .subtitle("subtitles.block.silentgear.stone_anvil.knife")
                        .sound(SoundTypeBuilder.RegistrationBuilder.ofFile(SilentGear.getId("knife_scrape")))
        );
    }

    @Override
    public String getName() {
        return "Silent Gear Sound Definitions";
    }
}
