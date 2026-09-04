package com.boone.epicscorch.mixins.scg;

import com.boone.epicscorch.config.EpicScorchConfig;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
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

/**
 * Server-side reload interrupt based on sprint.
 * Cancels reloading if the player sprints. Jumping does not cancel the reload.
 */
@Mixin(ReloadTracker.class)
public abstract class ReloadTrackerMotionBlockMixin {

    @Shadow
    private static Map<Player, ReloadTracker> RELOAD_TRACKER_MAP;

    @Inject(
        method = "onPlayerTick(Lnet/neoforged/neoforge/event/tick/PlayerTickEvent$Pre;)V",
        at = @At("HEAD"),
        remap = false,
        cancellable = true
    )
    private static void blockReloadDuringSprint(PlayerTickEvent.Pre event, CallbackInfo ci) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!EpicScorchConfig.CANCEL_RELOAD_ON_ACTION.get()) return;

        boolean shouldCancel = player.isSprinting()
                && ModSyncedDataKeys.RELOADING.getValue(player);

        if (!shouldCancel) return;

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem) {
            CustomData customData = heldItem.get(DataComponents.CUSTOM_DATA);
            CompoundTag tag = customData != null && !customData.isEmpty() ? customData.copyTag() : new CompoundTag();
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

            if (tag.isEmpty()) {
                heldItem.remove(DataComponents.CUSTOM_DATA);
            } else {
                heldItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }

            PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageStopReload());
        }

        if (RELOAD_TRACKER_MAP != null) {
            RELOAD_TRACKER_MAP.remove(player);
        }

        ci.cancel();
    }
}
