package net.silentchaos512.gear.setup;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.silentchaos512.lib.registry.DeferredHolder;
import net.silentchaos512.lib.registry.DeferredRegister;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.loot.condition.HasTraitCondition;
import net.silentchaos512.gear.loot.function.SetPartsFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Loot conditions, functions and loot table injection.
 *
 * <p>Port notes:
 * <ul>
 *     <li>Upstream also registers two NeoForge global loot modifiers ({@code bonus_drops_trait},
 *     {@code magmatic_smelting}). NeoForge has a registry for them; Fabric does not. They become
 *     {@code LootTableEvents.MODIFY_DROPS} listeners in milestone M8. Until then their classes are left
 *     out of the build.</li>
 *     <li>Chest loot injection used NeoForge's {@code LootTableLoadEvent}; it now uses Fabric's
 *     {@code LootTableEvents.MODIFY}, registered by {@link #register()}.</li>
 * </ul>
 */
public final class SgLoot {
    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, SilentGear.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, SilentGear.MOD_ID);

    // Conditions
    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<HasTraitCondition>> HAS_TRAIT =
            LOOT_CONDITIONS.register("has_trait", () -> HasTraitCondition.CODEC);

    // Functions
    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<SetPartsFunction>> SET_PARTS =
            LOOT_FUNCTIONS.register("set_parts", () -> SetPartsFunction.CODEC);

    private SgLoot() {
    }

    /**
     * Registers the loot table listeners. Called once from common init.
     */
    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) ->
                Injector.onLootTableLoad(key.identifier(), tableBuilder));
    }

    public static final class Tables {
        public static final ResourceKey<LootTable> DROPS_LEATHER_SCRAPS_LOW = table("drops/leather_scraps_low");
        public static final ResourceKey<LootTable> DROPS_LEATHER_SCRAPS_HIGH = table("drops/leather_scraps_high");
        public static final ResourceKey<LootTable> DROPS_FINE_SILK_LOW = table("drops/fine_silk_low");
        public static final ResourceKey<LootTable> DROPS_FINE_SILK_HIGH = table("drops/fine_silk_high");
        public static final ResourceKey<LootTable> DROPS_SINEW = table("drops/sinew");

        private static ResourceKey<LootTable> table(String path) {
            return ResourceKey.create(Registries.LOOT_TABLE, SilentGear.getId(path));
        }
    }

    public static final class Injector {
        public static final class Tables {
            private static final Map<Identifier, ResourceKey<LootTable>> MAP = new HashMap<>();

            // Chests
            public static final Identifier NETHER_BRIDGE = inject(BuiltInLootTables.NETHER_BRIDGE);
            public static final Identifier BASTION_TREASURE = inject(BuiltInLootTables.BASTION_TREASURE);
            public static final Identifier BASTION_OTHER = inject(BuiltInLootTables.BASTION_OTHER);
            public static final Identifier BASTION_BRIDGE = inject(BuiltInLootTables.BASTION_BRIDGE);
            public static final Identifier RUINED_PORTAL = inject(BuiltInLootTables.RUINED_PORTAL);

            public static Optional<ResourceKey<LootTable>> get(Identifier lootTable) {
                return Optional.ofNullable(MAP.get(lootTable));
            }

            private static Identifier inject(ResourceKey<LootTable> lootTable) {
                var originalId = lootTable.identifier();
                var newId = SilentGear.getId("inject/" + originalId.getNamespace() + "/" + originalId.getPath());
                MAP.put(originalId, ResourceKey.create(Registries.LOOT_TABLE, newId));
                return newId;
            }
        }

        private Injector() {}

        /**
         * Adds a pool that rolls Silent Gear's inject table to a vanilla chest table.
         *
         * <p>Port note: upstream takes NeoForge's {@code LootTableLoadEvent} and names the pool
         * {@code silentgear_injected}. Fabric hands over the table builder instead, and vanilla pools
         * have no names.
         *
         * @param name         the id of the table being loaded
         * @param tableBuilder the table builder
         */
        public static void onLootTableLoad(Identifier name, LootTable.Builder tableBuilder) {
            Tables.get(name).ifPresent(injectorName -> {
                SilentGear.LOGGER.info("Injecting loot table '{}' into '{}'", injectorName, name);
                tableBuilder.withPool(
                        LootPool.lootPool()
                                .add(NestedLootTable.lootTableReference(injectorName))
                );
            });
        }
    }
}
