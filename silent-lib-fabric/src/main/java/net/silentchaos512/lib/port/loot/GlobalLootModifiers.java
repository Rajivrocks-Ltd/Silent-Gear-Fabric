package net.silentchaos512.lib.port.loot;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.silentchaos512.lib.SilentLib;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the registered {@link IGlobalLootModifier}s and applies them, like NeoForge's
 * {@code LootModifierManager} and {@code CommonHooks.modifyLoot}.
 *
 * <p>Fabric port stand-in. {@code LootTableMixin} calls {@link #modifyLoot} once for each loot table that
 * is rolled on its own (block, entity, chest and so on). Tables rolled from inside another table are not
 * modified, as in NeoForge. Fabric's {@code LootTableEvents.MODIFY_DROPS} is not used because it also fires
 * for those inner tables, which would apply the modifiers twice.
 *
 * <p>Modifiers run in the order they are registered.
 */
public final class GlobalLootModifiers {
    private static final Map<Identifier, IGlobalLootModifier> MODIFIERS = new LinkedHashMap<>();

    private GlobalLootModifiers() {}

    /**
     * Registers a loot modifier. Call during mod init.
     *
     * @param id       the modifier's id, used in error messages
     * @param modifier the modifier
     * @throws IllegalArgumentException if the id is already registered
     */
    public static synchronized void register(Identifier id, IGlobalLootModifier modifier) {
        if (MODIFIERS.putIfAbsent(id, modifier) != null) {
            throw new IllegalArgumentException("Duplicate loot modifier " + id);
        }
    }

    /**
     * Applies all registered modifiers to a table's items. An exception in one modifier is logged and
     * that modifier is skipped, as NeoForge does.
     *
     * @param generatedLoot the items the table produced
     * @param context       the loot context the table was rolled with
     * @return the modified list of items
     */
    public static ObjectArrayList<ItemStack> modifyLoot(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        for (Map.Entry<Identifier, IGlobalLootModifier> entry : MODIFIERS.entrySet()) {
            try {
                generatedLoot = entry.getValue().apply(generatedLoot, context);
            } catch (Exception e) {
                SilentLib.LOGGER.error("Error applying global loot modifier {}", entry.getKey(), e);
            }
        }
        return generatedLoot;
    }
}
