package net.silentchaos512.lib.port.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

/**
 * Item tags provider with NeoForge's constructor, for data generators ported from NeoForge.
 *
 * <p>Fabric port stand-in for NeoForge's class of the same name, which is just vanilla's
 * {@link IntrinsicHolderTagsProvider} for items plus a mod id. Fabric's own
 * {@code FabricTagsProvider.ItemTagsProvider} does not extend that class and names its builder method
 * {@code valueLookupBuilder} instead of {@code tag}, so ported code would need many changes. Vanilla providers
 * run fine under Fabric's data generator. References to tags of other namespaces are accepted, as on NeoForge
 * (see {@link ExternalTags}).
 */
public abstract class ItemTagsProvider extends IntrinsicHolderTagsProvider<Item> {
    /** The mod id. NeoForge uses it to check existing files; here it is only kept for subclasses. */
    protected final String modId;

    /**
     * Creates the provider.
     *
     * @param output         the pack output
     * @param lookupProvider the registry lookup
     * @param modId          the mod id
     */
    public ItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, Registries.ITEM, lookupProvider, ExternalTags.outsideNamespace(modId), item -> item.builtInRegistryHolder().key());
        this.modId = modId;
    }
}
