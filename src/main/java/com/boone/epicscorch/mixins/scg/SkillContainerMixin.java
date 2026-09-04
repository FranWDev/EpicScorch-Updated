package com.boone.epicscorch.mixins.scg;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.event.types.player.SkillCastEvent;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;

@Mixin(value = SkillContainer.class, remap = false)
public abstract class SkillContainerMixin {

    @Inject(method = "sendCastRequest", at = @At("HEAD"), cancellable = true)
    private void epicscorch$blockComboSkillRequest(LocalPlayerPatch executor, ControlEngine controlEngine, CallbackInfoReturnable<SkillCastEvent> cir) {
        SkillContainer container = (SkillContainer) (Object) this;
        
        if (container.getSlot() == SkillSlots.COMBO_ATTACKS) {
            ItemStack stack = executor.getOriginal().getMainHandItem();
            if (stack.getItem() instanceof GunItem) {
                cir.setReturnValue(new SkillCastEvent(executor, container, null));
            }
        }
    }
}
