package com.boone.epicscorch.forge.world.capabilities.items;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.item.GrenadeItem;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class GunCapabilityProvider implements ICapabilityProvider, NonNullSupplier<CapabilityItem> {
   private final LazyOptional<CapabilityItem> optional = LazyOptional.of(this);
   private final CapabilityItem capability;

   public GunCapabilityProvider(ItemStack itemStack) {
      Item item = itemStack.getItem();
      String gripType = "UNKNOWN";
      String registryPath = "unknown";

      try {
          ResourceLocation loc = ForgeRegistries.ITEMS.getKey(item);
          if (loc != null) registryPath = loc.getPath().toLowerCase();
      } catch (Exception ignored) {}

      try {
          if (item instanceof GunItem gunItem) {
              Object gun = gunItem.getGun();
              Object general = gun.getClass().getMethod("getGeneral").invoke(gun);
              
              // Access the gripType field directly to avoid getGripType(ItemStack)
              // which internally calls getModifiedGun(ItemStack) — that can fail
              // during AttachCapabilitiesEvent if item NBT is not yet initialized.
              Object grip = null;
              Class<?> cls = general.getClass();
              while (cls != null && grip == null) {
                  try {
                      Field gripField = cls.getDeclaredField("gripType");
                      gripField.setAccessible(true);
                      grip = gripField.get(general);
                  } catch (NoSuchFieldException ignored) {
                      cls = cls.getSuperclass();
                  }
              }
              // Last resort: call the method directly
              if (grip == null) {
                  grip = general.getClass().getMethod("getGripType", ItemStack.class).invoke(general, itemStack);
              }
              
               String resolvedGrip = null;
               if (grip instanceof Enum<?> enumGrip) {
                   resolvedGrip = enumGrip.name();
               } else if (grip != null) {
                   resolvedGrip = grip.toString();
               }

               if (resolvedGrip != null) {
                   int commaIndex = resolvedGrip.indexOf(",");
                   if (commaIndex != -1) {
                       resolvedGrip = resolvedGrip.substring(0, commaIndex).trim();
                   }
                   gripType = resolvedGrip.toUpperCase();
                   if (gripType.contains(":")) {
                       gripType = gripType.substring(gripType.lastIndexOf(":") + 1);
                   }
               }
          } else {
              throw new Exception();
          }
      } catch (Exception e) {
          if (registryPath.contains("pistol") || registryPath.contains("revolver") || registryPath.contains("spirulida") || registryPath.contains("hand_cannon")) {
              gripType = "ONE_HANDED";
          } else if (registryPath.contains("rifle") || registryPath.contains("musket") || registryPath.contains("carbine") || registryPath.contains("shotgun") || registryPath.contains("blunderbuss") || registryPath.contains("longarm")) {
              gripType = "TWO_HANDED_SHOTGUN";
          } else if (registryPath.contains("bazooka") || registryPath.contains("rocket") || registryPath.contains("launcher")) {
              gripType = "BAZOOKA";
          } else if (registryPath.contains("minigun") || registryPath.contains("mini_gun") || registryPath.contains("gatling")) {
              gripType = "MINI_GUN";
          } else {
              gripType = "TWO_HANDED_SHOTGUN"; 
          }
      }

      this.capability = switch (gripType) {
         case "ONE_HANDED", "ONE_HANDED_2", "DUAL_WIELD" -> GunCapabilityPresets.PISTOL.apply(item).build();
         case "TWO_HANDED", "TWO_HANDED_SHOTGUN", "TWO_HANDED_SMG", "TWO_HANDED_RELOADABLE" -> GunCapabilityPresets.RIFLE.apply(item).build();
         case "BAZOOKA" -> GunCapabilityPresets.BAZOOKA.apply(item).build();
         case "MINI_GUN", "MINI_GUN_2", "MINI_GUN_3", "MINI_GUN_4", "MINI_GUN_5" -> GunCapabilityPresets.MINI_GUN.apply(item).build();
         default -> {
             if (item instanceof GrenadeItem grenadeItem) {
                 yield GunCapabilityPresets.GRENADE.apply(grenadeItem).build();
             }
             yield GunCapabilityPresets.RIFLE.apply(item).build();
         }
      };
   }

   @NotNull
   @Override
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
      return cap == EpicFightCapabilities.CAPABILITY_ITEM ? this.optional.cast() : LazyOptional.empty();
   }

   @NotNull
   @Override
   public CapabilityItem get() {
      return this.capability;
   }

   public boolean hasCapability() {
      return this.capability != null;
   }
}
