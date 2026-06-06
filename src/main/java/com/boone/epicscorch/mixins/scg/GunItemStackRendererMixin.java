package com.boone.epicscorch.mixins.scg;

import com.boone.epicscorch.forge.ModCapabilities;
import com.boone.epicscorch.forge.world.capabilities.items.GunOwnerCapabilityProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import top.ribs.scguns.client.GunItemStackRenderer;
import top.ribs.scguns.client.handler.GunRenderingHandler;

@OnlyIn(Dist.CLIENT)
@Mixin(GunItemStackRenderer.class)
public class GunItemStackRendererMixin {

   @Overwrite(remap = false)
   public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource source, int light, int overlay) {
      Minecraft mc = Minecraft.getInstance();
      GunOwnerCapabilityProvider.OwnerId ownerId = (GunOwnerCapabilityProvider.OwnerId)stack.getCapability(ModCapabilities.OWNER_ID).orElse(null);
      LivingEntity livingEntity = null;
      if (ownerId != null && mc.level.getEntity(ownerId.value) instanceof LivingEntity living) {
         livingEntity = living;
      }

      if (livingEntity == null) {
         livingEntity = mc.player;
      }

      poseStack.pushPose();
      if (context == ItemDisplayContext.GROUND) {
         GunRenderingHandler.get().applyWeaponScale(stack, poseStack);
      }

      GunRenderingHandler.get().renderWeapon(livingEntity, stack, context, poseStack, source, light, Minecraft.getInstance().getPartialTick());
      poseStack.popPose();
   }
}
