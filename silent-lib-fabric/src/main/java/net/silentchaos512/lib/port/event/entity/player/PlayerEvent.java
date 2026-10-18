package net.silentchaos512.lib.port.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.silentchaos512.lib.port.event.entity.living.LivingEvent;

import java.util.Optional;

/**
 * Base class for events about a player, plus the player events the Silent mods use.
 */
public abstract class PlayerEvent extends LivingEvent {
    private final Player player;

    protected PlayerEvent(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public Player getEntity() {
        return player;
    }

    /**
     * Fired when the game computes how fast a player breaks a block. Listeners can change the
     * speed. Canceling it means the player cannot break the block.
     *
     * <p>Fabric: not fired yet. Needs a mixin in {@code Player.getDestroySpeed} (milestone M8).
     */
    public static class BreakSpeed extends PlayerEvent {
        private final BlockState state;
        private final float originalSpeed;
        private final Optional<BlockPos> pos;
        private float newSpeed;

        public BreakSpeed(Player player, BlockState state, float originalSpeed, Optional<BlockPos> pos) {
            super(player);
            this.state = state;
            this.originalSpeed = originalSpeed;
            this.newSpeed = originalSpeed;
            this.pos = pos;
        }

        public BlockState getState() {
            return state;
        }

        public float getOriginalSpeed() {
            return originalSpeed;
        }

        public float getNewSpeed() {
            return newSpeed;
        }

        public void setNewSpeed(float newSpeed) {
            this.newSpeed = newSpeed;
        }

        public Optional<BlockPos> getPosition() {
            return pos;
        }
    }

    /**
     * Fired when a player takes a crafted item out of a result slot.
     *
     * <p>Fabric: not fired yet. Needs a mixin in {@code ResultSlot.checkTakeAchievements}
     * (milestone M7).
     */
    public static class ItemCraftedEvent extends PlayerEvent {
        private final ItemStack crafting;
        private final Container inventory;

        public ItemCraftedEvent(Player player, ItemStack crafting, Container inventory) {
            super(player);
            this.crafting = crafting;
            this.inventory = inventory;
        }

        public ItemStack getCrafting() {
            return crafting;
        }

        public Container getInventory() {
            return inventory;
        }
    }

    /**
     * Fired on the server when a player joins.
     *
     * <p>Fabric: fired from {@code ServerPlayConnectionEvents.JOIN}.
     */
    public static class PlayerLoggedInEvent extends PlayerEvent {
        public PlayerLoggedInEvent(Player player) {
            super(player);
        }
    }
}
