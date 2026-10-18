package net.silentchaos512.lib.port;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Tag keys that NeoForge defines in its {@code Tags} class and Silent Gear uses.
 *
 * <p>Fabric port stand-in. Each key has the same id as in NeoForge, so data packs keep working.
 * Most ids are {@code c:} convention tags, which Fabric API also defines. Only the constants that
 * the ported code uses are listed; add more as needed.
 */
public final class Tags {
    private Tags() {}

    /** Block tags. */
    public static final class Blocks {
        private Blocks() {}

        /** {@code neoforge:needs_netherite_tool}. Kept under NeoForge's namespace, as upstream data uses it. */
        public static final TagKey<Block> NEEDS_NETHERITE_TOOL = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("neoforge", "needs_netherite_tool"));
        /** {@code c:ores}. */
        public static final TagKey<Block> ORES = common("ores");
        /** {@code c:ores/diamond}. */
        public static final TagKey<Block> ORES_DIAMOND = common("ores/diamond");

        private static TagKey<Block> common(String path) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", path));
        }
    }

    /** Item tags. */
    public static final class Items {
        private Items() {}

        /** {@code c:drinks/water}. */
        public static final TagKey<Item> DRINKS_WATER = common("drinks/water");

        private static TagKey<Item> common(String path) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
        }
    }
}
