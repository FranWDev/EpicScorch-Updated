package com.boone.epicscorch.mixins.scg;

import com.boone.epicscorch.config.EpicScorchConfig;
import com.boone.epicscorch.forge.events.BalanceHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.client.handler.ReloadHandler;

/**
 * Intercepts ReloadHandler to enforce movement restrictions.
 * Prevents starting a reload during sprints or dodges.
 */
@Mixin(value = ReloadHandler.class, remap = false)
public abstract class ReloadHandlerMixin {

    private static final KeyMapping DUMMY_MAPPING = new KeyMapping("epicscorch.dummy", -1, "key.categories.scguns") {
        @Override
        public boolean isDown() {
            return false;
        }
    };

    @Redirect(
        method = "onMouseInput(Lnet/minecraftforge/client/event/InputEvent$MouseButton;)V",
        at = @At(value = "INVOKE",
                 target = "Ltop/ribs/scguns/client/KeyBinds;getAimMapping()Lnet/minecraft/client/KeyMapping;",
                 remap = false)
    )
    private KeyMapping epicscorch$getAimMappingMouse() {
        return BalanceHandler.shouldBlockAiming(Minecraft.getInstance().player)
                ? DUMMY_MAPPING
                : KeyBinds.getAimMapping();
    }

    @Inject(method = "onKeyPressed", at = @At("HEAD"), cancellable = true)
    private void epicscorch$blockReloadStart(InputEvent.Key event, CallbackInfo ci) {
        if (!EpicScorchConfig.CANCEL_RELOAD_ON_ACTION.get()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && BalanceHandler.shouldBlockReloading(player)) {
            ci.cancel();
        }
    }

    /**
     * No-op injection retained as a placeholder.
     * Reload-tick logic is managed centrally by {@link BalanceHandler}.
     */
    @Inject(method = "onClientTick(Lnet/minecraftforge/event/TickEvent$ClientTickEvent;)V", at = @At("HEAD"))
    private void epicscorch$onClientTickHead(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        // Handled in BalanceHandler.
    }
}
