package net.silentchaos512.lib.port.client.event;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

/**
 * Lets client setup code register key mappings. Client only.
 *
 * <p>Fabric port stand-in for NeoForge's event of the same name. Not posted on a bus: client init
 * creates one and passes it to the method that upstream subscribed to the NeoForge event.
 * {@link #register(KeyMapping)} forwards to Fabric's {@link KeyMappingHelper}.
 */
public class RegisterKeyMappingsEvent {
    /**
     * Registers a key mapping so it shows up in the controls screen.
     *
     * @param mapping the key mapping
     */
    public void register(KeyMapping mapping) {
        KeyMappingHelper.registerKeyMapping(mapping);
    }
}
