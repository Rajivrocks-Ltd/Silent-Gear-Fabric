package net.silentchaos512.lib.port.internal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import net.silentchaos512.lib.port.event.NeoForge;
import net.silentchaos512.lib.port.event.level.BlockDropsEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal: drops a broken block's items and experience through {@link BlockDropsEvent}, as NeoForge does.
 *
 * <p>Used by Silent Lib's {@code BlockDropsMixin} for vanilla's {@code Block.dropResources} variant that knows
 * the breaker and tool. Vanilla drops experience inside {@code spawnAfterBreak}; NeoForge computes it first so
 * listeners can change it. Here {@code spawnAfterBreak} runs as usual while the experience it would drop is
 * captured, then the event is posted with the drops and that experience, then both are spawned unless the
 * event was canceled. Items are created and spawned with the same rules as vanilla's {@code popResource}.
 */
public final class BlockDropsHook {
    private static final ThreadLocal<int @Nullable []> CAPTURED_EXPERIENCE = new ThreadLocal<>();

    private BlockDropsHook() {}

    /**
     * Adds experience to the current capture, if one is running.
     *
     * @param amount the experience a block is about to drop
     * @return true if it was captured, so the caller must not drop it
     */
    public static boolean captureExperience(int amount) {
        int[] captured = CAPTURED_EXPERIENCE.get();
        if (captured == null) {
            return false;
        }
        captured[0] += amount;
        return true;
    }

    /**
     * Replaces vanilla's {@code Block.dropResources(state, level, pos, blockEntity, breaker, tool)} on the server.
     *
     * @param state       the broken block
     * @param level       the level
     * @param pos         the position
     * @param blockEntity the block entity, or null
     * @param breaker     the entity that broke the block, or null
     * @param tool        the tool used
     */
    public static void dropResources(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity breaker, ItemStack tool) {
        List<ItemEntity> drops = new ArrayList<>();
        for (ItemStack stack : Block.getDrops(state, level, pos, blockEntity, breaker, tool)) {
            if (!stack.isEmpty()) {
                drops.add(createItemEntity(level, pos, stack));
            }
        }

        int[] previous = CAPTURED_EXPERIENCE.get();
        int[] captured = {0};
        CAPTURED_EXPERIENCE.set(captured);
        try {
            state.spawnAfterBreak(level, pos, tool, true);
        } finally {
            CAPTURED_EXPERIENCE.set(previous);
        }

        BlockDropsEvent event = new BlockDropsEvent(level, pos, state, blockEntity, drops, breaker, tool);
        event.setDroppedExperience(captured[0]);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled() || !level.getGameRules().get(GameRules.BLOCK_DROPS)) {
            return;
        }

        for (ItemEntity entity : event.getDrops()) {
            level.addFreshEntity(entity);
        }
        if (event.getDroppedExperience() > 0) {
            ExperienceOrb.award(level, Vec3.atCenterOf(pos), event.getDroppedExperience());
        }
    }

    /**
     * Creates an item entity the way vanilla's {@code Block.popResource} does.
     */
    private static ItemEntity createItemEntity(ServerLevel level, BlockPos pos, ItemStack stack) {
        double halfHeight = EntityType.ITEM.getHeight() / 2.0;
        RandomSource random = level.getRandom();
        double x = pos.getX() + 0.5 + Mth.nextDouble(random, -0.25, 0.25);
        double y = pos.getY() + 0.5 + Mth.nextDouble(random, -0.25, 0.25) - halfHeight;
        double z = pos.getZ() + 0.5 + Mth.nextDouble(random, -0.25, 0.25);
        ItemEntity entity = new ItemEntity(level, x, y, z, stack);
        entity.setDefaultPickUpDelay();
        return entity;
    }
}
