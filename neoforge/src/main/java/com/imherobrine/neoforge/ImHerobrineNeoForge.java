package com.imherobrine.neoforge;

import com.imherobrine.common.HerobrineGameLogic;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.fml.common.Mod;

@Mod("imherobrine")
public final class ImHerobrineNeoForge {
    public ImHerobrineNeoForge(IEventBus modBus) {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                IExtensionPoint.DisplayTest.IGNORE_SERVER_VERSION);
        modBus.addListener(NeoForgeNetworking::register);
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) ->
                HerobrineGameLogic.placeTotemIfNeeded(event.getServer().overworld()));
    }
}
