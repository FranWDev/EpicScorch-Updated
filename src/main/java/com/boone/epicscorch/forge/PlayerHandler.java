package com.boone.epicscorch.forge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.animation.LivingMotion;

@EventBusSubscriber(modid = "epicscorch")
public class PlayerHandler {
   private static final Map<UUID, LivingMotion> preLivingMotions = new HashMap<>();

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      Player player = event.getEntity();
      preLivingMotions.remove(player.getUUID());

      if (!player.level().isClientSide) {
         for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
             ItemStack stack = player.getInventory().getItem(i);
             if (stack.getItem() instanceof GunItem) {
                 clearReloadTagsFromStack(stack);
             }
         }
      }
   }

   @SubscribeEvent
   public static void onItemToss(ItemTossEvent event) {
      ItemStack stack = event.getEntity().getItem();
      if (!event.getEntity().level().isClientSide && stack.getItem() instanceof GunItem) {
          clearReloadTagsFromStack(stack);
      }
   }

   private static void clearReloadTagsFromStack(ItemStack stack) {
      CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null && !customData.isEmpty()) {
          CompoundTag tag = customData.copyTag();
          clearReloadNbtTags(tag);
          if (tag.isEmpty()) {
              stack.remove(DataComponents.CUSTOM_DATA);
          } else {
              stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
          }
      }
   }

   public static void clearReloadNbtTags(CompoundTag tag) {
      tag.remove("ReloadTick");
      tag.remove("ReloadLoopTick");
      tag.remove("ReloadComplete");
      tag.remove("IsReloading");
      tag.remove("IsManualReload");
      tag.remove("InCriticalReloadPhase");
      tag.remove("InReloadLoop");
      tag.remove("Reloading");
      tag.remove("scguns:ReloadComplete");
      tag.remove("scguns:ReloadProgress");
      tag.remove("scguns:ReloadTick");
      tag.remove("scguns:ReloadLoopTick");
      tag.remove("scguns:AnimationReloadState");
      tag.remove("scguns:IsPlayingReloadStop");
      tag.remove("scguns:IsPlayingReloadLoop");
      tag.remove("scguns:IsReloading");
      tag.remove("scguns:Reloading");
      tag.remove("scguns:ShouldStopAfterLoop");
      tag.remove("scguns:ReloadState");
   }

   public static Map<UUID, LivingMotion> getPreLivingMotions() {
      return preLivingMotions;
   }
}

