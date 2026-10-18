package net.silentchaos512.lib.port.client.event;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Entity renderer setup. Client only.
 *
 * <p>Fabric port stand-in for NeoForge's {@code EntityRenderersEvent}.
 */
public abstract class EntityRenderersEvent {
    /**
     * Lets client setup code register entity and block entity renderers. Not posted on a bus (see
     * {@link RegisterKeyMappingsEvent}). Forwards to Fabric's {@link EntityRendererRegistry} and to
     * vanilla's {@link BlockEntityRenderers}, which Fabric API makes accessible.
     */
    public static class RegisterRenderers extends EntityRenderersEvent {
        /**
         * Registers the renderer for an entity type.
         *
         * @param entityType the entity type
         * @param provider   creates the renderer
         * @param <T>        the entity class
         */
        public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> entityType, EntityRendererProvider<T> provider) {
            EntityRendererRegistry.register(entityType, provider);
        }

        /**
         * Registers the renderer for a block entity type.
         *
         * @param blockEntityType the block entity type
         * @param provider        creates the renderer
         * @param <T>             the block entity class
         * @param <S>             the render state class
         */
        public <T extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T, S> provider) {
            BlockEntityRenderers.register(blockEntityType, provider);
        }
    }
}
