package com.boone.epicscorch.forge.client;

import com.boone.epicscorch.forge.events.AbstractClientPlayerPatchMixin;

public class ClientEventHandler {
   public static void registerClient() {
      AbstractClientPlayerPatchMixin.register();
   }
}

