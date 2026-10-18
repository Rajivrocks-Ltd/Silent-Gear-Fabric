package net.silentchaos512.lib.port.menu;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * Creates menu types whose menus receive raw extra data from the server.
 *
 * <p>Fabric port stand-in for NeoForge's {@code IMenuTypeExtension.create}. NeoForge sends the extra
 * data as raw bytes; Fabric's {@link ExtendedMenuType} sends a typed value. Here that value is the
 * raw bytes themselves, so menus written for NeoForge keep reading a {@link RegistryFriendlyByteBuf}.
 * The server side is {@link IPlayerExtension#openMenu(net.minecraft.world.MenuProvider, java.util.function.Consumer)}.
 */
public final class IMenuTypeExtension {
    private IMenuTypeExtension() {}

    /**
     * Creates a menu type for the given factory.
     *
     * @param factory creates the menu from the extra data
     * @param <T>     the menu type
     * @return the new menu type, ready to register
     */
    public static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
        return new ExtendedMenuType<>(
                (containerId, inventory, bytes) -> {
                    var buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), inventory.player.registryAccess());
                    try {
                        return factory.create(containerId, inventory, buf);
                    } finally {
                        buf.release();
                    }
                },
                ByteBufCodecs.BYTE_ARRAY
        );
    }
}
