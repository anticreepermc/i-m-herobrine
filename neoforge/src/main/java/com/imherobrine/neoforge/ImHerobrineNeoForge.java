package com.imherobrine.neoforge;

import com.imherobrine.common.HerobrineGameLogic;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.Dist;
import net.neoforged.fml.DistExecutor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.fml.common.Mod;

@Mod("imherobrine")
public final class ImHerobrineNeoForge {
    public ImHerobrineNeoForge(IEventBus modBus) {
        modBus.addListener(NeoForgeNetworking::register);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modBus.addListener(NeoForgeClient::registerKeys));
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) ->
                HerobrineGameLogic.placeTotemIfNeeded(event.getServer().overworld()));
    }
}
