package net.silentchaos512.lib.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Routes block entity update packets on the client through
 * {@link net.silentchaos512.lib.port.IBlockEntityExtension#onDataPacket}, as NeoForge does.
 *
 * <p>Fabric port only. Vanilla loads the packet data directly with {@code loadWithComponents}; the
 * default {@code onDataPacket} does exactly that, so block entities that do not override it behave the
 * same. Client only.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Dynamic("Lambda body of handleBlockEntityData")
    @WrapOperation(
            method = "lambda$handleBlockEntityData$0",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/world/level/storage/ValueInput;)V")
    )
    private void silentlib$onDataPacket(BlockEntity blockEntity, ValueInput input, Operation<Void> original) {
        blockEntity.onDataPacket(((ClientPacketListener) (Object) this).getConnection(), input);
    }
}
