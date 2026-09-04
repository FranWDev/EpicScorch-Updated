package com.boone.epicscorch.client.handler;

import com.boone.epicscorch.forge.events.BalanceHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

/**
 * Detects when dodge ends (inaction() true → false) and forces reload layer to
 * stay off.
 * Epic Fight re-activates RELOAD layer after dodge because isUsingItem()
 * remains true.
 * This catches the transition and prevents re-activation.
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = "epicscorch", value = Dist.CLIENT)
public class ReloadAnimationCleaner {

    private static boolean wasInaction = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            wasInaction = false;
            return;
        }

        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch == null || !playerPatch.isEpicFightMode()) {
            wasInaction = false;
            return;
        }

        EntityState state = playerPatch.getEntityState();
        boolean isInaction = state.inaction();

        // Detect transition: inaction() true → false (just exited dodge/roll)
        if (wasInaction && !isInaction) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof GunItem) {
                ClientAnimator animator = (ClientAnimator) playerPatch.getAnimator();
                if (animator != null) {
                    try {
                        // Force reload layer off to prevent re-activation
                        Layer reloadLayer = animator.getCompositeLayer(Layer.Priority.MIDDLE);
                        if (reloadLayer != null && !reloadLayer.isOff()) {
                            reloadLayer.off(playerPatch);
                        }
                    } catch (Exception e) {
                        // Ignore errors in cleanup
                    }
                }

                // Clean NBT flags so Epic Fight doesn't re-detect reload
                CompoundTag tag = BalanceHandler.getOrCreateStackTag(stack);
                tag.putBoolean("scguns:IsReloading", false);
                tag.putString("scguns:ReloadState", "");
                tag.putBoolean("scguns:IsPlayingReloadStop", false);
                BalanceHandler.setStackTag(stack, tag);
            }
        }

        wasInaction = isInaction;
    }
}
