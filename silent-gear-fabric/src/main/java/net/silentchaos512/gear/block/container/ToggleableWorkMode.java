package net.silentchaos512.gear.block.container;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.silentchaos512.gear.network.payload.client.ToggleWorkModePayload;

public interface ToggleableWorkMode {
    boolean getWorkEnabled();

    void setWorkEnabled(boolean enabled);

    default void toggleWorkEnabled() {
        boolean newMode = !getWorkEnabled();
        setWorkEnabled(newMode);
        ClientPlayNetworking.send(new ToggleWorkModePayload(newMode));
    }
}
