package net.silentchaos512.lib.port.data;

import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagBuilder;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A parent tag lookup that treats tags outside the generating mod's namespace as existing.
 *
 * <p>Fabric port helper. Vanilla's {@link TagsProvider} refuses to write a tag that references another tag,
 * unless that tag is defined by the same provider or by its parent lookup. NeoForge also accepts tags that
 * exist in the loaded resources (vanilla's own tags, convention tags, other mods), so ported generators reference
 * tags like {@code #minecraft:mineable/shovel} freely. Fabric's answer is {@code forceAddTag} on each reference,
 * which would change a lot of ported code. Passing this lookup as the parent keeps the check for the mod's own
 * tags and accepts every other namespace. It only affects validation, not the generated files.
 */
public final class ExternalTags {
    private ExternalTags() {}

    /**
     * Creates the parent lookup.
     *
     * @param modId the namespace whose tags must be defined by the provider itself
     * @param <T>   the registry type
     * @return a completed future with the lookup
     */
    public static <T> CompletableFuture<TagsProvider.TagLookup<T>> outsideNamespace(String modId) {
        TagsProvider.TagLookup<T> lookup = key -> modId.equals(key.location().getNamespace())
                ? Optional.empty()
                : Optional.of(TagBuilder.create());
        return CompletableFuture.completedFuture(lookup);
    }
}
