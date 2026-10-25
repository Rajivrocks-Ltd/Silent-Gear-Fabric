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
 * the ported code (including the data generators) uses are listed; add more as needed. Ids were read from
 * NeoForge 26.1.2.112's compiled {@code Tags} class.
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

        /** {@code c:fence_gates/wooden}. */
        public static final TagKey<Block> FENCE_GATES_WOODEN = common("fence_gates/wooden");
        /** {@code c:netherracks}. */
        public static final TagKey<Block> NETHERRACKS = common("netherracks");
        /** {@code c:ores_in_ground/deepslate}. */
        public static final TagKey<Block> ORES_IN_GROUND_DEEPSLATE = common("ores_in_ground/deepslate");
        /** {@code c:ores_in_ground/netherrack}. */
        public static final TagKey<Block> ORES_IN_GROUND_NETHERRACK = common("ores_in_ground/netherrack");
        /** {@code c:ores_in_ground/stone}. */
        public static final TagKey<Block> ORES_IN_GROUND_STONE = common("ores_in_ground/stone");
        /** {@code c:storage_blocks}. */
        public static final TagKey<Block> STORAGE_BLOCKS = common("storage_blocks");

        private static TagKey<Block> common(String path) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", path));
        }
    }

    /** Item tags. */
    public static final class Items {
        private Items() {}

        /** {@code c:drinks/water}. */
        public static final TagKey<Item> DRINKS_WATER = common("drinks/water");

        /** {@code c:buckets/empty}. */
        public static final TagKey<Item> BUCKETS_EMPTY = common("buckets/empty");
        /** {@code c:cobblestones}. */
        public static final TagKey<Item> COBBLESTONES = common("cobblestones");
        /** {@code c:dusts}. */
        public static final TagKey<Item> DUSTS = common("dusts");
        /** {@code c:dusts/glowstone}. */
        public static final TagKey<Item> DUSTS_GLOWSTONE = common("dusts/glowstone");
        /** {@code c:dusts/redstone}. */
        public static final TagKey<Item> DUSTS_REDSTONE = common("dusts/redstone");
        /** {@code c:dyes}. */
        public static final TagKey<Item> DYES = common("dyes");
        /** {@code c:dyes/black}. */
        public static final TagKey<Item> DYES_BLACK = common("dyes/black");
        /** {@code c:dyes/blue}. */
        public static final TagKey<Item> DYES_BLUE = common("dyes/blue");
        /** {@code c:dyes/brown}. */
        public static final TagKey<Item> DYES_BROWN = common("dyes/brown");
        /** {@code c:dyes/cyan}. */
        public static final TagKey<Item> DYES_CYAN = common("dyes/cyan");
        /** {@code c:dyes/gray}. */
        public static final TagKey<Item> DYES_GRAY = common("dyes/gray");
        /** {@code c:dyes/green}. */
        public static final TagKey<Item> DYES_GREEN = common("dyes/green");
        /** {@code c:dyes/light_blue}. */
        public static final TagKey<Item> DYES_LIGHT_BLUE = common("dyes/light_blue");
        /** {@code c:dyes/light_gray}. */
        public static final TagKey<Item> DYES_LIGHT_GRAY = common("dyes/light_gray");
        /** {@code c:dyes/lime}. */
        public static final TagKey<Item> DYES_LIME = common("dyes/lime");
        /** {@code c:dyes/magenta}. */
        public static final TagKey<Item> DYES_MAGENTA = common("dyes/magenta");
        /** {@code c:dyes/orange}. */
        public static final TagKey<Item> DYES_ORANGE = common("dyes/orange");
        /** {@code c:dyes/pink}. */
        public static final TagKey<Item> DYES_PINK = common("dyes/pink");
        /** {@code c:dyes/purple}. */
        public static final TagKey<Item> DYES_PURPLE = common("dyes/purple");
        /** {@code c:dyes/red}. */
        public static final TagKey<Item> DYES_RED = common("dyes/red");
        /** {@code c:dyes/white}. */
        public static final TagKey<Item> DYES_WHITE = common("dyes/white");
        /** {@code c:dyes/yellow}. */
        public static final TagKey<Item> DYES_YELLOW = common("dyes/yellow");
        /** {@code c:ender_pearls}. */
        public static final TagKey<Item> ENDER_PEARLS = common("ender_pearls");
        /** {@code c:end_stones}. */
        public static final TagKey<Item> END_STONES = common("end_stones");
        /** {@code c:feathers}. */
        public static final TagKey<Item> FEATHERS = common("feathers");
        /** {@code c:fence_gates/wooden}. */
        public static final TagKey<Item> FENCE_GATES_WOODEN = common("fence_gates/wooden");
        /** {@code c:gems}. */
        public static final TagKey<Item> GEMS = common("gems");
        /** {@code c:gems/amethyst}. */
        public static final TagKey<Item> GEMS_AMETHYST = common("gems/amethyst");
        /** {@code c:gems/diamond}. */
        public static final TagKey<Item> GEMS_DIAMOND = common("gems/diamond");
        /** {@code c:gems/emerald}. */
        public static final TagKey<Item> GEMS_EMERALD = common("gems/emerald");
        /** {@code c:gems/lapis}. */
        public static final TagKey<Item> GEMS_LAPIS = common("gems/lapis");
        /** {@code c:gems/prismarine}. */
        public static final TagKey<Item> GEMS_PRISMARINE = common("gems/prismarine");
        /** {@code c:gems/quartz}. */
        public static final TagKey<Item> GEMS_QUARTZ = common("gems/quartz");
        /** {@code c:glass_blocks/colorless}. */
        public static final TagKey<Item> GLASS_BLOCKS_COLORLESS = common("glass_blocks/colorless");
        /** {@code c:ingots}. */
        public static final TagKey<Item> INGOTS = common("ingots");
        /** {@code c:ingots/copper}. */
        public static final TagKey<Item> INGOTS_COPPER = common("ingots/copper");
        /** {@code c:ingots/gold}. */
        public static final TagKey<Item> INGOTS_GOLD = common("ingots/gold");
        /** {@code c:ingots/iron}. */
        public static final TagKey<Item> INGOTS_IRON = common("ingots/iron");
        /** {@code c:ingots/netherite}. */
        public static final TagKey<Item> INGOTS_NETHERITE = common("ingots/netherite");
        /** {@code c:leathers}. */
        public static final TagKey<Item> LEATHERS = common("leathers");
        /** {@code c:tools/melee_weapon}. */
        public static final TagKey<Item> MELEE_WEAPON_TOOLS = common("tools/melee_weapon");
        /** {@code c:tools/mining_tool}. */
        public static final TagKey<Item> MINING_TOOL_TOOLS = common("tools/mining_tool");
        /** {@code c:netherracks}. */
        public static final TagKey<Item> NETHERRACKS = common("netherracks");
        /** {@code c:nuggets}. */
        public static final TagKey<Item> NUGGETS = common("nuggets");
        /** {@code c:nuggets/gold}. */
        public static final TagKey<Item> NUGGETS_GOLD = common("nuggets/gold");
        /** {@code c:obsidians}. */
        public static final TagKey<Item> OBSIDIANS = common("obsidians");
        /** {@code c:ores}. */
        public static final TagKey<Item> ORES = common("ores");
        /** {@code c:ores_in_ground/deepslate}. */
        public static final TagKey<Item> ORES_IN_GROUND_DEEPSLATE = common("ores_in_ground/deepslate");
        /** {@code c:ores_in_ground/netherrack}. */
        public static final TagKey<Item> ORES_IN_GROUND_NETHERRACK = common("ores_in_ground/netherrack");
        /** {@code c:ores_in_ground/stone}. */
        public static final TagKey<Item> ORES_IN_GROUND_STONE = common("ores_in_ground/stone");
        /** {@code c:tools/ranged_weapon}. */
        public static final TagKey<Item> RANGED_WEAPON_TOOLS = common("tools/ranged_weapon");
        /** {@code c:raw_materials}. */
        public static final TagKey<Item> RAW_MATERIALS = common("raw_materials");
        /** {@code c:rods}. */
        public static final TagKey<Item> RODS = common("rods");
        /** {@code c:rods/blaze}. */
        public static final TagKey<Item> RODS_BLAZE = common("rods/blaze");
        /** {@code c:rods/breeze}. */
        public static final TagKey<Item> RODS_BREEZE = common("rods/breeze");
        /** {@code c:rods/wooden}. */
        public static final TagKey<Item> RODS_WOODEN = common("rods/wooden");
        /** {@code c:sandstone/red_blocks}. */
        public static final TagKey<Item> SANDSTONE_RED_BLOCKS = common("sandstone/red_blocks");
        /** {@code c:sandstone/uncolored_blocks}. */
        public static final TagKey<Item> SANDSTONE_UNCOLORED_BLOCKS = common("sandstone/uncolored_blocks");
        /** {@code c:seeds}. */
        public static final TagKey<Item> SEEDS = common("seeds");
        /** {@code c:slime_balls}. */
        public static final TagKey<Item> SLIME_BALLS = common("slime_balls");
        /** {@code c:stones}. */
        public static final TagKey<Item> STONES = common("stones");
        /** {@code c:storage_blocks}. */
        public static final TagKey<Item> STORAGE_BLOCKS = common("storage_blocks");
        /** {@code c:storage_blocks/gold}. */
        public static final TagKey<Item> STORAGE_BLOCKS_GOLD = common("storage_blocks/gold");
        /** {@code c:storage_blocks/iron}. */
        public static final TagKey<Item> STORAGE_BLOCKS_IRON = common("storage_blocks/iron");
        /** {@code c:strings}. */
        public static final TagKey<Item> STRINGS = common("strings");
        /** {@code c:tools/bow}. */
        public static final TagKey<Item> TOOLS_BOW = common("tools/bow");
        /** {@code c:tools/crossbow}. */
        public static final TagKey<Item> TOOLS_CROSSBOW = common("tools/crossbow");
        /** {@code c:tools/shear}. */
        public static final TagKey<Item> TOOLS_SHEAR = common("tools/shear");
        /** {@code c:tools/shield}. */
        public static final TagKey<Item> TOOLS_SHIELD = common("tools/shield");

        private static TagKey<Item> common(String path) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
        }
    }
}
