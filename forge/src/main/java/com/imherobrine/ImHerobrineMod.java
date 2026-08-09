package com.imherobrine;

import com.imherobrine.network.HerobrineNetworking;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ImHerobrineMod.MODID)
public final class ImHerobrineMod {
    public static final String MODID = "imherobrine";

    public ImHerobrineMod(FMLJavaModLoadingContext context) {
        HerobrineNetworking.register();
        MinecraftForge.EVENT_BUS.register(HerobrineGameEvents.class);
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.server.ServerStartedEvent event) ->
                com.imherobrine.common.HerobrineGameLogic.placeTotemIfNeeded(event.getServer().overworld()));
    }
}
