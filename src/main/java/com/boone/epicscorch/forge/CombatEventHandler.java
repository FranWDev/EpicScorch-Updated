package com.boone.epicscorch.forge;

import com.boone.epicscorch.EpicScorch;
import com.boone.epicscorch.config.EpicScorchConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.event.GunFireEvent;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import top.ribs.scguns.util.GunModifierHelper;
import top.ribs.scguns.util.GunEnchantmentHelper;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import top.ribs.scguns.init.ModSyncedDataKeys;

/**
 * Synchronizes combat mechanics between Epic Fight and Scorched Guns,
 * primarily managing stamina consumption during weapon use.
 */
@EventBusSubscriber(modid = EpicScorch.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public class CombatEventHandler {

    @SubscribeEvent
    public static void onGunFirePre(GunFireEvent.Pre event) {
        if (!EpicScorchConfig.ENABLE_STAMINA_REDUCTION.get()) return;

        Player player = event.getEntity();
        if (player.isCreative() || player.isSpectator()) return;

        ItemStack stack = event.getStack();
        if (stack.getItem() instanceof GunItem gunItem) {
            float requiredStamina = getRequiredStamina(player, stack, gunItem);
            if (requiredStamina <= 0.0f) return;

            EpicFightCapabilities.getUnparameterizedEntityPatch(player, PlayerPatch.class).ifPresent(playerPatch -> {
                if (!playerPatch.hasStamina(requiredStamina)) {
                    event.setCanceled(true); // Cancel shot if stamina is insufficient
                } else if (!player.level().isClientSide) {
                    if (playerPatch instanceof ServerPlayerPatch serverPlayerPatch) {
                        serverPlayerPatch.setStamina(Math.max(0.0f, serverPlayerPatch.getStamina() - requiredStamina));
                        serverPlayerPatch.setStaminaRegenAwaitTicks(EpicScorchConfig.STAMINA_REGEN_DELAY.get());
                    }
                }
            });
        }
    }

    private static float getRequiredStamina(Player player, ItemStack stack, GunItem gunItem) {
        String registryName = ForgeRegistries.ITEMS.getKey(gunItem).toString();
        List<? extends String> overrides = EpicScorchConfig.WEAPON_STAMINA_OVERRIDES.get();
        
        for (String override : overrides) {
            String[] parts = override.split("=");
            if (parts.length == 2 && parts[0].trim().equals(registryName)) {
                try {
                    return Float.parseFloat(parts[1].trim());
                } catch (NumberFormatException e) {
                    return 0.0f; // Default to zero consumption if configuration is malformed
                }
            }
        }

        Gun modifiedGun = gunItem.getModifiedGun(stack);
        
        // Extract recoil data from NBT, supporting legacy and current Scorched Guns formats
        CompoundTag generalNbt = modifiedGun.getGeneral().serializeNBT();
        CompoundTag projectileNbt = modifiedGun.getProjectile().serializeNBT();
        
        float recoilAngle = generalNbt.contains("RecoilAngle") ? generalNbt.getFloat("RecoilAngle") 
                          : projectileNbt.getFloat("RecoilAngle");
        
        // Incorporate recoil modifiers from attachments and enchantments
        float modifier = 1.0f - GunModifierHelper.getRecoilModifier(stack);
        modifier *= GunEnchantmentHelper.getRecoilModifier(player, stack);

        // Apply ADS recoil reduction if aiming
        float adsReduction = generalNbt.contains("RecoilAdsReduction") ? generalNbt.getFloat("RecoilAdsReduction") 
                           : projectileNbt.getFloat("RecoilAdsReduction");

        // Use the synced AIMING data key — available server-side and safe on both sides.
        // AimingHandler is client-only and must not be referenced here to avoid
        // ClassNotFoundException on dedicated server, which would prevent this
        // entire @EventBusSubscriber class from loading.
        if (ModSyncedDataKeys.AIMING.getValue(player)) {
            modifier *= (1.0f - adsReduction);
        }
        
        return recoilAngle * modifier * EpicScorchConfig.STAMINA_MULTIPLIER.get().floatValue();
    }
}
