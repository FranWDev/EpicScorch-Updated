package com.boone.epicscorch.mixins.scg;

import com.boone.epicscorch.forge.ModCapabilities;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import top.ribs.scguns.client.GunItemStackRenderer;
import top.ribs.scguns.client.handler.GunRenderingHandler;

@Mixin(GunItemStackRenderer.class)
public class GunItemStackRendererMixin {

   @Overwrite(remap = false)
   public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource source, int light, int overlay) {
      Minecraft mc = Minecraft.getInstance();
      LivingEntity livingEntity = null;

      if (mc.level != null && stack.hasData(ModCapabilities.OWNER_ID)) {
         int ownerId = stack.getData(ModCapabilities.OWNER_ID);
         if (mc.level.getEntity(ownerId) instanceof LivingEntity living) {
            livingEntity = living;
         }
      }

      if (livingEntity == null) {
         livingEntity = mc.player;
      }

      poseStack.popPose();
      poseStack.pushPose();

      if (context == ItemDisplayContext.GROUND) {
         GunRenderingHandler.get().applyWeaponScale(stack, poseStack);
      }

      float deltaTicks = mc.getTimer() != null ? mc.getTimer().getGameTimeDeltaTicks() : 0.0f;
      GunRenderingHandler.get().renderWeapon(livingEntity, stack, context, poseStack, source, light, deltaTicks);
      
      poseStack.popPose();
      poseStack.pushPose();
   }
}

