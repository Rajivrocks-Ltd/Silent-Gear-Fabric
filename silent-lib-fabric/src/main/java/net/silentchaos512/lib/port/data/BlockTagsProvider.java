package net.silentchaos512.lib.port.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

/**
 * Block tags provider with NeoForge's constructor, for data generators ported from NeoForge.
 *
 * <p>Fabric port stand-in for NeoForge's class of the same name, which is just vanilla's
 * {@link IntrinsicHolderTagsProvider} for blocks plus a mod id. See {@link ItemTagsProvider} for why Fabric's
 * own provider is not used, and {@link ExternalTags} for tag references.
 */
public abstract class BlockTagsProvider extends IntrinsicHolderTagsProvider<Block> {
    /** The mod id. NeoForge uses it to check existing files; here it is only kept for subclasses. */
    protected final String modId;

    /**
     * Creates the provider.
     *
     * @param output         the pack output
     * @param lookupProvider the registry lookup
     * @param modId          the mod id
     */
    public BlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, Registries.BLOCK, lookupProvider, ExternalTags.outsideNamespace(modId), block -> block.builtInRegistryHolder().key());
        this.modId = modId;
    }
}
