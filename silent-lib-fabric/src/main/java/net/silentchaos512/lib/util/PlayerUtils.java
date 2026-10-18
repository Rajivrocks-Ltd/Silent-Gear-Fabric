package net.silentchaos512.lib.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silentchaos512.lib.SilentLib;

public final class PlayerUtils {
    private PlayerUtils() {}

    /**
     * Gives the player an item. If it can't be given directly, it spawns an EntityItem. Spawns 1
     * block above player's feet.
     *
     * @param player The player
     * @param stack  The item
     */
    public static void giveItem(Player player, ItemStack stack) {
        ItemStack copy = stack.copy();
        if (!player.getInventory().add(copy)) {
            ItemEntity entityItem = new ItemEntity(player.level(), player.getX(), player.getY(0.5), player.getZ(), copy);
            entityItem.setNoPickUpDelay();
            entityItem.setThrower(player);
            player.level().addFreshEntity(entityItem);
        }
    }

    /**
     * Gets a tag compound from the player's persisted data NBT compound, or creates it if it does
     * not exist. This can be used to save additional data to a player.
     *
     * <p>Port note: upstream reads the {@code PlayerPersisted} compound inside NeoForge's
     * {@code getPersistentData()}. On Fabric that compound is the
     * {@link SilentLib#PERSISTED_PLAYER_DATA} data attachment, which is saved with the player and
     * copied on death in the same way.
     *
     * @param player         The player
     * @param subCompoundKey The key for the tag compound (ideally should contain mod ID)
     * @return The tag compound, creating it if it does not exist.
     */
    public static CompoundTag getPersistedDataSubcompound(Player player, String subCompoundKey) {
        CompoundTag persistedData = player.getAttachedOrCreate(SilentLib.PERSISTED_PLAYER_DATA);
        if (!persistedData.contains(subCompoundKey)) {
            persistedData.put(subCompoundKey, new CompoundTag());
        }

        return persistedData.getCompound(subCompoundKey).orElseThrow();
    }
}
