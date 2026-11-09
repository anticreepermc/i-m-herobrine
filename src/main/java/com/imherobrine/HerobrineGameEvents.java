//? forge {
package com.imherobrine;

import com.imherobrine.common.HerobrineGameLogic;
import com.imherobrine.common.HerobrineAction;
import com.imherobrine.common.HerobrinePermissionConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

public final class HerobrineGameEvents {
    private HerobrineGameEvents() {
    }

    public static void handleServerAction(ServerPlayer player, HerobrineAction action) {
        HerobrineGameLogic.handleServerAction(player, action);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        HerobrinePermissionConfig.load(event.getServer());
        HerobrineGameLogic.placeTotemIfNeeded(event.getServer().overworld());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        HerobrinePermissionConfig.registerCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            HerobrineGameLogic.onPlayerTick(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HerobrineGameLogic.onPlayerChangedDimension(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HerobrineGameLogic.onPlayerRespawn(player);
        }
    }
}

//?}
