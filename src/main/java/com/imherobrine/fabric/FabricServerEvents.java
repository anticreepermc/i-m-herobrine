//? fabric {
package com.imherobrine.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.imherobrine.common.HerobrineGameLogic;
import com.imherobrine.common.HerobrinePermissionConfig;

final class FabricServerEvents {
    private FabricServerEvents() {
    }

    static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                HerobrinePermissionConfig.registerCommands(dispatcher));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            HerobrinePermissionConfig.load(server);
            HerobrineGameLogic.placeTotemIfNeeded(server.overworld());
        });
        ServerTickEvents.END_SERVER_TICK.register(server ->
                server.getPlayerList().getPlayers().forEach(HerobrineGameLogic::onPlayerTick));
    }
}

//?}
