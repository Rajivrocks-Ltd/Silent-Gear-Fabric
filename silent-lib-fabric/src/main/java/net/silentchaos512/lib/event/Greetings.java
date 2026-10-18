package net.silentchaos512.lib.event;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Can be used to send messages to the player's chat log when they log in. Good for notifying the
 * player when something has gone horribly wrong.
 *
 * <p>Port note: upstream listens to NeoForge's {@code PlayerLoggedInEvent}. Here the listener is
 * {@link ServerPlayConnectionEvents#JOIN}, registered by {@link #init()}.
 *
 * @since 2.3.17
 */
public final class Greetings {
    private static final Greetings INSTANCE = new Greetings();

    private final List<Function<Player, Optional<Component>>> messages = new ArrayList<>();

    private Greetings() {}

    /**
     * Registers the player join listener. Called once from
     * {@link net.silentchaos512.lib.SilentLib#onInitialize()}.
     */
    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                INSTANCE.onPlayerLoggedIn(handler.getPlayer()));
    }

    /**
     * Add a message to display to the player on login. If the function returns {@code null}, no
     * message is displayed. Consider displaying your message only once per session or per day.
     *
     * @param message A function to create the message.
     * @since 3.0.6
     */
    public static void addMessage(Function<Player, Component> message) {
        INSTANCE.messages.add(player -> Optional.ofNullable(message.apply(player)));
    }

    private void onPlayerLoggedIn(Player player) {
        messages.forEach(msg -> msg.apply(player).ifPresent(player::sendSystemMessage));
    }
}
