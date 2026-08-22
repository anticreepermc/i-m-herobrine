package com.imherobrine;

import com.imherobrine.network.HerobrineNetworking;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ImHerobrineMod.MODID)
public final class ImHerobrineMod {
    public static final String MODID = "imherobrine";

    public ImHerobrineMod(FMLJavaModLoadingContext context) {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                IExtensionPoint.DisplayTest.IGNORE_SERVER_VERSION);
        HerobrineNetworking.register();
        MinecraftForge.EVENT_BUS.register(HerobrineGameEvents.class);
    }
}
