package net.silentchaos512.gear.api.data.part;

import com.google.common.collect.Sets;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class PartsProviderBase implements DataProvider {
    /**
     * The pack to write into.
     *
     * <p>Port note: upstream keeps NeoForge's {@code DataGenerator} and reads its pack output; Fabric's data
     * generator gives each provider its pack output directly.
     */
    protected final PackOutput output;
    protected final String modId;

    public PartsProviderBase(PackOutput output, String modId) {
        this.output = output;
        this.modId = modId;
    }

    public @NotNull String getName() {
        return "Silent Gear Parts: " + modId;
    }

    protected abstract Collection<PartBuilder> getParts();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path outputFolder = this.output.getOutputFolder();
        Set<Identifier> set = Sets.newHashSet();
        List<CompletableFuture<?>> list = new ArrayList<>();

        this.getParts().forEach(builder -> {
            if (!set.add(builder.getId())) {
                throw new IllegalStateException("Duplicate part: " + builder.getId());
            }
            Path path = outputFolder.resolve(String.format("data/%s/silentgear_parts/%s.json", builder.getId().getNamespace(), builder.getId().getPath()));
            list.add(DataProvider.saveStable(cache, builder.serialize(), path));
        });

        return CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
    }
}
