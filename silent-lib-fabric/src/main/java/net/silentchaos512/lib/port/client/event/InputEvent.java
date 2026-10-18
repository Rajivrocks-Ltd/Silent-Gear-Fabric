package net.silentchaos512.lib.port.client.event;

import net.silentchaos512.lib.port.event.CancellableEvent;
import net.silentchaos512.lib.port.event.Event;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

/**
 * Client input events. Client only.
 *
 * <p>Fabric port stand-in for NeoForge's {@code InputEvent}.
 */
public abstract class InputEvent extends Event {
    /**
     * Fired when a keyboard key is pressed or released.
     *
     * <p>Fabric: fired from {@code ScreenKeyboardEvents.afterKeyPress} / {@code afterKeyRelease}, so only
     * while a screen is open. NeoForge fires it for every key; the Silent mods only use it for keys
     * that act on the item under the mouse in a screen.
     */
    public static class Key extends InputEvent {
        private final int key;
        private final int scanCode;
        private final int action;
        private final int modifiers;

        public Key(int key, int scanCode, int action, int modifiers) {
            this.key = key;
            this.scanCode = scanCode;
            this.action = action;
            this.modifiers = modifiers;
        }

        /** @return the GLFW key code */
        public int getKey() {
            return key;
        }

        /** @return the platform scan code */
        public int getScanCode() {
            return scanCode;
        }

        /** @return {@code GLFW_PRESS}, {@code GLFW_RELEASE} or {@code GLFW_REPEAT} */
        public int getAction() {
            return action;
        }

        /** @return the GLFW modifier bits */
        public int getModifiers() {
            return modifiers;
        }
    }

    /**
     * Fired when the attack, use or pick block key mapping is clicked.
     *
     * <p>Fabric: only the attack key is fired, from {@code ClientPreAttackCallback} on clicks (not while
     * holding). Use and pick block are not fired yet.
     */
    public static class InteractionKeyMappingTriggered extends CancellableEvent {
        private final int button;
        private final KeyMapping keyMapping;
        private final InteractionHand hand;
        private boolean handSwing = true;

        public InteractionKeyMappingTriggered(int button, KeyMapping keyMapping, InteractionHand hand) {
            this.button = button;
            this.keyMapping = keyMapping;
            this.hand = hand;
        }

        public boolean isAttack() {
            return button == 0;
        }

        public boolean isUseItem() {
            return button == 1;
        }

        public boolean isPickBlock() {
            return button == 2;
        }

        public KeyMapping getKeyMapping() {
            return keyMapping;
        }

        public InteractionHand getHand() {
            return hand;
        }

        public void setSwingHand(boolean value) {
            handSwing = value;
        }

        public boolean shouldSwingHand() {
            return handSwing;
        }

        /**
         * Creates the event for an attack click.
         *
         * @param minecraft the client
         * @return the event
         */
        public static InteractionKeyMappingTriggered attack(Minecraft minecraft) {
            return new InteractionKeyMappingTriggered(0, minecraft.options.keyAttack, InteractionHand.MAIN_HAND);
        }
    }
}
