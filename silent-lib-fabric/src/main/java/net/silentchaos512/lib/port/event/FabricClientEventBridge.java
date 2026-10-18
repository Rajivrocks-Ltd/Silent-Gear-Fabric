package net.silentchaos512.lib.port.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.silentchaos512.lib.port.client.event.InputEvent;
import net.silentchaos512.lib.port.event.entity.player.ItemTooltipEvent;
import net.silentchaos512.lib.port.event.tick.LevelTickEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Posts client-side events on {@link NeoForge#EVENT_BUS} from Fabric API client callbacks.
 *
 * <p>Fabric port only. Only load this class on the physical client.
 */
public final class FabricClientEventBridge {
    private static boolean initialized = false;

    private FabricClientEventBridge() {}

    /**
     * Registers the client bridges. Called once from Silent Lib's client init.
     */
    public static void init() {
        if (initialized) return;
        initialized = true;

        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) ->
                NeoForge.EVENT_BUS.post(new ItemTooltipEvent(stack, Minecraft.getInstance().player, lines, flag, context)));

        ClientTickEvents.START_LEVEL_TICK.register(level ->
                NeoForge.EVENT_BUS.post(new LevelTickEvent.Pre(level)));
        ClientTickEvents.END_LEVEL_TICK.register(level ->
                NeoForge.EVENT_BUS.post(new LevelTickEvent.Post(level)));

        // Key presses while a screen is open
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.afterKeyPress(screen).register((s, event) ->
                    NeoForge.EVENT_BUS.post(new InputEvent.Key(event.key(), event.scancode(), GLFW.GLFW_PRESS, event.modifiers())));
            ScreenKeyboardEvents.afterKeyRelease(screen).register((s, event) ->
                    NeoForge.EVENT_BUS.post(new InputEvent.Key(event.key(), event.scancode(), GLFW.GLFW_RELEASE, event.modifiers())));
        });

        // Attack clicks. Returning true would cancel vanilla's attack handling.
        ClientPreAttackCallback.EVENT.register((client, player, clickCount) ->
                clickCount != 0 && NeoForge.EVENT_BUS.post(InputEvent.InteractionKeyMappingTriggered.attack(client)).isCanceled());
    }
}
