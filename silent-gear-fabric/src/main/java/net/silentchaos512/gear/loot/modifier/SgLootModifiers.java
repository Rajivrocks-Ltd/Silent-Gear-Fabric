package net.silentchaos512.gear.loot.modifier;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.loot.condition.HasTraitCondition;
import net.silentchaos512.gear.setup.SgLoot;
import net.silentchaos512.gear.util.Const;
import net.silentchaos512.lib.port.loot.GlobalLootModifiers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Silent Gear's global loot modifiers, set up the Fabric way.
 *
 * <p>Fabric port only. Upstream defines these as NeoForge global loot modifier JSON files in
 * {@code data/silentgear/loot_modifiers} (left out of the port in M3). Each one is reproduced here with the same
 * conditions:
 * <ul>
 *     <li>{@code add_mob_drops/*} ({@code neoforge:add_table}): when one of the listed entity loot tables rolls,
 *     also roll a Silent Gear drop table. Done with {@code LootTableEvents.MODIFY}, by adding a pool that
 *     references the drop table, which rolls it once with the same loot context.</li>
 *     <li>{@code bonus_drops_trait} (no conditions) and {@code magmatic_smelting} (tool has the Magmatic trait):
 *     change the drops after any table rolls. Registered with Silent Lib's {@code GlobalLootModifiers}, which
 *     applies them at the same point as NeoForge. Fabric's {@code LootTableEvents.MODIFY_DROPS} would also run
 *     them for inner tables (like the mob drop tables above), giving bonus drops a second roll.</li>
 * </ul>
 * NeoForge orders modifiers by priority only, and all of these have the default priority. Here the mob drop
 * items are part of the table result before bonus drops and magmatic smelting run.
 */
public final class SgLootModifiers {
    private static final BonusDropsTraitLootModifier BONUS_DROPS_TRAIT = new BonusDropsTraitLootModifier();
    private static final MagmaticTraitLootModifier MAGMATIC_SMELTING = new MagmaticTraitLootModifier();
    /** {@code magmatic_smelting.json}: {@code "condition": "silentgear:has_trait", "trait": "silentgear:magmatic", "level": {}}. */
    private static final LootItemCondition HAS_MAGMATIC = HasTraitCondition.builder(Const.Traits.MAGMATIC).build();

    /** Entity loot table id to extra Silent Gear drop table, from {@code add_mob_drops/*.json}. */
    private static final Map<Identifier, ResourceKey<LootTable>> MOB_DROPS = new HashMap<>();

    static {
        // fine_silk_high.json
        addMobDrops(SgLoot.Tables.DROPS_FINE_SILK_HIGH, "cave_spider");
        // fine_silk_low.json
        addMobDrops(SgLoot.Tables.DROPS_FINE_SILK_LOW, "spider");
        // leather_scraps_high.json
        addMobDrops(SgLoot.Tables.DROPS_LEATHER_SCRAPS_HIGH, "zombie_villager");
        // leather_scraps_low.json
        addMobDrops(SgLoot.Tables.DROPS_LEATHER_SCRAPS_LOW, "husk", "zombie");
        // sinew.json
        addMobDrops(SgLoot.Tables.DROPS_SINEW, "cow", "pig");
        for (String color : List.of("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
                "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black")) {
            addMobDrops(SgLoot.Tables.DROPS_SINEW, "sheep/" + color);
        }
    }

    private SgLootModifiers() {}

    private static void addMobDrops(ResourceKey<LootTable> dropTable, String... entityTablePaths) {
        for (String path : entityTablePaths) {
            MOB_DROPS.put(Identifier.withDefaultNamespace("entities/" + path), dropTable);
        }
    }

    /**
     * Registers the mob drop table listener and the two loot modifiers. Called once from common init.
     */
    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            ResourceKey<LootTable> dropTable = MOB_DROPS.get(key.identifier());
            if (dropTable != null) {
                tableBuilder.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(dropTable)));
            }
        });

        GlobalLootModifiers.register(SilentGear.getId("bonus_drops_trait"), BONUS_DROPS_TRAIT::doApply);
        GlobalLootModifiers.register(SilentGear.getId("magmatic_smelting"), (loot, context) ->
                HAS_MAGMATIC.test(context) ? MAGMATIC_SMELTING.doApply(loot, context) : loot);
    }
}
