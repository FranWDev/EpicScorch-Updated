package com.boone.epicscorch.forge;

import net.neoforged.neoforge.common.NeoForge;

public class ServerEventHandler {
   public static void registerServer() {
      NeoForge.EVENT_BUS.register(ServerEventHandler.class);
   }
}

