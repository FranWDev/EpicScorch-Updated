package com.boone.epicscorch.forge;

import com.boone.epicscorch.EpicScorch;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModCapabilities {
   public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
      DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, EpicScorch.MOD_ID);

   public static final Supplier<AttachmentType<Integer>> OWNER_ID =
      ATTACHMENT_TYPES.register("owner_id", () -> AttachmentType.builder(() -> -1).build());
}

