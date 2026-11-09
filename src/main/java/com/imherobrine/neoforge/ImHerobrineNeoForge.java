//? neoforge {
package com.imherobrine.neoforge;

import com.imherobrine.common.HerobrineGameLogic;
import com.imherobrine.common.HerobrinePermissionConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.Mod;

@Mod("imherobrine")
public final class ImHerobrineNeoForge {
    public ImHerobrineNeoForge(IEventBus modBus) {
        modBus.addListener(NeoForgeNetworking::register);
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                HerobrinePermissionConfig.registerCommands(event.getDispatcher()));
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> {
            HerobrinePermissionConfig.load(event.getServer());
            HerobrineGameLogic.placeTotemIfNeeded(event.getServer().overworld());
        });
        NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post event) -> {
            if (event.getEntity() instanceof ServerPlayer player) HerobrineGameLogic.onPlayerTick(player);
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) HerobrineGameLogic.onPlayerChangedDimension(player);
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) HerobrineGameLogic.onPlayerRespawn(player);
        });
    }
}

//?}
