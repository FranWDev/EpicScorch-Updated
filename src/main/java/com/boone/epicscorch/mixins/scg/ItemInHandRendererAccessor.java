package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererAccessor {
    @Accessor(value = "mainHandHeight")
    void setMainHandHeight(float height);

    @Accessor(value = "offHandHeight")
    void setOffHandHeight(float height);
}
