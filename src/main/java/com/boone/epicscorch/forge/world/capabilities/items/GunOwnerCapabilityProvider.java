package com.boone.epicscorch.forge.world.capabilities.items;

import com.boone.epicscorch.forge.ModCapabilities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import top.ribs.scguns.init.ModItems;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@EventBusSubscriber(modid = "epicscorch")
public class GunOwnerCapabilityProvider {

   public static void registerCapabilities(RegisterCapabilitiesEvent event) {
      event.registerItem(EpicFightCapabilities.CAPABILITY_ITEM, GunCapabilityProvider.INSTANCE, 
         ModItems.ITEMS.getEntries().stream().map(entry -> entry.get()).toArray(Item[]::new)
      );
   }


   @SubscribeEvent
   public static void onLivingTick(EntityTickEvent.Pre event) {
      if (event.getEntity() instanceof LivingEntity entity) {
         entity.setData(ModCapabilities.OWNER_ID, entity.getId());
      }
   }
}

