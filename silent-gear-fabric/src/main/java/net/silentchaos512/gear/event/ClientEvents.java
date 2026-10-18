package net.silentchaos512.gear.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.silentchaos512.lib.port.client.event.InputEvent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.network.payload.client.SwingGearPayload;
import net.silentchaos512.gear.util.GearHelper;

public final class ClientEvents {
    private ClientEvents() {}

    public static void onClick(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack()) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            HitResult raytrace = mc.hitResult;

            if (player != null && (raytrace == null || raytrace.getType() == HitResult.Type.MISS)) {
                ItemStack stack = player.getMainHandItem();

                if (GearHelper.isGear(stack)) {
                    ClientPlayNetworking.send(new SwingGearPayload());
                }
            }
        }
    }
}
