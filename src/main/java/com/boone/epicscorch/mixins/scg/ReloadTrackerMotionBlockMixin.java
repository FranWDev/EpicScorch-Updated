package com.boone.epicscorch.mixins.scg;

import com.boone.epicscorch.config.EpicScorchConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.common.ReloadTracker;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageStopReload;

import java.util.Map;

/**
 * Server-side reload interrupt based on sprint.
 * Cancels reloading if the player sprints. Jumping does not cancel the reload.
 */
@Mixin(ReloadTracker.class)
public abstract class ReloadTrackerMotionBlockMixin {

    @Shadow
    private static Map<net.minecraft.world.entity.player.Player, ReloadTracker> RELOAD_TRACKER_MAP;

    @Inject(
        method = "onPlayerTick",
        at = @At("HEAD"),
        remap = false,
        cancellable = true
    )
    private static void blockReloadDuringSprint(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!EpicScorchConfig.CANCEL_RELOAD_ON_ACTION.get()) return;


        boolean shouldCancel = player.isSprinting()
                && ModSyncedDataKeys.RELOADING.getValue(player);

        if (!shouldCancel) return;

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem) {
            CompoundTag tag = heldItem.getOrCreateTag();
            boolean hasReloadTag = tag.getBoolean("IsReloading") || tag.getBoolean("scguns:IsReloading");
            if (!ModSyncedDataKeys.RELOADING.getValue(player) && !hasReloadTag) return;

            ModSyncedDataKeys.RELOADING.setValue(player, false);
            ModSyncedDataKeys.AIMING.setValue(player, false);

            if (heldItem.getItem() instanceof AnimatedGunItem animated) {
                animated.cleanupReloadState(tag);
            }

            tag.remove("ReloadComplete");
            tag.remove("scguns:ReloadComplete");
            tag.putBoolean("scguns:PausedDuringReload", true);
            tag.remove("scguns:ReloadState");
            tag.remove("scguns:IsPlayingReloadStop");
            tag.remove("InCriticalReloadPhase");
            tag.remove("Reloading");
            tag.remove("scguns:Reloading");
            tag.remove("scguns:ShouldStopAfterLoop");

            PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageStopReload());
        }

        if (RELOAD_TRACKER_MAP != null) {
            RELOAD_TRACKER_MAP.remove(player);
        }

        ci.cancel();
    }
}
