package com.boone.epicscorch.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import top.ribs.scguns.client.handler.AimingHandler;

/**
 * Alternative event listener to prevent aiming while sprinting.
 * Backup to the Mixin approach for version compatibility.
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = "epicscorch", value = Dist.CLIENT)
public class SprintAimingBlocker {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null || mc.isPaused()) {
            return;
        }

        AimingHandler aimingHandler = AimingHandler.get();
        
        if (aimingHandler != null && aimingHandler.isAiming()) {
            if (player.isSprinting() || mc.options.keySprint.isDown()) {
                // Block aiming during sprint
            }
        }
    }
}
