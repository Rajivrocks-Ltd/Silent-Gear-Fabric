package net.silentchaos512.lib.port.event.level;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.silentchaos512.lib.port.event.CancellableEvent;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Fired when a broken block is about to drop its items and experience. Listeners can change the
 * drop list and the experience. Canceling it drops nothing.
 *
 * <p>Fabric: not fired yet. Needs a mixin around block drops (milestone M8).
 */
public class BlockDropsEvent extends CancellableEvent {
    private final ServerLevel level;
    private final BlockPos pos;
    private final BlockState state;
    private final @Nullable BlockEntity blockEntity;
    private final List<ItemEntity> drops;
    private final @Nullable Entity breaker;
    private final ItemStack tool;
    private int experience = 0;

    public BlockDropsEvent(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, List<ItemEntity> drops, @Nullable Entity breaker, ItemStack tool) {
        this.level = level;
        this.pos = pos;
        this.state = state;
        this.blockEntity = blockEntity;
        this.drops = drops;
        this.breaker = breaker;
        this.tool = tool;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public List<ItemEntity> getDrops() {
        return drops;
    }

    public @Nullable BlockEntity getBlockEntity() {
        return blockEntity;
    }

    public @Nullable Entity getBreaker() {
        return breaker;
    }

    public ItemStack getTool() {
        return tool;
    }

    public int getDroppedExperience() {
        return experience;
    }

    public void setDroppedExperience(int experience) {
        this.experience = Math.max(0, experience);
    }
}
