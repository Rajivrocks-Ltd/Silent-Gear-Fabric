package net.silentchaos512.lib.port;

import net.minecraft.network.Connection;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;

/**
 * Extra block entity methods that NeoForge adds to every {@link BlockEntity}, for the Fabric port.
 *
 * <p>Injected into {@link BlockEntity} by Silent Lib, like {@link IItemExtension}.
 */
public interface IBlockEntityExtension {
    private BlockEntity self() {
        return (BlockEntity) this;
    }

    /**
     * Called on the client when the server sends this block entity's update packet. The default
     * loads the data, as vanilla does.
     *
     * <p>Hook: not called by vanilla yet; vanilla loads the data directly (milestone M7).
     *
     * @param net        the connection the packet came from
     * @param valueInput the data sent by the server
     */
    default void onDataPacket(Connection net, ValueInput valueInput) {
        self().loadWithComponents(valueInput);
    }
}
