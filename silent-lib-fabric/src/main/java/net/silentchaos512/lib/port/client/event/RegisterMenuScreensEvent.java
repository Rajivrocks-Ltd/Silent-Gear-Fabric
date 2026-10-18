package net.silentchaos512.lib.port.client.event;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * Lets client setup code link menu types to their screens. Client only.
 *
 * <p>Fabric port stand-in for NeoForge's event of the same name. Not posted on a bus (see
 * {@link RegisterKeyMappingsEvent}). {@link #register} forwards to vanilla's {@link MenuScreens},
 * which Fabric API makes accessible.
 */
public class RegisterMenuScreensEvent {
    /**
     * Links a menu type to the screen that shows it.
     *
     * @param menuType          the menu type
     * @param screenConstructor creates the screen
     * @param <M>               the menu class
     * @param <U>               the screen class
     */
    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> menuType, MenuScreens.ScreenConstructor<M, U> screenConstructor) {
        MenuScreens.register(menuType, screenConstructor);
    }
}
