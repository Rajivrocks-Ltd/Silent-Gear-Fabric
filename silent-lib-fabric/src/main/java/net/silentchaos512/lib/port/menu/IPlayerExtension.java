package net.silentchaos512.lib.port.menu;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

import java.util.OptionalInt;
import java.util.function.Consumer;

/**
 * Extra player methods that NeoForge adds to every {@link Player}, for the Fabric port.
 *
 * <p>Injected into {@link Player} by Silent Lib. The menu methods open a menu whose type was made
 * with {@link IMenuTypeExtension#create(IContainerFactory)}, sending extra data to the client.
 */
public interface IPlayerExtension {
    private Player self() {
        return (Player) this;
    }

    /**
     * Opens a menu and sends extra data that the client menu reads when it is created. Does
     * nothing on the client, like vanilla's {@code openMenu}.
     *
     * @param menuProvider    creates the server menu
     * @param extraDataWriter writes the extra data
     * @return the container id, or empty if no menu was opened
     */
    default OptionalInt openMenu(@Nullable MenuProvider menuProvider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        if (menuProvider == null || !(self() instanceof ServerPlayer serverPlayer)) {
            return OptionalInt.empty();
        }
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), serverPlayer.registryAccess());
        byte[] bytes;
        try {
            extraDataWriter.accept(buf);
            bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
        } finally {
            buf.release();
        }
        return serverPlayer.openMenu(new BytesMenuProvider(menuProvider, bytes));
    }

    /**
     * Opens a menu and sends a block position as the extra data.
     *
     * @param menuProvider creates the server menu
     * @param pos          the position to send
     * @return the container id, or empty if no menu was opened
     */
    default OptionalInt openMenu(@Nullable MenuProvider menuProvider, BlockPos pos) {
        return openMenu(menuProvider, buf -> buf.writeBlockPos(pos));
    }

    /**
     * Wraps a menu provider so Fabric sends the given bytes as screen opening data.
     */
    record BytesMenuProvider(MenuProvider delegate, byte[] bytes) implements ExtendedMenuProvider<byte[]> {
        @Override
        public byte[] getScreenOpeningData(ServerPlayer player) {
            return bytes;
        }

        @Override
        public Component getDisplayName() {
            return delegate.getDisplayName();
        }

        @Override
        public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
            return delegate.createMenu(containerId, inventory, player);
        }
    }
}
