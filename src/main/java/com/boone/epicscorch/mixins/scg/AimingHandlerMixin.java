package com.boone.epicscorch.mixins.scg;

import com.boone.epicscorch.forge.events.BalanceHandler;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.client.handler.AimingHandler;

/**
 * Intercepts AimingHandler to enforce movement restrictions.
 * Prevents aiming when the player is restricted (sprint, dodge, airborne).
 */
@Mixin(value = AimingHandler.class, remap = false)
public abstract class AimingHandlerMixin {

    @Shadow
    private boolean aiming;

    private static final KeyMapping DUMMY_MAPPING = new KeyMapping("epicscorch.dummy", -1, "key.categories.scguns") {
        @Override
        public boolean isDown() {
            return false;
        }
    };

    @Redirect(method = "onClientTick(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V", at = @At(value = "INVOKE", target = "Ltop/ribs/scguns/client/KeyBinds;getAimMapping()Lnet/minecraft/client/KeyMapping;", remap = false))
    private KeyMapping epicscorch$getAimMapping() {
        return BalanceHandler.isCurrentlyRestricted()
                ? DUMMY_MAPPING
                : KeyBinds.getAimMapping();
    }

}
