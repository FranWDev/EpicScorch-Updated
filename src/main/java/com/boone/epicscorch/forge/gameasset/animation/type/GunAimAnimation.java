package com.boone.epicscorch.forge.gameasset.animation.type;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.animation.types.AimAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class GunAimAnimation extends AimAnimation {
   public GunAimAnimation(float transitionTime, boolean repeatPlay, AnimationAccessor<? extends AimAnimation> accessor, String path1, String path2, String path3, String path4, AssetAccessor<? extends Armature> armature) {
      super(transitionTime, repeatPlay, accessor, path1, path2, path3, path4, armature);

      // Extend speed modifier logic to support Scorched Guns ADS (Aim Down Sights).
      // Parent implementation only accounts for vanilla item usage states.
      this.addProperty(
         StaticAnimationProperty.PLAY_SPEED_MODIFIER,
         (DynamicAnimation animation, LivingEntityPatch<?> entitypatch, float speed, float prevElapsedTime, float elapsedTime) -> {
            if (animation.isLinkAnimation()) {
               return 1.0F;
            }

            // Lock animation at the final frame during ADS or vanilla ranged weapon usage.
            boolean isAiming = (FMLEnvironment.dist.isClient() && isAimingClientSafe()) || entitypatch.getOriginal().isUsingItem();
            if (isAiming) {
               return (this.getTotalTime() - elapsedTime) / this.getTotalTime();
            }

            return 1.0F;
         }
      );
   }

    private static java.lang.reflect.Method isAimingMethod = null;
    private static Object aimingHandlerInstance = null;
    private static boolean initialized = false;

    private static boolean isAimingClientSafe() {
        if (!initialized) {
            try {
                Class<?> handlerClass = Class.forName("top.ribs.scguns.client.handler.AimingHandler");
                aimingHandlerInstance = handlerClass.getMethod("get").invoke(null);
                isAimingMethod = handlerClass.getMethod("isAiming");
            } catch (Exception ignored) {
            }
            initialized = true;
        }
        if (isAimingMethod != null && aimingHandlerInstance != null) {
            try {
                return (boolean) isAimingMethod.invoke(aimingHandlerInstance);
            } catch (Exception ignored) {
            }
        }
        return false;
    }

   @Override
   public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
      if (!entitypatch.isFirstPerson()) {
         JointTransform chest = pose.orElseEmpty("Chest");
         JointTransform head = pose.orElseEmpty("Head");
         float maxRotation = 90.0F;
         LivingEntity entity = entitypatch.getOriginal();
         float headPitch = Math.abs(entity.getXRot());
         float ratio = (maxRotation - headPitch) / maxRotation;
         float bodyYaw = entity.yBodyRot;
         float headYaw = entity.yHeadRot;
         Quaternionf qHead = new Quaternionf().rotationY(Mth.wrapDegrees(bodyYaw - headYaw) * ratio * (float) (Math.PI / 180.0));
         Quaternionf qBody = new Quaternionf().rotationY(Mth.wrapDegrees(headYaw - bodyYaw) * ratio * (float) (Math.PI / 180.0));

         if (entitypatch.getCurrentLivingMotion() == LivingMotions.SWIM) {
            qHead.rotateX((float) Math.toRadians(-80.0));
         }

         head.frontResult(JointTransform.rotation(qHead), OpenMatrix4f::mul);
         chest.frontResult(JointTransform.rotation(qBody), OpenMatrix4f::mul);
      }
   }
}
