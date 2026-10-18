package net.silentchaos512.lib.port.event.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.silentchaos512.lib.port.event.CancellableEvent;

/**
 * Fired on the server when a player is about to break a block. Canceling it keeps the block.
 *
 * <p>Fabric: fired from {@code PlayerBlockBreakEvents.BEFORE}.
 */
public class BreakBlockEvent extends CancellableEvent {
    private final Level level;
    private final BlockPos pos;
    private final BlockState state;
    private final Player player;

    public BreakBlockEvent(Level level, BlockPos pos, BlockState state, Player player) {
        this.level = level;
        this.pos = pos;
        this.state = state;
        this.player = player;
    }

    public Level getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public Player getPlayer() {
        return player;
    }
}
