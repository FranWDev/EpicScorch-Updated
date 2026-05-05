package com.boone.epicscorch.forge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import yesman.epicfight.api.animation.LivingMotion;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import top.ribs.scguns.item.GunItem;

@EventBusSubscriber(modid = "epicscorch", bus = Bus.FORGE)
public class PlayerHandler {
   private static final Map<UUID, LivingMotion> preLivingMotions = new HashMap<>();

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      Player player = event.getEntity();
      preLivingMotions.remove(player.getUUID());

      if (!player.level().isClientSide) {
         for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
             ItemStack stack = player.getInventory().getItem(i);
             if (stack.getItem() instanceof GunItem && stack.hasTag()) {
                 clearReloadNbtTags(stack.getOrCreateTag());
             }
         }
      }
   }

   @SubscribeEvent
   public static void onItemToss(ItemTossEvent event) {
      ItemStack stack = event.getEntity().getItem();
      if (!event.getEntity().level().isClientSide && stack.getItem() instanceof GunItem && stack.hasTag()) {
          clearReloadNbtTags(stack.getOrCreateTag());
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
